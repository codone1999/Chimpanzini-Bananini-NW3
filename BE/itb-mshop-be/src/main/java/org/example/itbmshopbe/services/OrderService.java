package org.example.itbmshopbe.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.itbmshopbe.dtos.OrderDTO.*;
import org.example.itbmshopbe.entities.*;

import org.example.itbmshopbe.repositories.*;
import org.example.itbmshopbe.utils.EntityValidatorUtil;
import org.example.itbmshopbe.utils.OrderSpecifications;
import org.example.itbmshopbe.utils.PagingUtil;
import org.example.itbmshopbe.utils.ValidationUtil;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final AccountRepository accountRepository;
    private final SaleItemRepository saleItemRepository;
    private final SellerRepository sellerRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final ModelMapper modelMapper;
    private final PagingUtil pagingUtil;
    private final ValidationUtil validationUtil;
    private final EntityValidatorUtil entityValidatorUtil;

    @Transactional
    public List<OrderResponseDto<OrderSellerResponseDto>> createOrder(Integer buyerId, OrderRequestDto orderRequestDto) {

        validationUtil.validateNotEmpty(orderRequestDto.getOrderItems(), "Order items");

        Account buyer = accountRepository.findById(buyerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Buyer not found"));

        Map<Integer, List<OrderItemRequestDto>> itemsBySeller = groupItemsBySeller(
                orderRequestDto.getOrderItems(),
                buyerId
        );

        List<OrderResponseDto<OrderSellerResponseDto>> createdOrders = new ArrayList<>();
        List<Integer> successfulSaleItemIds = new ArrayList<>();

        for (Map.Entry<Integer, List<OrderItemRequestDto>> entry : itemsBySeller.entrySet()) {
            OrderResponseDto<OrderSellerResponseDto> orderResponse = createOrderForSeller(
                    entry.getKey(),
                    entry.getValue(),
                    buyer,
                    orderRequestDto,
                    successfulSaleItemIds
            );
            createdOrders.add(orderResponse);
        }
        removeSuccessfulItemsFromCart(buyerId, successfulSaleItemIds);
        return createdOrders;
    }

    private Map<Integer, List<OrderItemRequestDto>> groupItemsBySeller(
            List<OrderItemRequestDto> orderItems,
            Integer buyerId
    ) {
        Map<Integer, List<OrderItemRequestDto>> itemsBySeller = new HashMap<>();

        for (OrderItemRequestDto itemDto : orderItems) {
            SaleItem saleItem = saleItemRepository.findById(itemDto.getSaleItemId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "SaleItem not found: " + itemDto.getSaleItemId()
                    ));

            Integer sellerId = saleItem.getSeller().getId();
            entityValidatorUtil.validateNotOwnItem(
                    sellerId,
                    buyerId,
                    "Cannot purchase your own items"
            );

            itemsBySeller.computeIfAbsent(sellerId, k -> new ArrayList<>()).add(itemDto);
        }
        return itemsBySeller;
    }

    private OrderResponseDto<OrderSellerResponseDto> createOrderForSeller(
            Integer sellerId,
            List<OrderItemRequestDto> sellerItems,
            Account buyer,
            OrderRequestDto orderRequestDto,
            List<Integer> successfulSaleItemIds
    ) {
        boolean isStockSufficient = checkStockAvailability(sellerItems);

        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Seller not found: " + sellerId
                ));

        Order order = createOrder(buyer, orderRequestDto, isStockSufficient);
        Order savedOrder = orderRepository.save(order);

        List<OrderItem> orderItems = createOrderItems(
                savedOrder,
                sellerItems,
                isStockSufficient,
                successfulSaleItemIds
        );
        orderItemRepository.saveAll(orderItems);

        OrderSellerResponseDto sellerDto = mapToSellerBase(seller);
        List<OrderItemRequestDto> responseItems = mapOrderItems(orderItems);

        return mapOrderToResponse(savedOrder, List.of(sellerDto), responseItems);
    }
    private boolean checkStockAvailability(List<OrderItemRequestDto> items) {
        for (OrderItemRequestDto itemDto : items) {
            SaleItem saleItem = saleItemRepository.findById(itemDto.getSaleItemId()).get();
            if (saleItem.getQuantity() < itemDto.getQuantity()) {
                return false;
            }
        }
        return true;
    }

    private Order createOrder(Account buyer, OrderRequestDto dto, boolean isStockSufficient) {
        Order order = new Order();
        order.setCustomer(buyer);
        order.setOrderNote(dto.getOrderNote());
        order.setShippingAddress(dto.getShippingAddress());
        order.setStatus(isStockSufficient ? "COMPLETED" : "CANCELLED");
        return order;
    }

    private List<OrderItem> createOrderItems(
            Order order,
            List<OrderItemRequestDto> items,
            boolean isStockSufficient,
            List<Integer> successfulSaleItemIds
    ) {
        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemRequestDto itemDto : items) {
            SaleItem saleItem = saleItemRepository.findById(itemDto.getSaleItemId()).get();

            if (isStockSufficient) {
                saleItem.setQuantity(saleItem.getQuantity() - itemDto.getQuantity());
                saleItemRepository.save(saleItem);
                successfulSaleItemIds.add(saleItem.getId());
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setSaleItem(saleItem);
            orderItem.setQuantity(itemDto.getQuantity());
            orderItem.setPriceEach(itemDto.getPrice());
            orderItems.add(orderItem);
        }

        return orderItems;
    }

    private void removeSuccessfulItemsFromCart(Integer buyerId, List<Integer> saleItemIds) {
        if (!saleItemIds.isEmpty()) {
            List<Cart> cartItemsToRemove = cartRepository.findByAccountIdAndSaleItemIdIn(
                    buyerId,
                    saleItemIds
            );
            if (!cartItemsToRemove.isEmpty()) {
                cartRepository.deleteAll(cartItemsToRemove);
            }
        }
    }

    private <T> OrderResponseDto<T> mapOrderToResponse(Order order,
                                                       List<T> sellerDtos,
                                                       List<OrderItemRequestDto> orderItems) {
        OrderResponseDto<T> response = modelMapper.map(order, OrderResponseDto.class);
        response.setSeller(sellerDtos);
        response.setOrderItems(orderItems);
        response.setBuyerId(order.getCustomer().getId());
        response.setOrderDate(order.getCreatedOn());
        response.setPaymentDate(order.getPaymentDate());
        return response;
    }

    private OrderSellerResponseDto mapToSellerBase(Seller seller) {
        OrderSellerResponseDto response = modelMapper.map(seller, OrderSellerResponseDto.class);
        response.setSellerName(seller.getAccount().getNickname());
        return response;
    }

    private OrderSellerDetailDto mapToSellerDetail(Seller seller) {
        Account account = seller.getAccount();
        OrderSellerDetailDto dto = modelMapper.map(seller, OrderSellerDetailDto.class);
        dto.setEmail(account.getEmail());
        dto.setFullName(account.getFullname());
        dto.setNickName(account.getNickname());
        dto.setUserType(account.getRole());
        return dto;
    }

    private List<OrderItemRequestDto> mapOrderItems(List<OrderItem> orderItems) {
        return orderItems.stream().map(
                oi->{
                    OrderItemRequestDto dto = modelMapper.map(oi, OrderItemRequestDto.class);
                    SaleItem saleItem = oi.getSaleItem();
                    String brandName = saleItem.getBrand().getName();
                    String model =  saleItem.getModel();
                    String storage = saleItem.getStorageGb() != null ? saleItem.getStorageGb() + "GB" : "-";
                    String color = saleItem.getColor() != null ? saleItem.getColor() : "-";
                    dto.setDescription(brandName + " " + model + " ("+ storage+" " + color+")");
                    dto.setSaleItemId(saleItem.getId());
                    dto.setPrice(saleItem.getPrice());
                    return dto;
                }
        ).toList();
    }

    @Transactional
    public OrderResponseDto<OrderSellerDetailDto> getOrderDetails(Integer orderId, Integer currentUserId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        boolean isBuyer = order.getCustomer().getId().equals(currentUserId);
        boolean isSeller = order.getOrderItems().stream()
                .anyMatch(oi -> oi.getSaleItem().getSeller().getId().equals(currentUserId));

        if (!isBuyer && !isSeller) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User id not an owner (seller/buyer) of the order");
        }
        List<Seller> sellersInOrder = order.getOrderItems().stream()
                .map(oi -> oi.getSaleItem().getSeller())
                .distinct()
                .toList();
        List<OrderSellerDetailDto> sellerDetails = sellersInOrder.stream()
                .map(this::mapToSellerDetail)
                .toList();
        List<OrderItemRequestDto> orderItems = mapOrderItems(order.getOrderItems());

        return mapOrderToResponse(order, sellerDetails, orderItems);
    }

    @Transactional
    public OrderPagedResponseDto<OrderSellerResponseDto> getAllOrderPaginated(
            Integer currentUserId,
            Integer page,
            Integer size,
            String sortField,
            String sortDirection
    ){
        if (page == null || page < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page parameter is required and must be non-negative.");
        }
        Pageable pageable = pagingUtil.createPageable(page, size, sortField, sortDirection, "createdOn");
        Specification<Order> spec = Specification.where(OrderSpecifications.belongsToAccount(currentUserId));
        Page<Order> ordersPage = orderRepository.findAll(spec, pageable);
        List<OrderResponseDto<OrderSellerResponseDto>> orderResponse = ordersPage.getContent().stream()
                .map(order -> {
                    List<Seller> sellers = order.getOrderItems().stream()
                            .map(oi -> oi.getSaleItem().getSeller())
                            .distinct()
                            .toList();
                    List<OrderSellerResponseDto> sellerResponses = sellers.stream()
                            .map(this::mapToSellerBase)
                            .toList();
                    List<OrderItemRequestDto> orderItems = mapOrderItems(order.getOrderItems());
                    return mapOrderToResponse(order, sellerResponses, orderItems);
                }).toList();
        OrderPagedResponseDto<OrderSellerResponseDto> response = new OrderPagedResponseDto<>();
        response.setContent(orderResponse);
        response.setPage(ordersPage.getNumber());
        response.setSize(ordersPage.getSize());
        response.setTotalElements(ordersPage.getTotalElements());
        response.setTotalPages(ordersPage.getTotalPages());
        response.setFirst(ordersPage.isFirst());
        response.setLast(ordersPage.isLast());
        response.setSort(ordersPage.getSort().isSorted()
                ? ordersPage.getSort().toString().replace(": ", ":")
                : null);
        return response;
    }

    @Transactional
    public List<OrderSellerViewResponseDto> getAllOrdersForSeller(
                Integer sellerId,
                Integer page,
                Integer size,
                String sortField,
                String sortDirection
        ) {
            if (page == null || page < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page parameter is required and must be non-negative.");
            }
        Pageable pageable = pagingUtil.createPageable(page, size, sortField, sortDirection, "createdOn");
        Specification<Order> spec = Specification.where(OrderSpecifications.belongsToAccount(sellerId));
        Page<Order> ordersPage = orderRepository.findAll(spec, pageable);
        return ordersPage.getContent().stream().map(order -> {
            OrderSellerViewResponseDto dto = new OrderSellerViewResponseDto();
            dto.setId(order.getId());
            dto.setSellerId(sellerId);
            dto.setOrderDate(order.getCreatedOn());
            dto.setPaymentDate(order.getPaymentDate());
            dto.setShippingAddress(order.getShippingAddress());
            dto.setOrderNote(order.getOrderNote());
            dto.setOrderStatus(order.getStatus());
            dto.setOrderItems(mapOrderItems(order.getOrderItems()));

            BuyerInfoDto buyerInfo = new BuyerInfoDto();
            buyerInfo.setId(order.getCustomer().getId());
            buyerInfo.setUsername(order.getCustomer().getNickname());
            dto.setBuyer(buyerInfo);

            return dto;
        }).toList();
    }


}

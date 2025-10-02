package org.example.itbmshopbe.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.itbmshopbe.dtos.OrderDTO.OrderItemRequestDto;
import org.example.itbmshopbe.dtos.OrderDTO.OrderRequestDto;
import org.example.itbmshopbe.dtos.OrderDTO.OrderResponseDto;
import org.example.itbmshopbe.dtos.OrderDTO.OrderSellerResponseDto;
import org.example.itbmshopbe.entities.*;

import org.example.itbmshopbe.repositories.*;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final AccountRepository accountRepository;
    private final SaleItemRepository saleItemRepository;
    private final SellerRepository sellerRepository;
    private final OrderItemRepository orderItemRepository;
    private final ModelMapper modelMapper;

    @Transactional
    public OrderResponseDto createOrder(Integer buyerId,OrderRequestDto orderRequestDto) {
        if (orderRequestDto.getBuyerId() == null ||
                orderRequestDto.getSellerId() == null ||
                orderRequestDto.getOrderItems() == null ||
                orderRequestDto.getOrderItems().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing or invalid request parameters");
        }

        Account buyer = accountRepository.findById(buyerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Buyer not found"));
        Seller seller = sellerRepository.findById(orderRequestDto.getSellerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Seller not found"));
        Order order = new Order();
        order.setCustomer(buyer);
        order.setStatus(orderRequestDto.getOrderStatus() != null ? orderRequestDto.getOrderStatus() : "PENDING");
        order.setOrderNote(orderRequestDto.getOrderNote());
        order.setShippingAddress(orderRequestDto.getShippingAddress());
        Order savedOrder = orderRepository.save(order);
        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderItemRequestDto itemDto : orderRequestDto.getOrderItems()) {
            SaleItem saleItem = saleItemRepository.findById(itemDto.getSaleItemId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "SaleItem not found" + itemDto.getSaleItemId()));
            if(saleItem.getQuantity() < itemDto.getQuantity()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Quantity not enough for item " + saleItem.getId());
            }
            saleItem.setQuantity(saleItem.getQuantity() - itemDto.getQuantity());
            saleItemRepository.save(saleItem);
            OrderItem orderItem = new OrderItem();
            orderItem.setSaleItem(saleItem);
            orderItem.setOrder(savedOrder);
            orderItem.setQuantity(itemDto.getQuantity());
            orderItem.setPriceEach(itemDto.getPrice());
            orderItems.add(orderItem);
        }
        orderItemRepository.saveAll(orderItems);
        OrderResponseDto response = modelMapper.map(order, OrderResponseDto.class);
        OrderSellerResponseDto sellerDto = new OrderSellerResponseDto();
        sellerDto.setId(seller.getId());
        sellerDto.setSellerName(seller.getAccount().getFullname());
        response.setSeller(List.of(sellerDto));
        response.setOrderDate(savedOrder.getCreatedOn().toString());
        response.setBuyerId(buyer.getId());
        List<OrderItemRequestDto> responseItems = orderItems.stream()
                .map(oi -> modelMapper.map(oi, OrderItemRequestDto.class))
                .toList();
        response.setOrderItems(responseItems);
        return response;
    }
}

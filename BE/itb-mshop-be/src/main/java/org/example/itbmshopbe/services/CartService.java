package org.example.itbmshopbe.services;

import lombok.RequiredArgsConstructor;
import org.example.itbmshopbe.dtos.CartDTO.CartRequestDto;
import org.example.itbmshopbe.dtos.CartDTO.CartResponseDto;
import org.example.itbmshopbe.entities.Account;
import org.example.itbmshopbe.entities.Cart;
import org.example.itbmshopbe.entities.OrderItem;
import org.example.itbmshopbe.entities.SaleItem;
import org.example.itbmshopbe.repositories.AccountRepository;
import org.example.itbmshopbe.repositories.CartRepository;
import org.example.itbmshopbe.repositories.OrderItemRepository;
import org.example.itbmshopbe.repositories.SaleItemRepository;
import org.example.itbmshopbe.utils.RepositoryHelper;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final AccountRepository accountRepository;
    private final SaleItemRepository saleItemRepository;
    private final OrderItemRepository orderItemRepository;
    private final ModelMapper modelMapper;
    private final RepositoryHelper repositoryHelper;

    @Transactional
    public CartResponseDto addToCart(Integer userId, CartRequestDto cartRequestDto) {
        Account account = repositoryHelper.findByIdOrThrow(accountRepository, userId, "Account");
        SaleItem saleItem = repositoryHelper.findByIdOrThrow(
                saleItemRepository,
                cartRequestDto.getSaleItemId(),
                "SaleItem"
        );

        // Check if item is deleted
        if (Boolean.TRUE.equals(saleItem.getDeleted())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot add deleted item to cart"
            );
        }

        // Check if item belongs to the user (can't add own items to cart)
        if (saleItem.getSeller().getId().equals(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Cannot add your own items to cart"
            );
        }
        // Check if item already exists in cart
        Cart existingCart = cartRepository.findByAccountIdAndSaleItemId(userId, saleItem.getId());
        if (existingCart != null) {
            // Update quantity
            existingCart.setQuantity(existingCart.getQuantity() + cartRequestDto.getQuantity());
            existingCart.setNote(cartRequestDto.getNote());
            Cart updatedCart = cartRepository.save(existingCart);
            return mapToCartResponse(updatedCart);
        }
        Cart cart = new Cart();
        cart.setAccount(account);
        cart.setSaleItem(saleItem);
        cart.setQuantity(cartRequestDto.getQuantity());
        cart.setNote(cartRequestDto.getNote());

        Cart savedCart = cartRepository.save(cart);
        return mapToCartResponse(savedCart);
    }

    @Transactional
    public List<CartResponseDto> getAllCartItemsByUser(Integer userId) {
        Account account = accountRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Account not found"
                ));

        List<Cart> cartItems = cartRepository.findByAccountId(userId);

        // Auto-remove items that have been ordered OR deleted
        List<Cart> itemsToRemove = cartItems.stream()
                .filter(cart ->
                        orderItemRepository.existsByOrderCustomerIdAndSaleItemId(
                                userId, cart.getSaleItem().getId()) ||
                                Boolean.TRUE.equals(cart.getSaleItem().getDeleted())
                )
                .collect(Collectors.toList());

        if (!itemsToRemove.isEmpty()) {
            cartRepository.deleteAll(itemsToRemove);
            cartItems.removeAll(itemsToRemove);
        }

        return cartItems.stream()
                .map(this::mapToCartResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CartResponseDto getCartById(Integer cartId, Integer userId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Cart item not found"
                ));

        if (!cart.getAccount().getId().equals(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Access denied: Not your cart item"
            );
        }
        if (Boolean.TRUE.equals(cart.getSaleItem().getDeleted())) {
            cartRepository.delete(cart);
            throw new ResponseStatusException(
                    HttpStatus.GONE,
                    "Item has been deleted and removed from cart"
            );
        }

        boolean isAlreadyOrdered = orderItemRepository.existsByOrderCustomerIdAndSaleItemId(
                userId, cart.getSaleItem().getId());

        if (isAlreadyOrdered) {
            cartRepository.delete(cart);
            throw new ResponseStatusException(
                    HttpStatus.GONE,
                    "Item already ordered and removed from cart"
            );
        }

        return mapToCartResponse(cart);
    }

    @Transactional
    public CartResponseDto updateCart(Integer cartId, Integer userId, CartRequestDto cartRequestDto) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Cart item not found"
                ));

        if (!cart.getAccount().getId().equals(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Access denied: Not your cart item"
            );
        }
        if (Boolean.TRUE.equals(cart.getSaleItem().getDeleted())) {
            cartRepository.delete(cart);
            throw new ResponseStatusException(
                    HttpStatus.GONE,
                    "Item has been deleted and removed from cart"
            );
        }

        boolean isAlreadyOrdered = orderItemRepository.existsByOrderCustomerIdAndSaleItemId(
                userId, cart.getSaleItem().getId());

        if (isAlreadyOrdered) {
            cartRepository.delete(cart);
            throw new ResponseStatusException(
                    HttpStatus.GONE,
                    "Item already ordered and removed from cart"
            );
        }

        if (cartRequestDto.getQuantity() != null && cartRequestDto.getQuantity() > 0) {
            cart.setQuantity(cartRequestDto.getQuantity());
        }

        if (cartRequestDto.getNote() != null) {
            cart.setNote(cartRequestDto.getNote());
        }

        Cart updatedCart = cartRepository.save(cart);
        return mapToCartResponse(updatedCart);
    }

    @Transactional
    public void deleteCart(Integer cartId, Integer userId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Cart item not found"
                ));
        if (!cart.getAccount().getId().equals(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Access denied: Not your cart item"
            );
        }
        cartRepository.delete(cart);
    }

    private CartResponseDto mapToCartResponse(Cart cart) {
        CartResponseDto dto = modelMapper.map(cart, CartResponseDto.class);

        SaleItem saleItem = cart.getSaleItem();
        dto.setSaleItemId(saleItem.getId());
        dto.setAccountId(cart.getAccount().getId());

        String brandName = saleItem.getBrand().getName();
        String model = saleItem.getModel();
        String storage = saleItem.getStorageGb() != null ? saleItem.getStorageGb() + "GB" : "-";
        String color = saleItem.getColor() != null ? saleItem.getColor() : "-";
        dto.setItemDescription(brandName + " " + model + " (" + storage + " " + color + ")");

        dto.setPriceEach(saleItem.getPrice());
        dto.setAvailableQuantity(saleItem.getQuantity());
        dto.setSellerName(saleItem.getSeller().getAccount().getNickname());

        return dto;
    }
}

package org.example.itbmshopbe.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.itbmshopbe.dtos.OrderDTO.OrderItemRequestDto;
import org.example.itbmshopbe.dtos.OrderDTO.OrderRequestDto;
import org.example.itbmshopbe.dtos.OrderDTO.OrderResponseDto;
import org.example.itbmshopbe.entities.Account;

import org.example.itbmshopbe.entities.SaleItem;
import org.example.itbmshopbe.entities.Seller;
import org.example.itbmshopbe.repositories.AccountRepository;
import org.example.itbmshopbe.repositories.OrderRepository;
import org.example.itbmshopbe.repositories.SaleItemRepository;
import org.example.itbmshopbe.repositories.SellerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final AccountRepository accountRepository;
    private final SaleItemRepository saleItemRepository;
    private final SellerRepository sellerRepository;

    @Transactional
    public OrderResponseDto createOrder(Integer buyerId,OrderRequestDto orderRequestDto) {
        if (orderRequestDto.getBuyerId() == null || orderRequestDto.getSellerId() == null || orderRequestDto.getOrderItems() == null || orderRequestDto.getOrderItems().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing or invalid request parameters");
        }

        Account buyer = accountRepository.findById(buyerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Buyer not found"));
        Seller seller = sellerRepository.findById(orderRequestDto.getSellerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Seller not found"));

    }
}

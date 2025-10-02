package org.example.itbmshopbe.controllers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.itbmshopbe.dtos.OrderDTO.OrderRequestDto;
import org.example.itbmshopbe.dtos.OrderDTO.OrderResponseDto;
import org.example.itbmshopbe.services.OrderService;
import org.example.itbmshopbe.utils.Util;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v2/orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "${frontend.url}")
public class OrderController {
    private final OrderService orderService;
    @PostMapping
    public ResponseEntity<OrderResponseDto> placeOrder(
            @RequestBody OrderRequestDto orderRequestDto,
            HttpServletRequest request
    ){
        Integer BuyerId = Util.validateAndGetUserId(request, orderRequestDto.getBuyerId());
        OrderResponseDto responseDto = orderService.createOrder(BuyerId, orderRequestDto);
        return ResponseEntity.ok(responseDto);
    }
}

package org.example.itbmshopbe.controllers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.itbmshopbe.dtos.CartDTO.CartRequestDto;
import org.example.itbmshopbe.dtos.CartDTO.CartResponseDto;
import org.example.itbmshopbe.services.CartService;
import org.example.itbmshopbe.utils.Util;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v2/carts")
@RequiredArgsConstructor
@CrossOrigin(origins = "${frontend.url}")
public class CartController {
    private final CartService cartService;

    @PostMapping
    public ResponseEntity<CartResponseDto> addToCart(
            @RequestBody CartRequestDto cartRequestDto,
            HttpServletRequest request
    ) {
        Integer userId = Util.validateAndGetUserId(request, cartRequestDto.getAccountId());
        CartResponseDto responseDto = cartService.addToCart(userId, cartRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @GetMapping
    public ResponseEntity<List<CartResponseDto>> getAllCartItems(
            @RequestParam Integer accountId,
            HttpServletRequest request
    ) {
        Integer userId = Util.validateAndGetUserId(request, accountId);
        List<CartResponseDto> responseDtos = cartService.getAllCartItemsByUser(userId);
        return ResponseEntity.ok(responseDtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CartResponseDto> getCartById(
            @PathVariable Integer id,
            @RequestParam Integer accountId,
            HttpServletRequest request
    ) {
        Integer userId = Util.validateAndGetUserId(request, accountId);
        CartResponseDto responseDto = cartService.getCartById(id, userId);
        return ResponseEntity.ok(responseDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CartResponseDto> updateCart(
            @PathVariable Integer id,
            @RequestBody CartRequestDto cartRequestDto,
            HttpServletRequest request
    ) {
        Integer userId = Util.validateAndGetUserId(request, cartRequestDto.getAccountId());
        CartResponseDto responseDto = cartService.updateCart(id, userId, cartRequestDto);
        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCart(
            @PathVariable Integer id,
            @RequestParam Integer accountId,
            HttpServletRequest request
    ) {
        Integer userId = Util.validateAndGetUserId(request, accountId);
        cartService.deleteCart(id, userId);
        return ResponseEntity.noContent().build();
    }
}

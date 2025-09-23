package org.example.itbmshopbe.controllers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.itbmshopbe.dtos.AccountDTO.UserProfileEditDto;
import org.example.itbmshopbe.dtos.AccountDTO.UserProfileResponseDto;
import org.example.itbmshopbe.dtos.AccountDTO.UserResponseDto;
import org.example.itbmshopbe.repositories.AccountRepository;
import org.example.itbmshopbe.services.AccountService;
import org.example.itbmshopbe.utils.JwtTokenUtil;
import org.example.itbmshopbe.utils.Util;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/v2/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "${frontend.url}")
public class UserController {
    private final AccountService accountService;
    @GetMapping("/{id}")
    public ResponseEntity<UserProfileResponseDto> getUserProfile(
            @PathVariable Integer id,
            HttpServletRequest request
    ) {
        Integer tokenUserId = Util.validateAndGetUserId(request,id);
        UserProfileResponseDto userProfileResponseDto = accountService.getUserProfile(tokenUserId);
        return ResponseEntity.ok(userProfileResponseDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> editUserProfile(
            @PathVariable Integer id,
            @RequestBody UserProfileEditDto  userProfileEditDto,
            HttpServletRequest request
    ){
        Integer tokenUserId = Util.validateAndGetUserId(request, id);
        UserResponseDto userResponseDto = accountService.editAccount(tokenUserId, userProfileEditDto);
        return ResponseEntity.ok(userResponseDto);
    }
}

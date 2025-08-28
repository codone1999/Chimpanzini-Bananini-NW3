package org.example.itbmshopbe.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.itbmshopbe.dtos.AccountDTO.LoginRequestDto;
import org.example.itbmshopbe.dtos.AccountDTO.LoginResponseDto;
import org.example.itbmshopbe.dtos.AccountDTO.RegisterRequestDto;
import org.example.itbmshopbe.dtos.AccountDTO.UserResponseDto;
import org.example.itbmshopbe.entities.Account;
import org.example.itbmshopbe.services.AccountService;
import org.example.itbmshopbe.services.FileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/v2/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "${frontend.url}")
public class AccountController {
    private final AccountService accountService;
    private final FileService fileService;


    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> registerAccount(
            @ModelAttribute RegisterRequestDto registerDto,
            @RequestParam(required = false) MultipartFile nationalCardPhotoFront,
            @RequestParam(required = false) MultipartFile nationalCardPhotoBack
    ) {
        UserResponseDto createdAccount = accountService.registerAccount(registerDto, nationalCardPhotoFront, nationalCardPhotoBack);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAccount);
    }

    @PostMapping("/verify-email")
    public ResponseEntity<UserResponseDto> verifyEmail(@RequestParam String token) {
        UserResponseDto user = accountService.verifyEmail(token);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/authentications")
    public ResponseEntity<LoginResponseDto> Login(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        try {
            LoginResponseDto loginResponse = accountService.loginAccount(loginRequestDto);
            return ResponseEntity.ok(loginResponse);
        }catch (ResponseStatusException e) {
            throw e;
        }catch (Exception e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Login failed", e);
        }
    }
}

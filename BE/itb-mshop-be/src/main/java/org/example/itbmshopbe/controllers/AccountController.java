package org.example.itbmshopbe.controllers;

import lombok.RequiredArgsConstructor;
import org.example.itbmshopbe.dtos.RegisterRequestDto;
import org.example.itbmshopbe.entities.Account;
import org.example.itbmshopbe.services.AccountService;
import org.example.itbmshopbe.services.FileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/v2/account")
@RequiredArgsConstructor
@CrossOrigin(origins = "${frontend.url}")
public class AccountController {
    private final AccountService accountService;
    private final FileService fileService;

    @PostMapping("/register")
    public ResponseEntity<Account> registerAccount(
            @ModelAttribute RegisterRequestDto registerDto,
            @RequestParam(required = false) MultipartFile nationalCardPhotoFront,
            @RequestParam(required = false) MultipartFile nationalCardPhotoBack
    ) {
        try {
            Account createdAccount = accountService.registerAccount(registerDto, nationalCardPhotoFront, nationalCardPhotoBack);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdAccount);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Account registration failed", e);
        }
    }

    @GetMapping("/verify-email")
    public ResponseEntity<Void> verifyEmail(@RequestParam String token) {
        accountService.verifyEmail(token);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}

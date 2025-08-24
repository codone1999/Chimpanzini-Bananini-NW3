package org.example.itbmshopbe.controllers;

import lombok.RequiredArgsConstructor;
import org.example.itbmshopbe.dtos.RegisterRequestDto;
import org.example.itbmshopbe.entities.Account;
import org.example.itbmshopbe.services.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v2/account")
@RequiredArgsConstructor
@CrossOrigin(origins = "${frontend.url}")
public class AccountController {
    private final AccountService accountService;

    @PostMapping("/register")
    public ResponseEntity<Account> registerAccount(@RequestBody RegisterRequestDto registerRequestDto) {
        Account account = accountService.registerAccount(registerRequestDto);
        return ResponseEntity.status(201).body(account);
    }

    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        String massage = accountService.verifyEmail(token);
        return ResponseEntity.ok(massage);
    }
}

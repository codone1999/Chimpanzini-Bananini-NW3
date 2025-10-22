package org.example.itbmshopbe.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.itbmshopbe.dtos.AccountDTO.LoginRequestDto;
import org.example.itbmshopbe.dtos.AccountDTO.LoginResponseDto;
import org.example.itbmshopbe.dtos.AccountDTO.RegisterRequestDto;
import org.example.itbmshopbe.dtos.AccountDTO.ResetPassword.ForgotPasswordRequestDto;
import org.example.itbmshopbe.dtos.AccountDTO.ResetPassword.ResetPasswordRequestDto;
import org.example.itbmshopbe.dtos.AccountDTO.ResetPassword.VerifyResetCodeRequestDto;
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
@RequestMapping("/v2/auth")
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
        UserResponseDto createdAccount = accountService.registerAccount(
                registerDto,
                nationalCardPhotoFront,
                nationalCardPhotoBack
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAccount);
    }

    @PostMapping("/verify-email")
    public ResponseEntity<UserResponseDto> verifyEmail(@RequestParam String token) {
        UserResponseDto user = accountService.verifyEmail(token);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> Login(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        LoginResponseDto loginResponse = accountService.loginAccount(loginRequestDto);
        return ResponseEntity.ok(loginResponse);
    }


    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(value = "refresh_token", required = false) String refreshToken
    ) {
        accountService.logout(refreshToken);
        return ResponseEntity.noContent()
                .header("Set-Cookie",
                        "refresh_token=; Max-Age=0; Path=/; HttpOnly; Secure; SameSite=Strict")
                .build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDto> refreshToken(
            @CookieValue(value = "refresh_token", required = false) String refreshToken
    ) {
        String newAccessToken = accountService.refreshAccessToken(refreshToken);
        return ResponseEntity.ok(new LoginResponseDto(newAccessToken, refreshToken));
    }
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequestDto forgotPasswordDto
    ) {
        accountService.forgotPassword(forgotPasswordDto.getEmail());
        return ResponseEntity.ok("A password reset code has been sent to your email.");
    }

    @PostMapping("/verify-reset-code")
    public ResponseEntity<String> verifyResetCode(
            @Valid @RequestBody VerifyResetCodeRequestDto verifyCodeDto
    ) {
        accountService.verifyResetCode(verifyCodeDto);
        return ResponseEntity.ok("Reset code verified successfully. You can now reset your password.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
            @Valid @RequestBody ResetPasswordRequestDto resetPasswordDto
    ) {
        accountService.resetPassword(resetPasswordDto);
        return ResponseEntity.ok("Your password has been successfully reset.");
    }
}

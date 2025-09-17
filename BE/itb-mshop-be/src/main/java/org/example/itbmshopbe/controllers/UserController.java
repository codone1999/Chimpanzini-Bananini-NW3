package org.example.itbmshopbe.controllers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.itbmshopbe.dtos.AccountDTO.UserProfileResponseDto;
import org.example.itbmshopbe.repositories.AccountRepository;
import org.example.itbmshopbe.services.AccountService;
import org.example.itbmshopbe.utils.JwtTokenUtil;
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
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Token");
        }
        String token = authHeader.substring(7);
        try{
            Integer tokenUserId = JwtTokenUtil.getIdFromToken(token);
            if(!id.equals(tokenUserId)){
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Request user id not matched with id in access token");
            }
            UserProfileResponseDto userProfile =  accountService.getUserProfile(id);
            return ResponseEntity.ok(userProfile);
        } catch (Exception e) {
            if (e instanceof ResponseStatusException) {
                throw e;
            }
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Token");
        }
    }
}

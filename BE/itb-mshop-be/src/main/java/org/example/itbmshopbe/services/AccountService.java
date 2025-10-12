package org.example.itbmshopbe.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.example.itbmshopbe.dtos.AccountDTO.*;
import org.example.itbmshopbe.dtos.AccountDTO.ResetPassword.ResetPasswordRequestDto;
import org.example.itbmshopbe.entities.Account;
import org.example.itbmshopbe.entities.EmailVerificationToken;
import org.example.itbmshopbe.entities.PasswordResetToken;
import org.example.itbmshopbe.entities.Seller;
import org.example.itbmshopbe.repositories.AccountRepository;
import org.example.itbmshopbe.repositories.EmailVerificationTokenRepository;
import org.example.itbmshopbe.repositories.PasswordResetTokenRepository;
import org.example.itbmshopbe.repositories.SellerRepository;
import org.example.itbmshopbe.utils.JwtTokenUtil;
import org.example.itbmshopbe.utils.Util;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final SellerRepository sellerRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileService fileService;
    private final EmailVerificationTokenRepository tokenRepository;
    private final EmailService emailService;
    private final ModelMapper modelMapper;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    public UserResponseDto registerAccount(RegisterRequestDto accountReq,
                                           MultipartFile frontPhoto,
                                           MultipartFile backPhoto) {
        if (accountRepository.existsByEmail(accountReq.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use!");
        }

        Account newAccount = new Account();
        newAccount.setNickname(accountReq.getNickName());
        newAccount.setFullname(accountReq.getFullName());
        newAccount.setEmail(accountReq.getEmail());
        newAccount.setPassword(passwordEncoder.encode(accountReq.getPassword()));
        newAccount.setRole(accountReq.getRole());
        newAccount.setStatus(Account.Status.INACTIVE);

        Account savedAccount = accountRepository.save(newAccount);

        if ("SELLER".equalsIgnoreCase(accountReq.getRole())) {
            Seller seller = new Seller();
            seller.setAccount(savedAccount);
            seller.setMobile(accountReq.getMobile());
            seller.setBankAccountNo(accountReq.getBankAccountNo());
            seller.setBankName(accountReq.getBankName());
            seller.setNationalCardNo(accountReq.getNationalCardNo());
            try {
                if (frontPhoto != null && !frontPhoto.isEmpty()) {
                    String frontFileName = fileService.storeFile(frontPhoto, savedAccount.getId(), 1, true);
                    seller.setNationalCardPhotoFront(frontFileName);
                }
                if (backPhoto != null && !backPhoto.isEmpty()) {
                    String backFileName = fileService.storeFile(backPhoto, savedAccount.getId(), 2, true);
                    seller.setNationalCardPhotoBack(backFileName);
                }
            } catch (Exception e) {
                throw new RuntimeException("Error saving seller card photos", e);
            }
            sellerRepository.save(seller);
        }

        // Generate token + send email
        String token = JwtTokenUtil.generateToken(savedAccount.getEmail());
        EmailVerificationToken verificationToken = new EmailVerificationToken();
        verificationToken.setAccount(savedAccount);
        verificationToken.setToken(token);
        verificationToken.setExpiryDate(Instant.now().plusSeconds(60 * 60));
        tokenRepository.save(verificationToken);

        String verificationUrl = "https://intproj24.sit.kmutt.ac.th/nw3/verify-email?token=" + token;
        String verificationHtml =
                "<body style='margin: 0; padding: 0; background-color: #f5f5f5; font-family: -apple-system, BlinkMacSystemFont, \"Segoe UI\", Arial, sans-serif;'>" +
                        "  <div style='max-width: 500px; margin: 40px auto; background-color: #ffffff; border-radius: 4px; overflow: hidden; box-shadow: 0 2px 8px rgba(81, 0, 122, 0.1);'>" +
                        "    <div style='background-color: #51007a; padding: 32px 24px; text-align: center;'>" +
                        "      <h1 style='margin: 0; color: #ffffff; font-size: 24px; font-weight: 600;'>Verify Your Account</h1>" +
                        "    </div>" +
                        "    <div style='padding: 32px 24px;'>" +
                        "      <p style='margin: 0 0 24px; color: #333333; font-size: 15px; line-height: 1.6;'>Welcome to ITBMS-Shop! Click the button below to verify your account and get started.</p>" +
                        "      <div style='text-align: center; margin: 32px 0;'>" +
                        "        <a href='" + verificationUrl + "' style='display: inline-block; background-color: #51007a; color: #ffffff; text-decoration: none; padding: 14px 32px; border-radius: 4px; font-size: 15px; font-weight: 500;'>Verify Account</a>" +
                        "      </div>" +
                        "      <p style='margin: 24px 0 0; color: #666666; font-size: 13px; line-height: 1.6;'>If the button doesn't work, copy and paste this link:</p>" +
                        "      <p style='margin: 8px 0 0; color: #51007a; font-size: 13px; word-break: break-all;'>" + verificationUrl + "</p>" +
                        "    </div>" +
                        "    <div style='background-color: #f9f9f9; padding: 16px 24px; border-top: 1px solid #eeeeee;'>" +
                        "      <p style='margin: 0; color: #999999; font-size: 12px; text-align: center;'>This link will expire in 1 hour</p>" +
                        "    </div>" +
                        "  </div>" +
                        "</body>";
        try {
            emailService.sendEmail(savedAccount.getEmail(),
                    "Verify your account",
                    verificationHtml);
        } catch (Exception e) {
            System.out.println("Failed to send verification email: " + e.getMessage());
        }

        return mapToUserResponseDto(savedAccount);
    }
    public UserResponseDto editAccount(Integer UserId,UserProfileEditDto userProfileEditDto) {
        Account existAccount = accountRepository.findById(UserId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found for id :: " + UserId)
        );
        existAccount.setFullname(userProfileEditDto.getFullName());
        existAccount.setNickname(userProfileEditDto.getNickName());
        Account updatedAccount = accountRepository.save(existAccount);
        return mapToUserResponseDto(updatedAccount);
    }

    public UserResponseDto verifyEmail(String token) {
        EmailVerificationToken verificationToken = tokenRepository.findByToken(token);
        if (verificationToken == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid verification token");
        }
        if (verificationToken.getExpiryDate().isBefore(Instant.now()) || JwtTokenUtil.isTokenExpired(token)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Verification token expired. Please request a new one.");
        }

        Account account = verificationToken.getAccount();
        account.setStatus(Account.Status.ACTIVE);
        Account updated = accountRepository.save(account);

        tokenRepository.delete(verificationToken);

        return mapToUserResponseDto(updated);
    }

    private UserResponseDto mapToUserResponseDto(Account account) {
        UserResponseDto dto = modelMapper.map(account, UserResponseDto.class);
        Optional<Seller> sellerOpt = sellerRepository.findById(account.getId());
        dto.setPhoneNumber(sellerOpt.map(Seller::getMobile).orElse(null));

        return dto;
    }
    public LoginResponseDto loginAccount(LoginRequestDto loginRequestDto) {
        Optional<Account> accountOpt = accountRepository.findByEmail(loginRequestDto.getEmail());
        if (accountOpt.isPresent()) {
            Account account = accountOpt.get();
            if(passwordEncoder.matches(loginRequestDto.getPassword(), account.getPassword())) {
                if (account.getStatus() != Account.Status.ACTIVE) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is not activated. Please verify your email first.");
                }

                String accessToken = JwtTokenUtil.generateAccessToken(
                        account.getId(),
                        account.getEmail(),
                        account.getNickname(),
                        account.getRole(),
                        account.getStatus().name()
                );
                String refreshToken = JwtTokenUtil.generateRefreshToken(
                        account.getId(),
                        account.getEmail(),
                        account.getNickname(),
                        account.getRole(),
                        account.getStatus().name()
                );
                return new LoginResponseDto(accessToken, refreshToken);
            }
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email or Password is incorrect.");
    }
    public void logout(String refreshToken) {
        try {
            JwtTokenUtil.validateToken(refreshToken);
        } catch (JwtException | IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid token");
        }
        Integer userId = JwtTokenUtil.getIdFromToken(refreshToken);
        Optional<Account> accountOpt = accountRepository.findById(userId);
        if(accountOpt.isEmpty()){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "user not found");
        }
        Account account = accountOpt.get();
        if (account.getStatus() != Account.Status.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not active");
        }
    }
    public String refreshAccessToken(String refreshToken) {
        try {
            JwtTokenUtil.validateToken(refreshToken);
        } catch (JwtException | IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid token");
        }
        Integer userId = JwtTokenUtil.getIdFromToken(refreshToken);
        Account account = accountRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "user not found"));
        if (account.getStatus() != Account.Status.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not active");
        }
        return JwtTokenUtil.generateAccessToken(
                account.getId(),
                account.getEmail(),
                account.getNickname(),
                account.getRole(),
                account.getStatus().name()
        );
    }
    public UserProfileResponseDto getUserProfile(Integer userId) {
        Optional<Account> accountOpt = accountRepository.findById(userId);
        if (accountOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "user not found");
        }
        Account account = accountOpt.get();
        if(account.getStatus() != Account.Status.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not active");
        }
        UserProfileResponseDto responseDto = modelMapper.map(account, UserProfileResponseDto.class);
        if("SELLER".equalsIgnoreCase(account.getRole())) {
            Optional<Seller> sellerOpt = sellerRepository.findById(account.getId());
            if(sellerOpt.isPresent()) {
                Seller seller = sellerOpt.get();
                responseDto.setPhoneNumber(seller.getMobile());
                responseDto.setBankName(seller.getBankName());
                responseDto.setBankAccount(seller.getBankAccountNo());
            }
        }
        return responseDto;
    }
    public void forgotPassword(String email) {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account with this email does not exist."));
        String code = String.format("%04d", new Random().nextInt(10000));
        passwordResetTokenRepository.findByAccount(account).ifPresent(passwordResetTokenRepository::delete);
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(code);
        resetToken.setAccount(account);
        resetToken.setExpiryDate(Instant.now().plusSeconds(60 * 10)); // Token expires in 10 minutes
        passwordResetTokenRepository.save(resetToken);
        String resetPasswordHtml =
                "<body style='margin: 0; padding: 0; background-color: #f5f5f5; font-family: -apple-system, BlinkMacSystemFont, \"Segoe UI\", Arial, sans-serif;'>" +
                        "  <div style='max-width: 500px; margin: 40px auto; background-color: #ffffff; border-radius: 4px; overflow: hidden; box-shadow: 0 2px 8px rgba(81, 0, 122, 0.1);'>" +
                        "    <div style='background-color: #51007a; padding: 32px 24px; text-align: center;'>" +
                        "      <h1 style='margin: 0; color: #ffffff; font-size: 24px; font-weight: 600;'>Password Reset</h1>" +
                        "    </div>" +
                        "    <div style='padding: 32px 24px;'>" +
                        "      <p style='margin: 0 0 8px; color: #333333; font-size: 15px;'>Hello " + account.getNickname() + ",</p>" +
                        "      <p style='margin: 0 0 24px; color: #666666; font-size: 15px; line-height: 1.6;'>Use the code below to reset your password:</p>" +
                        "      <div style='text-align: center; margin: 32px 0;'>" +
                        "        <div style='display: inline-block; background-color: #51007a; padding: 20px 40px; border-radius: 4px;'>" +
                        "          <p style='margin: 0; font-size: 32px; font-weight: 700; letter-spacing: 8px; color: #ffffff;'>" + code + "</p>" +
                        "        </div>" +
                        "      </div>" +
                        "      <p style='margin: 24px 0 0; color: #666666; font-size: 13px; line-height: 1.6; text-align: center;'>This code will expire in <strong style='color: #51007a;'>10 minutes</strong></p>" +
                        "    </div>" +
                        "    <div style='background-color: #f9f9f9; padding: 16px 24px; border-top: 1px solid #eeeeee;'>" +
                        "      <p style='margin: 0; color: #999999; font-size: 12px; text-align: center;'>If you didn't request this, please ignore this email</p>" +
                        "    </div>" +
                        "  </div>" +
                        "</body>";
        emailService.sendEmail(account.getEmail(), "Your Password Reset Code", resetPasswordHtml);
    }

    public void resetPassword(ResetPasswordRequestDto resetRequest) {
        Account account = accountRepository.findByEmail(resetRequest.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account with this email does not exist."));

        PasswordResetToken token = passwordResetTokenRepository.findByAccount(account)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No password reset request was found. Please request a new code."));
        if (token.getExpiryDate().isBefore(Instant.now())) {
            passwordResetTokenRepository.delete(token);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Your reset code has expired. Please request a new one.");
        }

        if (!token.getToken().equals(resetRequest.getCode())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The provided code is incorrect.");
        }
        account.setPassword(passwordEncoder.encode(resetRequest.getNewPassword()));
        accountRepository.save(account);
        passwordResetTokenRepository.delete(token);
    }

}

package org.example.itbmshopbe.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.example.itbmshopbe.dtos.AccountDTO.*;
import org.example.itbmshopbe.dtos.AccountDTO.ResetPassword.ResetPasswordRequestDto;
import org.example.itbmshopbe.dtos.AccountDTO.ResetPassword.VerifyResetCodeRequestDto;
import org.example.itbmshopbe.entities.Account;
import org.example.itbmshopbe.entities.EmailVerificationToken;
import org.example.itbmshopbe.entities.PasswordResetToken;
import org.example.itbmshopbe.entities.Seller;
import org.example.itbmshopbe.repositories.AccountRepository;
import org.example.itbmshopbe.repositories.EmailVerificationTokenRepository;
import org.example.itbmshopbe.repositories.PasswordResetTokenRepository;
import org.example.itbmshopbe.repositories.SellerRepository;
import org.example.itbmshopbe.utils.EntityValidatorUtil;
import org.example.itbmshopbe.utils.JwtTokenUtil;
import org.example.itbmshopbe.utils.Util;
import org.example.itbmshopbe.utils.ValidationUtil;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
    private final ValidationUtil validationUtil;
    private final EntityValidatorUtil entityValidatorUtil;

    public UserResponseDto registerAccount(RegisterRequestDto accountReq,
                                           MultipartFile frontPhoto,
                                           MultipartFile backPhoto) {
        try {
        validateRole(accountReq.getRole());
        checkEmailNotInUse(accountReq.getEmail());

        if ("SELLER".equalsIgnoreCase(accountReq.getRole())) {
            validateSellerFields(accountReq);
        }

        Account newAccount = createAccount(accountReq);
        Account savedAccount = accountRepository.save(newAccount);

        if ("SELLER".equalsIgnoreCase(accountReq.getRole())) {
            createSeller(savedAccount, accountReq, frontPhoto, backPhoto);
        }
        sendVerificationEmail(savedAccount);

        return mapToUserResponseDto(savedAccount);
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Account registration failed"
            );
        }
    }

    private void validateRole(String role) {
        if (!"SELLER".equalsIgnoreCase(role) && !"BUYER".equalsIgnoreCase(role)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid user role. Must be 'SELLER' or 'CUSTOMER'"
            );
        }
    }
    private void checkEmailNotInUse(String email) {
        if (accountRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
        }
    }
    private void validateSellerFields(RegisterRequestDto accountReq) {
        validationUtil.validateRequired(accountReq.getMobile(), "Mobile number");
        validationUtil.validateRequired(accountReq.getNationalCardNo(), "National ID number");
        validationUtil.validateRequired(accountReq.getBankAccountNo(), "Bank account number");

        validationUtil.validateMobile(accountReq.getMobile());
        validationUtil.validateNationalId(accountReq.getNationalCardNo());
        validationUtil.validateBankAccount(accountReq.getBankAccountNo());

        if (sellerRepository.existsByMobile(accountReq.getMobile())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This mobile number is already in use"
            );
        }

        if (sellerRepository.existsByNationalCardNo(accountReq.getNationalCardNo())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This National ID number is already in use"
            );
        }
    }

    private void createSeller(Account account, RegisterRequestDto dto,
                              MultipartFile frontPhoto, MultipartFile backPhoto) {
        Seller seller = new Seller();
        seller.setAccount(account);
        seller.setMobile(dto.getMobile());
        seller.setBankAccountNo(dto.getBankAccountNo());
        seller.setBankName(dto.getBankName());
        seller.setNationalCardNo(dto.getNationalCardNo());

        try {
            if (frontPhoto != null && !frontPhoto.isEmpty()) {
                String frontFileName = fileService.storeFile(frontPhoto, account.getId(), 1, true);
                seller.setNationalCardPhotoFront(frontFileName);
            }
            if (backPhoto != null && !backPhoto.isEmpty()) {
                String backFileName = fileService.storeFile(backPhoto, account.getId(), 2, true);
                seller.setNationalCardPhotoBack(backFileName);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error saving seller card photos", e);
        }

        sellerRepository.save(seller);
    }

    private Account createAccount(RegisterRequestDto accountReq) {
        Account account = new Account();
        account.setNickname(accountReq.getNickName());
        account.setFullname(accountReq.getFullName());
        account.setEmail(accountReq.getEmail());
        account.setPassword(passwordEncoder.encode(accountReq.getPassword()));
        account.setRole(accountReq.getRole());
        account.setStatus(Account.Status.INACTIVE);
        return account;
    }

    private void sendVerificationEmail(Account account) {
        String token = JwtTokenUtil.generateToken(account.getEmail());

        EmailVerificationToken verificationToken = new EmailVerificationToken();
        verificationToken.setAccount(account);
        verificationToken.setToken(token);
        verificationToken.setExpiryDate(Instant.now().plusSeconds(60 * 60));
        tokenRepository.save(verificationToken);

        String verificationUrl = "https://intproj24.sit.kmutt.ac.th/nw3/verify-email?token=" + token;
        String emailHtml = buildVerificationEmailHtml(verificationUrl);

        try {
            emailService.sendEmail(account.getEmail(), "Verify your account", emailHtml);
        } catch (Exception e) {
            System.out.println("Failed to send verification email: " + e.getMessage());
        }
    }

    private String buildVerificationEmailHtml(String verificationUrl) {
        return "<!DOCTYPE html>" +
                "<html lang='en'>" +
                "<head>" +
                "  <meta charset='UTF-8'>" +
                "  <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "  <title>Verify Your Email Address</title>" +
                "</head>" +
                "<body style='margin: 0; padding: 0; width: 100%; background-color: #f4f4f4; font-family: -apple-system, BlinkMacSystemFont, \"Segoe UI\", Roboto, Arial, sans-serif;'>" +
                "  <div style='max-width: 600px; margin: 40px auto; background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 15px rgba(0,0,0,0.05);'>" +
                "    <div style='padding: 40px; text-align: center;'>" +
                "      <h1 style='margin-top: 0; margin-bottom: 20px; color: #51007a; font-size: 24px; font-weight: 600;'>Verify Your Email Address</h1>" +
                "      <p style='margin-bottom: 30px; color: #555555; font-size: 16px; line-height: 1.5;'>Please click the button below to confirm your email address and activate your account.</p>" +
                "      <a href='" + verificationUrl + "' style='display: inline-block; padding: 12px 24px; background-color: #51007a; color: #ffffff; text-decoration: none; font-size: 16px; font-weight: 500; border-radius: 6px;'>Verify Account</a>" +
                "      <p style='margin-top: 30px; font-size: 12px; color: #999999;'>If the button above doesn't work, copy and paste this link into your browser:</p>" +
                "      <p style='margin-top: 10px; font-size: 12px; color: #777777; word-break: break-all;'>" + verificationUrl + "</p>" +
                "    </div>" +
                "    <div style='background-color: #fafafa; padding: 20px; border-top: 1px solid #eeeeee;'>" +
                "      <p style='margin: 0; color: #999999; font-size: 12px; text-align: center;'>If you did not request this email, you can safely ignore it.</p>" +
                "    </div>" +
                "  </div>" +
                "</body>" +
                "</html>";
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
        try {
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
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Email verification failed",
                    e
            );
        }
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
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No refresh token provided"
            );
        }
        try {
            JwtTokenUtil.validateToken(refreshToken);
        } catch (JwtException | IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid token"
            );
        }
        Integer userId = JwtTokenUtil.getIdFromToken(refreshToken);
        Account account = entityValidatorUtil.validateActiveAccount(userId);

        if (account.getStatus() != Account.Status.ACTIVE) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "User is not active"
            );
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

    @Transactional
    public void forgotPassword(String email) {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account with this email does not exist."));
        passwordResetTokenRepository.deleteByAccountAndUsedFalse(account);
        String resetCode = String.format("%04d", new Random().nextInt(10000));

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(resetCode);
        resetToken.setAccount(account);
        resetToken.setExpiryDate(Instant.now().plus(2, ChronoUnit.MINUTES));//2min
        resetToken.setVerified(false);
        resetToken.setUsed(false);
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
                        "          <p style='margin: 0; font-size: 32px; font-weight: 700; letter-spacing: 8px; color: #ffffff;'>" + resetCode + "</p>" +
                        "        </div>" +
                        "      </div>" +
                        "      <p style='margin: 24px 0 0; color: #666666; font-size: 13px; line-height: 1.6; text-align: center;'>This code will expire in <strong style='color: #51007a;'>2 minutes</strong></p>" +
                        "    </div>" +
                        "    <div style='background-color: #f9f9f9; padding: 16px 24px; border-top: 1px solid #eeeeee;'>" +
                        "      <p style='margin: 0; color: #999999; font-size: 12px; text-align: center;'>If you didn't request this, please ignore this email</p>" +
                        "    </div>" +
                        "  </div>" +
                        "</body>";
        emailService.sendEmail(account.getEmail(), "Your Password Reset Code", resetPasswordHtml);
    }

    @Transactional
    public void verifyResetCode(VerifyResetCodeRequestDto verifyCodeDto) {
        PasswordResetToken resetToken = passwordResetTokenRepository
                .findByTokenAndAccountEmail(verifyCodeDto.getCode(), verifyCodeDto.getEmail())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid reset code"
                ));
        if (resetToken.getExpiryDate().isBefore(Instant.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Reset code has expired. Please request a new one."
            );
        }
        if (Boolean.TRUE.equals(resetToken.getUsed())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "This reset code has already been used. Please request a new one."
            );
        }
        resetToken.setVerified(true);
        passwordResetTokenRepository.save(resetToken);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequestDto resetPasswordDto) {
        PasswordResetToken resetToken = passwordResetTokenRepository
                .findTopByAccountEmailAndVerifiedTrueAndUsedFalseOrderByExpiryDateDesc(
                        resetPasswordDto.getEmail()
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "No valid reset code found. Please verify your code first."
                ));

        if (resetToken.getExpiryDate().isBefore(Instant.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Reset code has expired. Please request a new one."
            );
        }
        Account account = resetToken.getAccount();
        account.setPassword(passwordEncoder.encode(resetPasswordDto.getNewPassword()));
        accountRepository.save(account);
        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
    }
}

package org.example.itbmshopbe.services;

import lombok.RequiredArgsConstructor;
import org.example.itbmshopbe.dtos.AccountDTO.LoginRequestDto;
import org.example.itbmshopbe.dtos.AccountDTO.LoginResponseDto;
import org.example.itbmshopbe.dtos.AccountDTO.RegisterRequestDto;
import org.example.itbmshopbe.dtos.AccountDTO.UserResponseDto;
import org.example.itbmshopbe.entities.Account;
import org.example.itbmshopbe.entities.EmailVerificationToken;
import org.example.itbmshopbe.entities.Seller;
import org.example.itbmshopbe.repositories.AccountRepository;
import org.example.itbmshopbe.repositories.EmailVerificationTokenRepository;
import org.example.itbmshopbe.repositories.SellerRepository;
import org.example.itbmshopbe.utils.JwtTokenUtil;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Optional;

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
    public UserResponseDto registerAccount(RegisterRequestDto accountReq,
                                           MultipartFile frontPhoto,
                                           MultipartFile backPhoto) {
        if (accountRepository.existsByEmail(accountReq.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use!");
        }

        // Create account
        Account newAccount = new Account();
        newAccount.setNickname(accountReq.getNickName());
        newAccount.setFullname(accountReq.getFullName());
        newAccount.setEmail(accountReq.getEmail());
        newAccount.setPassword(passwordEncoder.encode(accountReq.getPassword()));
        newAccount.setRole(accountReq.getRole());
        newAccount.setStatus(Account.Status.INACTIVE);

        Account savedAccount = accountRepository.save(newAccount);

        // If seller, save seller info...
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

        String verificationUrl = "http://intproj24.sit.kmutt.ac.th/nw3/verify-email?token=" + token;
        try {
            emailService.sendEmail(savedAccount.getEmail(),
                    "Verify your account",
                    verificationUrl);
        } catch (Exception e) {
            System.out.println("Failed to send verification email: " + e.getMessage());
        }

        return mapToUserResponseDto(savedAccount);
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

}

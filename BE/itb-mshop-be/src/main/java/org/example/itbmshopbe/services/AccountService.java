package org.example.itbmshopbe.services;

import lombok.RequiredArgsConstructor;
import org.example.itbmshopbe.dtos.RegisterRequestDto;
import org.example.itbmshopbe.entities.Account;
import org.example.itbmshopbe.entities.EmailVerificationToken;
import org.example.itbmshopbe.entities.Seller;
import org.example.itbmshopbe.repositories.AccountRepository;
import org.example.itbmshopbe.repositories.EmailVerificationTokenRepository;
import org.example.itbmshopbe.repositories.SellerRepository;
import org.example.itbmshopbe.utils.JwtTokenUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final SellerRepository sellerRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileService fileService;
    private final EmailVerificationTokenRepository tokenRepository;

    public Account registerAccount(RegisterRequestDto accountReq,
                                   MultipartFile frontPhoto,
                                   MultipartFile backPhoto) {
        if (accountRepository.existsByEmail(accountReq.getEmail())) {
            throw new RuntimeException("Email already in use!");
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

        // If seller, create seller entity
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

        String token= JwtTokenUtil.generateToken(savedAccount.getEmail());
        EmailVerificationToken verificationToken = new EmailVerificationToken();
        verificationToken.setAccount(savedAccount);
        verificationToken.setToken(token);
        verificationToken.setExpiryDate(Instant.now().plusSeconds(15*60));
        tokenRepository.save(verificationToken);

        String verificationUrl = "http://intproj24.sit.kmutt.ac.th/nw3/verify-email/?token=" + token;
        //emailService.sendEmail(savedAccount.getEmail(),
        //        "verify your account",
        //        "Please click the link to verify your account: " + verificationUrl
        //        );
        try {
            emailService.sendEmail(savedAccount.getEmail(),
                    "verify your account",
                    "Please click the link to verify your account: " + verificationUrl
            );
        } catch (Exception e) {
            System.out.println("Failed to send verification email: " + e.getMessage());
            e.printStackTrace(); // print full stack trace
        }

        return savedAccount;
    }

    public String verifyEmail(String token){
        EmailVerificationToken verificationToken = tokenRepository.findByToken(token);
        if (verificationToken.getExpiryDate().isBefore(Instant.now()) || JwtTokenUtil.isTokenExpired(token)) {
            throw new RuntimeException("Verification token expired. Please request a new one.");
        }

        Account account = verificationToken.getAccount();
        account.setStatus(Account.Status.ACTIVE);
        accountRepository.save(account);

        tokenRepository.delete(verificationToken); // optional: remove token after success
        return "Your account has been successfully activated.";
    }
}

package org.example.itbmshopbe.services;

import lombok.RequiredArgsConstructor;
import org.example.itbmshopbe.dtos.RegisterRequestDto;
import org.example.itbmshopbe.entities.Account;
import org.example.itbmshopbe.entities.Seller;
import org.example.itbmshopbe.repositories.AccountRepository;
import org.example.itbmshopbe.repositories.EmailVerificationTokenRepository;
import org.example.itbmshopbe.repositories.SellerRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final SellerRepository sellerRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileService fileService;

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

        return savedAccount;
    }
}

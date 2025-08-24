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

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final SellerRepository sellerRepository;
    private final PasswordEncoder passwordEncoder;

    public Account registerAccount(RegisterRequestDto accountReq){
        if(accountRepository.existsByEmail(accountReq.getEmail())){
            throw new RuntimeException("Email already in use!");
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
            seller.setNationalCardPhoto(accountReq.getNationalCardPhoto());
            sellerRepository.save(seller);
        }

        return savedAccount;
    }

}

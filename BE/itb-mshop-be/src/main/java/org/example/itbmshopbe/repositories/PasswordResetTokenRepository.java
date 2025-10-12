package org.example.itbmshopbe.repositories;

import org.example.itbmshopbe.entities.Account;
import org.example.itbmshopbe.entities.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Integer> {
    Optional<PasswordResetToken> findByAccount(Account account);
}

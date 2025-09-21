package org.example.itbmshopbe.repositories;

import org.example.itbmshopbe.entities.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {
    boolean existsByEmail(String email);

    Optional<Account> findByEmail(String email);

    boolean findAccountsById(Integer id);
}

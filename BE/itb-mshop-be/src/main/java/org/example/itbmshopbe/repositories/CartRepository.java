package org.example.itbmshopbe.repositories;

import org.example.itbmshopbe.entities.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Integer> {
    List<Cart> findByAccountId(Integer accountId);
    Cart findByAccountIdAndSaleItemId(Integer accountId, Integer saleItemId);
}

package org.example.itbmshopbe.repositories;

import org.example.itbmshopbe.entities.Seller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SellerRepository extends JpaRepository<Seller, Integer> {
    boolean existsByMobile(String mobile);
    boolean existsByNationalCardNo(String nationalCardNo);
}

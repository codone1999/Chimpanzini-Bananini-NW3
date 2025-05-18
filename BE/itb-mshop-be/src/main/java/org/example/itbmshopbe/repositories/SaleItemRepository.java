package org.example.itbmshopbe.repositories;

import org.example.itbmshopbe.entities.SaleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SaleItemRepository extends JpaRepository<SaleItem, Integer> {

    @Query("SELECT COUNT(s) FROM SaleItem s WHERE s.brand.id = :brandId")
    Integer countByBrandId(Integer brandId);

    boolean existsByBrandId(Integer id);
}

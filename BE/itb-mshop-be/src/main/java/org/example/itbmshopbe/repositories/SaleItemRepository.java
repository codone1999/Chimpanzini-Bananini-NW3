package org.example.itbmshopbe.repositories;

import org.example.itbmshopbe.entities.SaleItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SaleItemRepository extends JpaRepository<SaleItem, Integer> {

    @Query("SELECT COUNT(s) FROM SaleItem s WHERE s.brand.id = :brandId")
    Integer countByBrandId(Integer brandId);

    boolean existsByBrandId(Integer id);

    Page<SaleItem> findByBrand_NameIgnoreCaseIn(List<String> brandNames, Pageable pageable);
}

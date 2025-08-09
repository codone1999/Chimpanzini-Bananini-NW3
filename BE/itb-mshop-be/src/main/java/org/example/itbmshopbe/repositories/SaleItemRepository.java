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

    @Query("""
    select s FROM SaleItem s 
    WHERE (:brandIds IS NULL OR s.brand.id IN :brandIds)
                  AND (:minPrice IS NULL OR s.price >= :minPrice)
                  AND (:maxPrice IS NULL OR s.price <= :maxPrice)
                  AND (:storageSizes IS NULL OR s.storageGb IN :storageSizes)"""
    )
    List<SaleItem> filterSaleItem(
            @Param("brandIds") List<Integer> brandIds,
            @Param("minPrice") Integer minPrice,
            @Param("maxPrice") Integer maxPrice,
            @Param("storageSizes") List<Integer> storageSizes
    );

    @Query("SELECT DISTINCT s FROM SaleItem s ORDER BY s.storageGb ASC")
    List<SaleItem> findDistinctStorageGb();

    @Query("""
    SELECT s FROM SaleItem s 
    WHERE s.id IN (SELECT MIN(s2.id) FROM SaleItem s2 GROUP BY s2.brand.id)
    ORDER BY s.brand.name ASC
    """)
    List<SaleItem> findDistinctBrandNames();

}

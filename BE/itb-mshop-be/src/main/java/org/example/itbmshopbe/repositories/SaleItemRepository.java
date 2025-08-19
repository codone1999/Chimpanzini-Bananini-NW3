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
        SELECT s FROM SaleItem s
        WHERE (:brandNames IS NULL OR LOWER(s.brand.name) IN :brandNames)
        AND (:minPrice IS NULL OR s.price >= :minPrice)
        AND (:maxPrice IS NULL OR s.price <= :maxPrice)
        AND (
                (:storageSizes IS NULL AND (:filterNullStorage IS NULL OR :filterNullStorage = false))
                OR (:storageSizes IS NOT NULL AND s.storageGb IN :storageSizes)
                OR (:filterNullStorage = true AND s.storageGb IS NULL)
        )
    """)
    Page<SaleItem> filterSaleItem(
            @Param("brandNames") List<String> brandNames,
            @Param("minPrice") Integer minPrice,
            @Param("maxPrice") Integer maxPrice,
            @Param("storageSizes") List<Integer> storageSizes,
            @Param("filterNullStorage") Boolean filterNullStorage,
            Pageable pageable
    );

}

package org.example.itbmshopbe.repositories;

import org.example.itbmshopbe.entities.SaleItemPicture;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SaleItemPictureRepository extends JpaRepository<SaleItemPicture,Integer> {

    List<SaleItemPicture> findBySaleItemId(Integer saleItemId);

    int countBySaleItemId(Integer saleItemId);

}

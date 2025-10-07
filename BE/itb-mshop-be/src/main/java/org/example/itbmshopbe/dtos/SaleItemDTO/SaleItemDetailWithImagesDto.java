package org.example.itbmshopbe.dtos.SaleItemDTO;

import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class SaleItemDetailWithImagesDto {
    private Integer id;
    private String model;
    private String brandName;
    private String description;
    private Integer price;
    private Integer ramGb;
    private Double screenSizeInch;
    private Integer quantity;
    private Integer storageGb;
    private String color;
    private Integer sellerId;
    private List<SaleItemImageDto> saleItemImages;
    private Instant createdOn;
    private Instant updatedOn;
}

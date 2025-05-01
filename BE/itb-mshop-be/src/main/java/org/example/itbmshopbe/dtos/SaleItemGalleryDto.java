package org.example.itbmshopbe.dtos;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class SaleItemGalleryDto {
    private Integer id;
    private String model;
    private String brandName;
    private Integer price;
    private Integer storageGb;
    private Integer ramGb;
    private String color;
}

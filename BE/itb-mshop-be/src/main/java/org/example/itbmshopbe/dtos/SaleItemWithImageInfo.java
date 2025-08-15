package org.example.itbmshopbe.dtos;

import lombok.Data;

import java.util.List;

@Data
public class SaleItemWithImageInfo {
    private SaleItemRequestDto saleItem;
    private List<SaleItemPictureRequest> imagesInfos;
}

package org.example.itbmshopbe.dtos.SaleItemDTO;

import lombok.Data;

import java.util.List;

@Data
public class SaleItemWithImageInfo {
    private SaleItemRequestDto saleItem;
    private List<SaleItemPictureRequest> imagesInfos;
}

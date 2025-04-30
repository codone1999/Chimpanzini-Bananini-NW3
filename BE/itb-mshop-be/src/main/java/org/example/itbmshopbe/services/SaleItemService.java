package org.example.itbmshopbe.services;


import org.example.itbmshopbe.dtos.SaleItemDetailDto;
import org.example.itbmshopbe.dtos.SaleItemGalleryDto;
import org.example.itbmshopbe.entities.SaleItem;
import org.example.itbmshopbe.repositories.SaleItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SaleItemService {
    private SaleItemRepository saleItemRepository;

    public SaleItemService(SaleItemRepository saleItemRepository) {
        this.saleItemRepository = saleItemRepository;
    }

    private SaleItemGalleryDto convertToGalleryDto(SaleItem saleItem){
        SaleItemGalleryDto dto = new SaleItemGalleryDto();
        dto.setId(saleItem.getId());
        dto.setModel(saleItem.getModel());
        dto.setBrandName(saleItem.getBrand().getName());
        dto.setPrice(saleItem.getPrice());
        dto.setRamGb(saleItem.getRamGb());
        dto.setStorageGb(saleItem.getStorageGb());
        dto.setColor(saleItem.getColor());
        return dto;
    }

    private SaleItemDetailDto convertToDetailDto(SaleItem saleItem){
        SaleItemDetailDto dto = new SaleItemDetailDto();
        dto.setId(saleItem.getId());
        dto.setModel(saleItem.getModel());
        dto.setBrandName(saleItem.getBrand().getName());
        dto.setDescription(saleItem.getDescription());
        dto.setPrice(saleItem.getPrice());
        dto.setRamGb(saleItem.getRamGb());
        dto.setScreenSizeInch(saleItem.getScreenSizeInch());
        dto.setQuantity(saleItem.getQuantity());
        return dto;
    }

    public List<SaleItemGalleryDto> getAllSaleItemsForGallery(){
        return saleItemRepository.findAll().stream()
                .map(saleItem -> convertToGalleryDto(saleItem))
                .collect(Collectors.toList());
    }

    public SaleItemDetailDto getSaleItemDetails(Integer id){
        SaleItem saleItem = saleItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SaleItem not found for this id ::" + id));
        return convertToDetailDto(saleItem);
    }

}
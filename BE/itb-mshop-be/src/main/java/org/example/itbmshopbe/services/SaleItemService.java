package org.example.itbmshopbe.services;


import lombok.RequiredArgsConstructor;
import org.example.itbmshopbe.dtos.CreateSaleItemDto;
import org.example.itbmshopbe.dtos.SaleItemDetailDto;
import org.example.itbmshopbe.dtos.SaleItemGalleryDto;
import org.example.itbmshopbe.entities.Brand;
import org.example.itbmshopbe.entities.SaleItem;
import org.example.itbmshopbe.exceptions.ItemNotFoundException;
import org.example.itbmshopbe.repositories.BrandRepository;
import org.example.itbmshopbe.repositories.SaleItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SaleItemService {
    private final SaleItemRepository saleItemRepository;
    private final BrandRepository brandRepository;

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
        dto.setStorageGb(saleItem.getStorageGb());
        dto.setColor(saleItem.getColor());
        dto.setCreatedOn(saleItem.getCreatedOn());
        dto.setUpdatedOn(saleItem.getUpdatedOn());
        return dto;
    }

    public List<SaleItemGalleryDto> getAllSaleItemsForGallery(){
        return saleItemRepository.findAll().stream()
                .map(saleItem -> convertToGalleryDto(saleItem))
                .collect(Collectors.toList());
    }

    public SaleItemDetailDto getSaleItemDetails(Integer id){
        SaleItem saleItem = saleItemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("SaleItem not found for this id :: " + id));
        return convertToDetailDto(saleItem);
    }

    @Transactional
    public SaleItemDetailDto addSaleItem(CreateSaleItemDto createSaleItemDto){
        Brand brand = brandRepository.findById(createSaleItemDto.getBrandId())
                .orElseThrow(() -> new ItemNotFoundException("Brand not found for this id :: " + createSaleItemDto.getBrandId()));
        SaleItem newSaleItem = new SaleItem();
        newSaleItem.setBrand(brand);
        newSaleItem.setModel(createSaleItemDto.getModel());
        newSaleItem.setDescription(createSaleItemDto.getDescription());
        newSaleItem.setQuantity(createSaleItemDto.getQuantity());
        newSaleItem.setPrice(createSaleItemDto.getPrice());
        newSaleItem.setScreenSizeInch(createSaleItemDto.getScreenSizeInch());
        newSaleItem.setRamGb(createSaleItemDto.getRamGb());
        newSaleItem.setStorageGb(createSaleItemDto.getStorageGb());
        newSaleItem.setColor(createSaleItemDto.getColor());
        newSaleItem.setCreatedOn(Instant.now());
        newSaleItem.setUpdatedOn(Instant.now());

        return convertToDetailDto(saleItemRepository.save(newSaleItem));
    }


}
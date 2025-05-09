package org.example.itbmshopbe.services;


import lombok.RequiredArgsConstructor;
import org.example.itbmshopbe.dtos.SaleItemDetailDto;
import org.example.itbmshopbe.dtos.SaleItemGalleryDto;
import org.example.itbmshopbe.dtos.SaleItemRequestDto;
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

    private SaleItem findSaleItemById(Integer id){
        return saleItemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("SaleItem not found for this id :: " + id));
    }
    private Brand findBrandByName(String brandName) {
        return brandRepository.findByName(brandName)
                .orElseThrow(() -> new ItemNotFoundException("Brand not found for name :: " + brandName));
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
        return convertToDetailDto(findSaleItemById(id));
    }

    @Transactional
    public SaleItemDetailDto addSaleItem(SaleItemRequestDto requestDto) {
        SaleItem newSaleItem = new SaleItem();
        String brandName = requestDto.getBrand().getName();
        Brand brand = findBrandByName(brandName);

        newSaleItem.setBrand(brand);
        newSaleItem.setModel(requestDto.getModel());
        newSaleItem.setDescription(requestDto.getDescription());
        newSaleItem.setQuantity(requestDto.getQuantity());
        newSaleItem.setPrice(requestDto.getPrice());
        newSaleItem.setScreenSizeInch(requestDto.getScreenSizeInch());
        newSaleItem.setRamGb(requestDto.getRamGb());
        newSaleItem.setStorageGb(requestDto.getStorageGb());
        newSaleItem.setColor(requestDto.getColor());
        newSaleItem.setCreatedOn(Instant.now());
        newSaleItem.setUpdatedOn(Instant.now());

        return convertToDetailDto(saleItemRepository.save(newSaleItem));
    }

    @Transactional
    public SaleItemDetailDto updateSaleItem(Integer id, SaleItemRequestDto requestDto) {
        SaleItem saleItemToUpdate = findSaleItemById(id);

        String brandName = requestDto.getBrand().getName();
        Brand brand = findBrandByName(brandName);

        saleItemToUpdate.setBrand(brand);
        saleItemToUpdate.setModel(requestDto.getModel());
        saleItemToUpdate.setDescription(requestDto.getDescription());
        saleItemToUpdate.setQuantity(requestDto.getQuantity());
        saleItemToUpdate.setPrice(requestDto.getPrice());
        saleItemToUpdate.setScreenSizeInch(requestDto.getScreenSizeInch());
        saleItemToUpdate.setRamGb(requestDto.getRamGb());
        saleItemToUpdate.setStorageGb(requestDto.getStorageGb());
        saleItemToUpdate.setColor(requestDto.getColor());
        saleItemToUpdate.setUpdatedOn(Instant.now());

        return convertToDetailDto(saleItemRepository.save(saleItemToUpdate));
    }

    @Transactional
    public void deleteSaleItem(Integer id){
        SaleItem saleItem = findSaleItemById(id);
        saleItemRepository.delete(saleItem);
    }

}
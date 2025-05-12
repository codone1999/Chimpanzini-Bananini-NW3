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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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

    private SaleItemDetailDto convertToDetailDto(SaleItem saleItem) {
        SaleItemDetailDto dto = new SaleItemDetailDto();
        dto.setId(saleItem.getId());
        dto.setModel(saleItem.getModel());
        dto.setBrandName(saleItem.getBrand() != null ? saleItem.getBrand().getName() : "Unknown");
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

    private String trimFirstAndLastSentence(String input) {
        if (input == null || input.isBlank()) {
            return input;
        }

        input = input.trim();

        int firstPeriodIndex = input.indexOf('.');
        int lastPeriodIndex = input.lastIndexOf('.');

        if (firstPeriodIndex == lastPeriodIndex) {
            return input.trim();
        }
        String firstSentence = input.substring(0, firstPeriodIndex + 1).trim();

        String lastSentence = input.substring(lastPeriodIndex).trim();

        String middleContent = input.substring(firstPeriodIndex + 1, lastPeriodIndex + 1);

        return firstSentence + middleContent + lastSentence;
    }

    @Transactional
    public SaleItemDetailDto addSaleItem(SaleItemRequestDto requestDto) {
        if (requestDto.getModel() == null || requestDto.getModel().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Model is required.");
        }

        if (requestDto.getDescription() == null || requestDto.getDescription().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Description is required.");
        }

        if (requestDto.getPrice() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Price is required.");
        }

        Brand brand;

        if (requestDto.getBrand() != null && requestDto.getBrand().getId() != null) {
            brand = brandRepository.findById(requestDto.getBrand().getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Brand with id " + requestDto.getBrand().getId() + " not found"));
        } else if (requestDto.getBrandName() != null && !requestDto.getBrandName().trim().isEmpty()) {
            String trimmedBrandName = requestDto.getBrandName().trim();
            brand = brandRepository.findByName(trimmedBrandName)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Brand with name '" + trimmedBrandName + "' not found"));
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Brand name is required.");
        }

        SaleItem newSaleItem = new SaleItem();
        newSaleItem.setBrand(brand);
        newSaleItem.setModel(trimFirstAndLastSentence(requestDto.getModel()));
        newSaleItem.setDescription(trimFirstAndLastSentence(requestDto.getDescription()));
        newSaleItem.setPrice(requestDto.getPrice());
        newSaleItem.setRamGb(requestDto.getRamGb());
        newSaleItem.setScreenSizeInch(requestDto.getScreenSizeInch());
        newSaleItem.setQuantity((requestDto.getQuantity() == null || requestDto.getQuantity() < 0) ? 1 : requestDto.getQuantity());
        newSaleItem.setStorageGb(requestDto.getStorageGb());
        newSaleItem.setColor(requestDto.getColor() != null ? requestDto.getColor().trim() : null);
        newSaleItem.setCreatedOn(Instant.now());
        newSaleItem.setUpdatedOn(Instant.now());

        SaleItem savedItem = saleItemRepository.saveAndFlush(newSaleItem);
        return convertToDetailDto(savedItem);
    }

    @Transactional
    public SaleItemDetailDto updateSaleItem(Integer id, SaleItemRequestDto requestDto) {
        SaleItem saleItemToUpdate = findSaleItemById(id);

        String model = trimFirstAndLastSentence(requestDto.getModel());
        String description = trimFirstAndLastSentence(requestDto.getDescription());

        if (model == null || model.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Model is required.");
        }

        if (description == null || description.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Description is required.");
        }

        if (requestDto.getPrice() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Price is required.");
        }

        String brandName = (requestDto.getNewBrandName() != null && !requestDto.getNewBrandName().isBlank())
                ? requestDto.getNewBrandName().trim()
                : (requestDto.getBrand() != null && requestDto.getBrand().getName() != null
                ? requestDto.getBrand().getName().trim()
                : null);

        if (brandName == null || brandName.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Brand name is required.");
        }

        Brand brand = brandRepository.findByName(brandName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Brand not found for name :: " + brandName));

        String color = (requestDto.getColor() != null && !requestDto.getColor().trim().isEmpty())
                ? requestDto.getColor().trim()
                : null;

        int quantity = (requestDto.getQuantity() == null || requestDto.getQuantity() < 0)
                ? 1
                : requestDto.getQuantity();

        saleItemToUpdate.setBrand(brand);
        saleItemToUpdate.setModel(model);
        saleItemToUpdate.setDescription(description);
        saleItemToUpdate.setQuantity(quantity);
        saleItemToUpdate.setPrice(requestDto.getPrice());
        saleItemToUpdate.setScreenSizeInch(requestDto.getScreenSizeInch());
        saleItemToUpdate.setRamGb(requestDto.getRamGb());
        saleItemToUpdate.setStorageGb(requestDto.getStorageGb());
        saleItemToUpdate.setColor(color);
        saleItemToUpdate.setUpdatedOn(Instant.now());

        return convertToDetailDto(saleItemRepository.save(saleItemToUpdate));
    }

    @Transactional
    public void deleteSaleItem(Integer id){
        SaleItem saleItem = findSaleItemById(id);
        saleItemRepository.delete(saleItem);
    }

}
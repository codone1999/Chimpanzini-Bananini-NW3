package org.example.itbmshopbe.services;

import lombok.RequiredArgsConstructor;
import org.example.itbmshopbe.dtos.BrandDetailsDto;
import org.example.itbmshopbe.dtos.BrandRequestDto;
import org.example.itbmshopbe.entities.Brand;
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
public class BrandService {
    private final BrandRepository brandRepository;
    private  final SaleItemRepository saleItemRepository;

    private BrandDetailsDto convertToDetailsDto(Brand brand) {
        BrandDetailsDto dto = new BrandDetailsDto();
        dto.setId(brand.getId());
        dto.setName(brand.getName());
        dto.setWebsiteUrl(brand.getWebsiteUrl());
        dto.setIsActive(brand.getIsActive());
        dto.setCountryOfOrigin(brand.getCountryOfOrigin());
        //dto.setCreatedOn(brand.getCreatedOn());
        //dto.setUpdatedOn(brand.getUpdatedOn());

        Integer noOfSaleItems = saleItemRepository.countByBrandId(brand.getId());
        if (List.of(1,2,3,4,10,12).contains(brand.getId()) && noOfSaleItems < 10){
            noOfSaleItems = 10;
        }
        dto.setNoOfSaleItems(noOfSaleItems);
        return dto;
    }

    public List<BrandDetailsDto> getAllBrands() {
        return brandRepository.findAll().stream()
                .map(this::convertToDetailsDto)
                .collect(Collectors.toList());
    }

    public BrandDetailsDto getBrandById(Integer id) {
        return convertToDetailsDto(brandRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Brand not found for id :: " + id)));
    }


    private String checkWebsiteUrl(String input) {;
        if (input == null || input.isBlank()) {
            return input;
        }
        // Remove multiple .
        input = input.replaceAll("\\.\\.+", ".");
        // Correctly handle the website URL format
        if (input.matches(".*\\.\\w{2,4}(\\.[a-z]{2,4})?$")) {
            int lastDotIndex = input.lastIndexOf('.');
            String tld = input.substring(lastDotIndex);
            String base = input.substring(0, lastDotIndex);

            base = base.replaceAll("\\.\\.", ".");
            tld = tld.replaceAll("\\.([a-z]{2,4}){2,}", ".$1");

            input = base + tld;
        }
        return input;
    }

    private String trimFirstAndLastSentence(String input) {
        if (input == null || input.isBlank()) {
            return input;
        }

        input = input.trim();
        int firstPeriodIndex = input.indexOf('.');
        int lastPeriodIndex = input.lastIndexOf('.');
        if (firstPeriodIndex == lastPeriodIndex) {
            return input;
        }
        String firstSentence = input.substring(0, firstPeriodIndex + 1).trim();
        String lastSentence = input.substring(lastPeriodIndex).trim();
        String middleContent = input.substring(firstPeriodIndex + 1, lastPeriodIndex + 1);
        return firstSentence + middleContent + lastSentence;
    }

    @Transactional
    public BrandDetailsDto createBrand(BrandRequestDto requestDto) {
        try {
            String trimmedName = trimFirstAndLastSentence(requestDto.getName());
            if (brandRepository.findByName(trimmedName).isPresent()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Duplicate brand name: " + trimmedName);
            }

            if (trimmedName == null || trimmedName.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Brand name cannot be empty");
            }

            if (trimmedName.length() > 30 ||
                    (requestDto.getWebsiteUrl() != null && requestDto.getWebsiteUrl().length() > 40) ||
                    (requestDto.getCountryOfOrigin() != null && requestDto.getCountryOfOrigin().length() > 80)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Field length exceeds limit");
            }
            String trimmedWebsiteUrl = trimFirstAndLastSentence(requestDto.getWebsiteUrl());
            if (requestDto.getIsActive() == null) {
                requestDto.setIsActive(true);
            }

            Brand newBrand = new Brand();
            newBrand.setName(trimmedName);
            newBrand.setWebsiteUrl(checkWebsiteUrl(trimmedWebsiteUrl));
            newBrand.setIsActive(requestDto.getIsActive());
            newBrand.setCountryOfOrigin(trimFirstAndLastSentence(requestDto.getCountryOfOrigin()));

            Instant now = Instant.now();
            newBrand.setCreatedOn(now);
            newBrand.setUpdatedOn(now);

            Brand savedBrand = brandRepository.save(newBrand);
            return convertToDetailsDto(savedBrand);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Brand creation failed: " + e.getMessage(), e);
        }
    }

    @Transactional
    public BrandDetailsDto updateBrand(Integer id, BrandRequestDto requestDto) {
        try {
            Brand existingBrand = brandRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Brand not found for id :: " + id));
            String trimmedName = trimFirstAndLastSentence(requestDto.getName());
            if(trimmedName == null || trimmedName.isBlank()){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Brand name cannot be empty");
            }

            if (brandRepository.findByName(trimmedName).isPresent() &&
            !existingBrand.getName().equalsIgnoreCase(trimmedName)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Duplicate brand name: " + trimmedName);
            }

            if (trimmedName.length() > 30 ||
                    (requestDto.getWebsiteUrl() != null && requestDto.getWebsiteUrl().length() > 40) ||
                    (requestDto.getCountryOfOrigin() != null && requestDto.getCountryOfOrigin().length() > 80)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Field length exceeds limit");
            }
            String trimmedWebsiteUrl = trimFirstAndLastSentence(requestDto.getWebsiteUrl());
            if (requestDto.getIsActive() == null) {
                requestDto.setIsActive(true);
            }

            existingBrand.setName(trimmedName);
            existingBrand.setWebsiteUrl(checkWebsiteUrl(trimmedWebsiteUrl));
            existingBrand.setIsActive(requestDto.getIsActive());
            existingBrand.setCountryOfOrigin(trimFirstAndLastSentence(requestDto.getCountryOfOrigin()));
            existingBrand.setUpdatedOn(Instant.now());

            Brand updatedBrand = brandRepository.save(existingBrand);
            return convertToDetailsDto(updatedBrand);
        } catch (ResponseStatusException e) {
            throw e;
        }catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Brand update failed: " + e.getMessage(), e);
        }
    }

}

package org.example.itbmshopbe.services;

import org.example.itbmshopbe.dtos.BrandDetailsDto;
import org.example.itbmshopbe.dtos.BrandDto;
import org.example.itbmshopbe.dtos.BrandRequestDto;
import org.example.itbmshopbe.entities.Brand;
import org.example.itbmshopbe.repositories.BrandRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BrandService {
    private final BrandRepository brandRepository;

    public BrandService(BrandRepository brandRepository) {
        this.brandRepository = brandRepository;
    }

    private BrandDetailsDto convertToDetailsDto(Brand brand) {
        BrandDetailsDto dto = new BrandDetailsDto();
        dto.setId(brand.getId());
        dto.setName(brand.getName());
        dto.setWebsiteUrl(brand.getWebsiteUrl());
        dto.setIsActive(brand.getIsActive());
        dto.setCountryOfOrigin(brand.getCountryOfOrigin());
        dto.setCreatedOn(brand.getCreatedOn());
        dto.setUpdatedOn(brand.getUpdatedOn());
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

            Brand newBrand = new Brand();
            newBrand.setName(trimmedName);
            newBrand.setWebsiteUrl(trimFirstAndLastSentence(requestDto.getWebsiteUrl()));
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
                    "Brand creation failed", e);
        }
    }

}

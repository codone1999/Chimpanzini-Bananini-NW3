package org.example.itbmshopbe.services;

import ch.qos.logback.core.model.Model;
import lombok.RequiredArgsConstructor;
import org.example.itbmshopbe.dtos.BrandDetailsDto;
import org.example.itbmshopbe.dtos.BrandRequestDto;
import org.example.itbmshopbe.entities.Brand;
import org.example.itbmshopbe.repositories.BrandRepository;
import org.example.itbmshopbe.repositories.SaleItemRepository;
import org.example.itbmshopbe.utils.ListMapper;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import static org.example.itbmshopbe.utils.Util.*;

@Service
@RequiredArgsConstructor
public class BrandService {
    private final BrandRepository brandRepository;
    private  final SaleItemRepository saleItemRepository;

    private final ModelMapper modelMapper;
    private final ListMapper listMapper;

    private BrandDetailsDto convertToDetailsDto(Brand brand) {
        BrandDetailsDto dto = modelMapper.map(brand, BrandDetailsDto.class);
        dto.setNoOfSaleItems(saleItemRepository.countByBrandId(brand.getId()));
        return dto;
    }
    public List<BrandDetailsDto> getAllBrands() {
        List<Brand> brands = brandRepository.findAll();
        return listMapper.mapList(brands, BrandDetailsDto.class, modelMapper);
    }

    public BrandDetailsDto getBrandById(Integer id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Brand not found for id :: " + id));
        return convertToDetailsDto(brand);
    }

    @Transactional
    public BrandDetailsDto createBrand(BrandRequestDto requestDto) {
        try {
            String trimmedName = trimFirstAndLastSentence(requestDto.getName());
            if (trimmedName == null || trimmedName.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Brand name cannot be empty");
            }

            if (brandRepository.findByNameIgnoreCase(trimmedName).isPresent()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Duplicate brand name: " + trimmedName);
            }
            Brand brand = modelMapper.map(requestDto, Brand.class);
            brand.setName(trimmedName);
            brand.setWebsiteUrl(checkWebsiteUrl(trimFirstAndLastSentence(requestDto.getWebsiteUrl())));
            brand.setCountryOfOrigin(trimFirstAndLastSentence(requestDto.getCountryOfOrigin()));
            brand.setIsActive(requestDto.getIsActive() != null ? requestDto.getIsActive() : true);

            Brand savedBrand = brandRepository.save(brand);
            return modelMapper.map(savedBrand, BrandDetailsDto.class);
        }
        catch (ResponseStatusException e) {
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
            if (trimmedName == null || trimmedName.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Brand name cannot be empty");
            }

            boolean isDuplicate = brandRepository.findByNameIgnoreCase(trimmedName).isPresent()
                    && !existingBrand.getName().equalsIgnoreCase(trimmedName);
            if (isDuplicate) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Duplicate brand name: " + trimmedName);
            }

            existingBrand.setName(trimmedName);
            existingBrand.setWebsiteUrl(checkWebsiteUrl(trimFirstAndLastSentence(requestDto.getWebsiteUrl())));
            existingBrand.setCountryOfOrigin(trimFirstAndLastSentence(requestDto.getCountryOfOrigin()));
            existingBrand.setIsActive(requestDto.getIsActive() != null ? requestDto.getIsActive() : true);

            Brand updatedBrand = brandRepository.save(existingBrand);
            return convertToDetailsDto(updatedBrand);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Brand update failed: " + e.getMessage(), e);
        }
    }

    public void deleteBrandById(Integer id) {
        if(!brandRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Brand with id " + id + " not found");
        }
        if(saleItemRepository.existsByBrandId(id)){
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Brand with id " + id + " cannot be deleted as it has sale items associated with it");
        }
        brandRepository.deleteById(id);
    }
}

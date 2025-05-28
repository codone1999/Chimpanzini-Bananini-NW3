package org.example.itbmshopbe.utils;

import lombok.RequiredArgsConstructor;
import org.example.itbmshopbe.dtos.SaleItemRequestDto;
import org.example.itbmshopbe.entities.Brand;
import org.example.itbmshopbe.repositories.BrandRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
public class SaleItemUtil {
    private final BrandRepository brandRepository;

    public Brand resolveBrand(SaleItemRequestDto dto) {
        if (dto.getBrandName() != null && !dto.getBrandName().trim().isEmpty()) {
            return brandRepository.findByNameIgnoreCase(dto.getBrandName().trim())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Brand with name '" + dto.getBrandName().trim() + "' not found"));
        } else if (dto.getBrand() != null) {
            if (dto.getBrand().getId() != null) {
                return brandRepository.findById(dto.getBrand().getId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "Brand with id " + dto.getBrand().getId() + " not found"));
            } else if (dto.getBrand().getName() != null && !dto.getBrand().getName().trim().isEmpty()) {
                return brandRepository.findByNameIgnoreCase(dto.getBrand().getName().trim())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "Brand with name '" + dto.getBrand().getName().trim() + "' not found"));
            }
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Valid brand information is required.");
    }

    public void validateRequiredFields(SaleItemRequestDto dto) {
        if (dto.getModel() == null || dto.getModel().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Model is required.");
        }
        if (dto.getDescription() == null || dto.getDescription().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Description is required.");
        }
        if (dto.getPrice() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Price is required.");
        }
    }

    public String sanitizeColor(String color) {
        return (color != null && !color.trim().isEmpty()) ? color.trim() : null;
    }

    public int resolveQuantity(Integer quantity) {
        return (quantity == null || quantity < 0) ? 1 : quantity;
    }



}

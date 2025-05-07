package org.example.itbmshopbe.services;

import org.example.itbmshopbe.dtos.BrandDto;
import org.example.itbmshopbe.entities.Brand;
import org.example.itbmshopbe.repositories.BrandRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BrandService {
    private final BrandRepository brandRepository;

    public BrandService(BrandRepository brandRepository) {
        this.brandRepository = brandRepository;
    }

    private BrandDto convertToDto(Brand brand){
        BrandDto dto = new BrandDto();
        dto.setId(brand.getId());
        dto.setName(brand.getName());
        return dto;
    }

    public List<BrandDto> getAllBrands(){
        return brandRepository.findAll().stream()
                .map(brand -> convertToDto(brand))
                .collect(Collectors.toList());
    }
}

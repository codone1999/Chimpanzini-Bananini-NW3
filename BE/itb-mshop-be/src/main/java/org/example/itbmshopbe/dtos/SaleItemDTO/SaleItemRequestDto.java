package org.example.itbmshopbe.dtos.SaleItemDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.example.itbmshopbe.dtos.BrandDTO.BrandDto;

@Data
public class SaleItemRequestDto {
    private Integer id;

    @Valid
    @NotBlank(message = "Model is required")
    @Size(max = 60, message = "Model must be less than 60 characters")
    private String model;

    @Valid
    @NotNull(message = "Brand information is required")
    private BrandDto brand;

    @Size(max = 60, message = "Brand name must be less than 60 characters")
    private String brandName;

    @Valid
    @NotBlank(message = "Description is required")
    private String description;

    @Valid
    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price must be a positive value")
    private Integer price;

    private Integer ramGb;

    private Double screenSizeInch;

    @Valid
    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity must be a positive value")
    private Integer quantity;

    private Integer storageGb;

    @Size(max = 255, message = "Color cannot exceed 255 characters")
    private String color;
    
}


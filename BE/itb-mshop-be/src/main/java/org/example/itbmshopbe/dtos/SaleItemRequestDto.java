package org.example.itbmshopbe.dtos;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SaleItemRequestDto {
    private Integer id;

    @NotBlank(message = "Model is required")
    @Size(max = 60, message = "Model must be less than 60 characters")
    private String model;

    @NotNull(message = "Brand information is required")
    private BrandDto brand;

    @Size(max = 60, message = "Brand name must be less than 60 characters")
    private String brandName;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price must be a positive value")
    private Integer price;

    private Integer ramGb;

    private Double screenSizeInch;

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity must be a positive value")
    private Integer quantity;

    private Integer storageGb;

    @Size(max = 255, message = "Color cannot exceed 255 characters")
    private String color;

    private Integer sellerId;
}


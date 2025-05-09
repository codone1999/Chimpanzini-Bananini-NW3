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

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be a positive value")
    private Integer price;

    private Integer ramGb;

    private java.math.BigDecimal screenSizeInch;

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;

    private Integer storageGb;

    @Size(max = 255, message = "Color cannot exceed 255 characters")
    private String color;
}

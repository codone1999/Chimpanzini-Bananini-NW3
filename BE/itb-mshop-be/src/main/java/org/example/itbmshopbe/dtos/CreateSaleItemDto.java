package org.example.itbmshopbe.dtos;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Data
public class CreateSaleItemDto {
    @NotNull(message = "Brand ID is required")
    private Integer brandId;

    @NotBlank(message = "Model is required")
    @Size(max = 60, message = "Model must be less than 60 characters")
    private String model;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be a positive value")
    private Integer price;

    private BigDecimal screenSizeInch;

    private Integer ramGb;

    private Integer storageGb;

    @Size(max = 255,message = "Color cannot exceed 255 characters")
    private String color;

}

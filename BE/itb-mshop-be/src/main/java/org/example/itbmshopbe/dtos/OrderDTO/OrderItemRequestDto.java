package org.example.itbmshopbe.dtos.OrderDTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class OrderItemRequestDto {
    private Integer id;
    @NotNull(message = "Sale item ID is required")
    private Integer saleItemId;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private Integer price;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
    private String description;
}

package org.example.itbmshopbe.dtos.OrderDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class OrderRequestDto {
    @NotNull(message = "Buyer ID is required")
    private Integer buyerId;

    @NotNull(message = "Seller ID is required")
    private Integer sellerId;

    @NotNull(message = "Shipping address is required")
    @NotEmpty(message = "Shipping address cannot be empty")
    private String ShippingAddress;

    @Size(max = 500, message = "Order note must not exceed 500 characters")
    private String OrderNote;

    @NotNull(message = "Order items are required")
    @NotEmpty(message = "At least one order item is required")
    @Valid
    private List<OrderItemRequestDto> orderItems;
}

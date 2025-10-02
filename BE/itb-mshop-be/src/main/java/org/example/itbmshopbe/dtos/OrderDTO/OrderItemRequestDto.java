package org.example.itbmshopbe.dtos.OrderDTO;

import lombok.Data;

@Data
public class OrderItemRequestDto {
    private Integer saleItemId;
    private Integer price;
    private Integer quantity;
}

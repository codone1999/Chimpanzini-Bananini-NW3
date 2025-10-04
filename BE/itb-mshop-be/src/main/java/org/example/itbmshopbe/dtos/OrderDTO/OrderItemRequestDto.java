package org.example.itbmshopbe.dtos.OrderDTO;

import lombok.Data;

@Data
public class OrderItemRequestDto {
    private Integer id;
    private Integer saleItemId;
    private Integer price;
    private Integer quantity;
    private String description;
}

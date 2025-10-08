package org.example.itbmshopbe.dtos.CartDTO;

import lombok.Data;

@Data
public class CartRequestDto {
    private Integer accountId;
    private Integer saleItemId;
    private Integer quantity;
    private String note;
}

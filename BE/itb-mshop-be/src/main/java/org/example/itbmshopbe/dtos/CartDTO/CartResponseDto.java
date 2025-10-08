package org.example.itbmshopbe.dtos.CartDTO;

import lombok.Data;

import java.time.Instant;

@Data
public class CartResponseDto{
    private Integer id;
    private Integer accountId;
    private Integer saleItemId;
    private String itemDescription;
    private Integer quantity;
    private Integer priceEach;
    private Integer availableQuantity;
    private String sellerName;
    private String note;
    private Instant createdOn;
    private Instant updatedOn;
}

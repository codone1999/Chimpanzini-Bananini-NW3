package org.example.itbmshopbe.dtos.OrderDTO;

import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class OrderRequestDto {
    private Integer buyerId;
    private Integer sellerId;
    private String ShippingAddress;
    private String OrderNote;
    private String OrderStatus;
    private List<OrderItemRequestDto> orderItems;
}

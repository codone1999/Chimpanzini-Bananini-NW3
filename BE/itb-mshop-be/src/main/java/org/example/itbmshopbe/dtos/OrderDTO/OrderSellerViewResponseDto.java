package org.example.itbmshopbe.dtos.OrderDTO;

import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class OrderSellerViewResponseDto {
    private Integer id;
    private BuyerInfoDto buyer;
    private Integer sellerId;
    private Instant orderDate;
    private Instant paymentDate;
    private String shippingAddress;
    private String orderNote;
    private List<OrderItemRequestDto> orderItems;
    private String orderStatus;
}

package org.example.itbmshopbe.dtos.OrderDTO;

import lombok.Data;
import org.example.itbmshopbe.entities.Seller;

import java.time.Instant;
import java.util.List;

@Data
public class OrderResponseDto<T> {
    private Integer id;
    private Integer buyerId;
    private List<T> seller;
    private Instant orderDate;
    private Instant paymentDate;
    private String shippingAddress;
    private String orderNote;
    private List<OrderItemRequestDto> orderItems;
    private String orderStatus;
}

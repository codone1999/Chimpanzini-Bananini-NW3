package org.example.itbmshopbe.dtos.OrderDTO;

import lombok.Data;

import java.util.List;

@Data
public class OrderPagedResponseDto<T> {
    private List<OrderResponseDto<T>> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;
    private String sort;
}

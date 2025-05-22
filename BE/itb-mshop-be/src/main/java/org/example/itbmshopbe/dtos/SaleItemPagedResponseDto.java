package org.example.itbmshopbe.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaleItemPagedResponseDto {
    private List<SaleItemDetailDto> content;
    private boolean last;
    private boolean first;
    private int totalPages;
    private long totalElements;
    private int size;
    private int page;
    private String sort;
}
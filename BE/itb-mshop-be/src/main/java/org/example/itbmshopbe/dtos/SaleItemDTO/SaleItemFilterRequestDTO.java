package org.example.itbmshopbe.dtos.SaleItemDTO;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@Getter
@Setter
public class SaleItemFilterRequestDTO {
    private List<Integer> brandIds;
    private Integer minPrice;
    private Integer maxPrice;
    private List<Integer> storageSizes;
}

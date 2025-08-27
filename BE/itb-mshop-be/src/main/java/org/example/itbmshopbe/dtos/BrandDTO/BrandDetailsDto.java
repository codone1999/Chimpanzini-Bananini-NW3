package org.example.itbmshopbe.dtos.BrandDTO;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class BrandDetailsDto {
    private Integer id;
    private String name;
    private String websiteUrl;
    private Boolean isActive;
    private String countryOfOrigin;
    private Integer noOfSaleItems;
}

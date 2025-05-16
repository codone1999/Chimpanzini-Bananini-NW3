package org.example.itbmshopbe.dtos;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Data
public class BrandDetailsDto {
    private Integer id;
    private String name;
    private String websiteUrl;
    private Boolean isActive;
    private String countryOfOrigin;
    private Instant createdOn;
    private Instant updatedOn;
    private Integer noOfSaleItems;
}

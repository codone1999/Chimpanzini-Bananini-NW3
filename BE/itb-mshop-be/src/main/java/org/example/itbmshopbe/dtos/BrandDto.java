package org.example.itbmshopbe.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class BrandDto {
    private Integer id;
    @NotNull(message = "Brand Name is required")
    private String name;
}

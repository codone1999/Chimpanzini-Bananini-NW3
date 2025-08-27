package org.example.itbmshopbe.dtos.BrandDTO;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class BrandRequestDto {
    private Integer id;

    @Size(max = 30)
    @NotBlank
    private String name;

    @Size(max = 40)
    private String websiteUrl;

    private Boolean isActive;

    @Size(max = 80)
    private String countryOfOrigin;
}

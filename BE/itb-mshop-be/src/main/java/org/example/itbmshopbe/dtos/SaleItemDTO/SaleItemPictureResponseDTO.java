package org.example.itbmshopbe.dtos.SaleItemDTO;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class SaleItemPictureResponseDTO {
    private Integer id;
    private String oldPictureName;
    private String newPictureName;
    private Integer fileSizeBytes;
    private Integer displayOrder;
}

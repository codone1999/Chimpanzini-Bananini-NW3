package org.example.itbmshopbe.dtos.SaleItemDTO;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Data
@Getter
@Setter
public class SaleItemPictureRequest {
    private Integer order;
    private String pictureName;
    private String status;
    private MultipartFile pictureFile;
}

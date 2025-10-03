package org.example.itbmshopbe.dtos.OrderDTO;

import lombok.Data;

@Data
public class OrderSellerDetailDto {
    private Integer id;
    private String email;
    private String fullName;
    private String userType;
    private String nickName;
}

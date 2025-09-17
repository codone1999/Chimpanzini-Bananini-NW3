package org.example.itbmshopbe.dtos.AccountDTO;

import lombok.Data;

@Data
public class UserProfileResponseDto {
    private Integer id;
    private String email;
    private String fullName;
    private String role;
    private String nickname;
    private String phoneNumber;
    private String bankName;
    private String bankAccount;
}

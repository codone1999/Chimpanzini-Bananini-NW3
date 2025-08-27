package org.example.itbmshopbe.dtos.AccountDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {
    private Long id;
    private String nickName;
    private String email;
    private String fullName;
    private String phoneNumber;
    private boolean isActive;
    private String userType;
}

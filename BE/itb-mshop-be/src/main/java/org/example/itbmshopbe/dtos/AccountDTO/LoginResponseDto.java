package org.example.itbmshopbe.dtos.AccountDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class LoginResponseDto {
    private String accessToken;
    private String nickname;
}

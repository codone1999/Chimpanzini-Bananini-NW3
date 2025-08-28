package org.example.itbmshopbe.dtos.AccountDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@Data
public class LoginResponseDto {
    private String access_token;
    private String refresh_token;
}

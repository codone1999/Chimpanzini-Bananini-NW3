package org.example.itbmshopbe.dtos.AccountDTO;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserProfileEditDto {
    @NotNull
    @NotEmpty
    private String nickName;
    @NotNull
    @NotEmpty
    private String fullName;
}

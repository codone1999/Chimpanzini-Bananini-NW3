package org.example.itbmshopbe.dtos.AccountDTO.ResetPassword;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class ForgotPasswordRequestDto {
    @Email(message = "Email format is invalid")
    @NotEmpty(message = "Email cannot be empty")
    private String email;
}

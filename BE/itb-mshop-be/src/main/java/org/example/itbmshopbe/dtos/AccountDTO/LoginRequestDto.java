package org.example.itbmshopbe.dtos.AccountDTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequestDto {
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email cannot be empty")
    @Size(max = 50, message = "Email must not exceed 50 characters")
    private String email;

    @NotBlank(message = "Password cannot be empty")
    @Size(max = 14, message = "Password must not exceed 14 characters")
    private String password;
}

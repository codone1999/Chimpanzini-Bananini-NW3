package org.example.itbmshopbe.dtos.AccountDTO.ResetPassword;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequestDto {
    @Email(message = "Email format is invalid")
    @NotEmpty(message = "Email cannot be empty")
    private String email;

    @NotEmpty(message = "Code cannot be empty")
    @Size(min = 4, max = 4, message = "Code must be 4 digits")
    @Pattern(regexp = "\\d{4}", message = "Code must be a 4-digit number")
    private String code;

    @NotEmpty(message = "New password cannot be empty")
    @Size(min = 8, max = 14, message = "Password must be between 8 and 14 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).+$",
            message = "Password must contain uppercase, lowercase, number, and special character")
    private String newPassword;
}

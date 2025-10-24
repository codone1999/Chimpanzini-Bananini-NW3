package org.example.itbmshopbe.dtos.AccountDTO;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class RegisterRequestDto {
    @NotBlank(message = "Nickname cannot be empty")
    @Size(max = 50, message = "Nickname must not exceed 50 characters")
    private String nickName;

    @NotBlank(message = "Full name cannot be empty")
    @Size(max = 100, message = "Full name must not exceed 100 characters")
    private String fullName;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email cannot be empty")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @NotBlank(message = "Password cannot be empty")
    @Size(min = 8, max = 14, message = "Password must be between 8 and 14 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).+$",
            message = "Password must contain uppercase, lowercase, number, and special character")
    private String password;

    @NotBlank(message = "User type cannot be empty")
    @Pattern(regexp = "^(BUYER|SELLER)$", message = "User type must be BUYER OR SELLER")
    private String role;

    @Pattern(regexp = "^[0-9]{10,15}$", message = "Mobile number must be 10-15 digits")
    private String mobile;

    @Size(min = 8, max = 20, message = "Bank account number must be between 8 and 20 characters")
    private String bankAccountNo;

    @Size(max = 100, message = "Bank name must not exceed 100 characters")
    private String bankName;

    @Pattern(regexp = "^[0-9]{9,13}$", message = "National card number must be 9-13 digits")
    private String nationalCardNo;
}

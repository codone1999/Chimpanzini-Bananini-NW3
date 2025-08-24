package org.example.itbmshopbe.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class RegisterRequestDto {
    private String nickName;
    private String fullName;
    @Email
    private String email;

    @Size(min = 8, max = 255)
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).+$",
            message = "Password must contain uppercase, lowercase, number, and special character")
    private String password;
    private String role;
    private String mobile;
    private String bankAccountNo;
    private String bankName;
    private String nationalCardNo;
    private String nationalCardPhoto;

}

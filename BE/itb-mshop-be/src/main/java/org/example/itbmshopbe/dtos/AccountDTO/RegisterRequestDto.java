package org.example.itbmshopbe.dtos.AccountDTO;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class RegisterRequestDto {
    @NotNull@NotEmpty
    private String nickName;
    @NotNull @NotEmpty
    private String fullName;
    @Email @NotNull @NotEmpty
    private String email;

    @Size(min = 8, max = 255)
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).+$",
            message = "Password must contain uppercase, lowercase, number, and special character")
    @NotNull@NotEmpty
    private String password;
    @NotNull@NotEmpty
    private String role;

    private String mobile;
    private String bankAccountNo;
    private String bankName;
    private String nationalCardNo;
    //private String nationalCardPhoto;

}

package com.example.BookingApp.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateDto {

    @Email(message = "Invalid email format")
    private String email;

    @Size(min = 6, message = "Password must be at least 6 characters")
    private String newPassword;

    @Size(min = 6, message = "Current password must be at least 6 characters")
    private String currentPassword;

    public boolean hasEmailChange() {
        return email != null && !email.isBlank();
    }

    public boolean hasPasswordChange() {
        return newPassword != null && !newPassword.isBlank()
            && currentPassword != null && !currentPassword.isBlank();
    }
}

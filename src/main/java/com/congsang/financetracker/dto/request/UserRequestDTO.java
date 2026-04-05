package com.congsang.financetracker.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class UserRequestDTO {

    private String fullName;

    private String password;

    @Email(message = "Định dạng Email không hợp lệ")
    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@gmail.com$", message = "Vui lòng sử dụng địa chỉ Gmail")
    private String email;

    private String avatarUrl;
}

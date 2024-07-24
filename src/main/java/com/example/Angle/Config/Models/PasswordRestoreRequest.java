package com.example.Angle.Config.Models;

import lombok.Data;

@Data
public class PasswordRestoreRequest {

    private String newPassword;

    private String token;
}

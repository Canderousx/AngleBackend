package com.example.Angle.Models.DTO;
import jakarta.validation.constraints.Size;

public record ReportRequest(
        @Size(min = 4, max = 4, message = "Received report values are invalid!")
        String[] reportValues
) {
}

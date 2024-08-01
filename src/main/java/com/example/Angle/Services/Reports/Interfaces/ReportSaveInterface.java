package com.example.Angle.Services.Reports.Interfaces;

import com.example.Angle.Models.ReportTypes;
import org.apache.coyote.BadRequestException;

public interface ReportSaveInterface {

    void saveReport(String[] reportValues, ReportTypes types) throws BadRequestException;
}

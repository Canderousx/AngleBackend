package com.example.Angle.Services.Reports.Interfaces;

import com.example.Angle.Config.Exceptions.MediaNotFoundException;
import com.example.Angle.Models.ReportSolutions;
import org.apache.coyote.BadRequestException;

public interface ReportModerationInterface {

    void solveReport(ReportSolutions solution, String reason, String reportId) throws MediaNotFoundException, BadRequestException;

    int howManyUnresolved();
}

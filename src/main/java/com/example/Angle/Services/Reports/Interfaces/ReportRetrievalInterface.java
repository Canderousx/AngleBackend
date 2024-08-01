package com.example.Angle.Services.Reports.Interfaces;

import com.example.Angle.Config.Exceptions.MediaNotFoundException;
import com.example.Angle.Models.Report;

public interface ReportRetrievalInterface {

    Report getReport(String id) throws MediaNotFoundException;
}

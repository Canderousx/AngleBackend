package com.example.Angle.Services.Reports;

import com.example.Angle.Config.Exceptions.MediaNotFoundException;
import com.example.Angle.Models.Report;
import com.example.Angle.Repositories.ReportRepository;
import com.example.Angle.Services.Reports.Interfaces.ReportRetrievalInterface;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class ReportRetrievalService implements ReportRetrievalInterface {

    private final Logger logger = LogManager.getLogger(ReportRetrievalService.class);

    private final ReportRepository reportRepository;

    @Autowired
    public ReportRetrievalService(ReportRepository reportRepository){
        this.reportRepository = reportRepository;
    }
    @Override
    public Report getReport(String id) throws MediaNotFoundException {
        Report report = reportRepository.findById(id).orElse(null);
        if(report == null){
            logger.error("Report doesn't exist!");
            throw new MediaNotFoundException("Report doesn't exist");
        }
        return report;
    }
}

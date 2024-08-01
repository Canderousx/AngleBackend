package com.example.Angle.Services.Reports;

import com.example.Angle.Config.SecServices.AccountService;
import com.example.Angle.Models.Report;
import com.example.Angle.Models.ReportTypes;
import com.example.Angle.Repositories.ReportRepository;
import com.example.Angle.Services.Reports.Interfaces.ReportSaveInterface;
import org.apache.coyote.BadRequestException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class ReportSaveService implements ReportSaveInterface {

    private final Logger logger = LogManager.getLogger(ReportSaveService.class);

    private final AccountService accountService;

    private final ReportRepository reportRepository;

    @Autowired
    public ReportSaveService(AccountService accountService,
                             ReportRepository reportRepository){
        this.accountService = accountService;
        this.reportRepository = reportRepository;
    }
    @Override
    public void saveReport(String[] reportValues, ReportTypes type) throws BadRequestException {
        Report report = new Report();
        report.setReporterId(accountService.getCurrentUser().getId());
        report.setReportedAccountId(reportValues[3]);
        report.setContent(reportValues[2]);
        report.setCategory(reportValues[1]);
        report.setMediaId(reportValues[0]);
        report.setDatePublished(new Date());
        report.setResolved(false);
        report.setType(type.name());
        logger.info("Saving new report!");
        reportRepository.save(report);
    }
}

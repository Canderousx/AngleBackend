package com.example.Angle.Services.Reports;

import com.example.Angle.Config.Exceptions.MediaNotFoundException;
import com.example.Angle.Config.Models.Account;
import com.example.Angle.Config.SecServices.Account.AccountRetrievalService;
import com.example.Angle.Config.SecServices.Account.Interfaces.AccountRetrievalServiceInterface;
import com.example.Angle.Models.DTO.ReportDTO;
import com.example.Angle.Models.Report;
import com.example.Angle.Repositories.ReportRepository;
import com.example.Angle.Services.Reports.Interfaces.ReportRetrievalInterface;
import org.apache.coyote.BadRequestException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Service
public class ReportRetrievalService implements ReportRetrievalInterface {

    private final Logger logger = LogManager.getLogger(ReportRetrievalService.class);

    private final ReportRepository reportRepository;

    private final AccountRetrievalService accountRetrievalService;

    @Autowired
    public ReportRetrievalService(ReportRepository reportRepository, AccountRetrievalService accountRetrievalService) {
        this.reportRepository = reportRepository;
        this.accountRetrievalService = accountRetrievalService;
    }

    private Pageable getPageable(int page, int pageSize, String sortBy, String order){
        Pageable pageable = PageRequest.of(page,pageSize, Sort.by(sortBy).ascending());
        if(order.contains("desc")){
            pageable = PageRequest.of(page,pageSize, Sort.by(sortBy).descending());
        }
        return pageable;
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

    @Override
    public Page<ReportDTO> getUnresolved(int page, int pageSize, String sortBy, String order) {
        Pageable pageable = getPageable(page,pageSize,sortBy,order);
        return reportRepository.getUnresolved(pageable);
    }

    @Override
    public Page<ReportDTO> getMyCases(int page, int pageSize, String sortBy, String order) throws BadRequestException {
        Pageable pageable = getPageable(page,pageSize,sortBy,order);
        Account account = accountRetrievalService.getCurrentUser();
        return reportRepository.getMyCases(account.getId(),pageable);
    }

    @Override
    public Page<ReportDTO> getResolved(int page, int pageSize, String sortBy, String order) {
        Pageable pageable = getPageable(page,pageSize,sortBy,order);
        return reportRepository.getResolved(pageable);
    }

    @Override
    public List<AccountRetrievalServiceInterface.AccountRecord> getUsersInvolved(String reportId) throws MediaNotFoundException, IOException, ClassNotFoundException {
        Report report = getReport(reportId);
        Account reporter = accountRetrievalService.getUser(report.getReporterId());
        Account reported = accountRetrievalService.getMediaAuthor(report.getType(),report.getMediaId());
        List<AccountRetrievalServiceInterface.AccountRecord>involved = new ArrayList<>();
        involved.add(accountRetrievalService.generateAccountResponse(reporter));
        involved.add(accountRetrievalService.generateAccountResponse(reported));
        return involved;
    }
}

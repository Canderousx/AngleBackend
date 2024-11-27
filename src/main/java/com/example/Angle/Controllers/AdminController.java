package com.example.Angle.Controllers;


import com.example.Angle.Config.Exceptions.MediaNotFoundException;
import com.example.Angle.Config.Models.Account;
import com.example.Angle.Config.Responses.SimpleResponse;
import com.example.Angle.Config.SecServices.Account.AccountAdminService;
import com.example.Angle.Config.SecServices.Account.AccountRetrievalService;
import com.example.Angle.Config.SecServices.Account.Interfaces.AccountRetrievalServiceInterface;
import com.example.Angle.Models.DTO.ReportDTO;
import com.example.Angle.Models.Report;
import com.example.Angle.Models.ReportSolutions;
import com.example.Angle.Repositories.ReportRepository;
import com.example.Angle.Services.Comments.CommentModerationService;
import com.example.Angle.Services.Reports.ReportModerationService;
import com.example.Angle.Services.Reports.ReportRetrievalService;
import com.example.Angle.Services.Videos.VideoModerationService;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.coyote.BadRequestException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin(value = {"http://localhost:4200","http://192.168.100.36:4200","http://142.93.104.248"})
@RequestMapping("")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminController {
    private final AccountAdminService accountAdminService;

    private final ReportModerationService reportModerationService;

    private final CommentModerationService commentModerationService;


    private final VideoModerationService videoModerationService;

    private final ReportRepository reportRepository;

    private final AccountRetrievalService accountRetrievalService;

    private final ReportRetrievalService reportRetrievalService;

    private final Logger logger = LogManager.getLogger(AdminController.class);


    @Autowired
    public AdminController(AccountAdminService accountAdminService,
                           ReportModerationService reportModerationService,
                           CommentModerationService commentModerationService,
                           VideoModerationService videoModerationService,
                           ReportRepository reportRepository,
                           AccountRetrievalService accountRetrievalService,
                           ReportRetrievalService reportRetrievalService) {
        this.accountAdminService = accountAdminService;
        this.reportModerationService = reportModerationService;
        this.commentModerationService = commentModerationService;
        this.videoModerationService = videoModerationService;
        this.reportRepository = reportRepository;
        this.accountRetrievalService = accountRetrievalService;
        this.reportRetrievalService = reportRetrievalService;
    }

    @RequestMapping(value = "/banAccount",method = RequestMethod.POST)
    public ResponseEntity<SimpleResponse> banAccount(@RequestBody String reason,
                                                     @RequestParam String accountId,
                                                     @RequestParam String reportId) throws MediaNotFoundException, BadRequestException {
        this.accountAdminService.banAccount(accountId);
        this.reportModerationService.solveReport(ReportSolutions.ACCOUNT_BANNED,reason,reportId);
        return ResponseEntity.ok(new SimpleResponse("Report has been solved! Good work!"));
    }

    @RequestMapping(value = "/banVideo",method = RequestMethod.POST)
    public ResponseEntity<SimpleResponse> banVideo(@RequestBody String reason,
                                                   @RequestParam String videoId,
                                                   @RequestParam String reportId) throws MediaNotFoundException, BadRequestException {
        this.videoModerationService.banVideo(videoId);
        this.reportModerationService.solveReport(ReportSolutions.MEDIA_BANNED,reason,reportId);
        return ResponseEntity.ok(new SimpleResponse("Report has been solved! Good work!"));
    }
    @RequestMapping(value = "/banComment",method = RequestMethod.POST)
    public ResponseEntity<SimpleResponse> banComment(@RequestBody String reason,
                                                     @RequestParam String commentId,
                                                     @RequestParam String reportId) throws MediaNotFoundException, BadRequestException {
        this.commentModerationService.banComment(commentId);
        this.reportModerationService.solveReport(ReportSolutions.MEDIA_BANNED,reason,reportId);
        return ResponseEntity.ok(new SimpleResponse("Report has been solved! Good work!"));
    }
    @RequestMapping(value = "/cancelReport",method = RequestMethod.POST)
    public ResponseEntity<SimpleResponse> cancelReport(@RequestBody String reason,
                                                       @RequestParam String reportId) throws MediaNotFoundException, BadRequestException {
        this.reportModerationService.solveReport(ReportSolutions.CANCELED,reason,reportId);
        return ResponseEntity.ok(new SimpleResponse("Report has been solved! Good work!"));
    }

    @RequestMapping(value = "report/howManyUnresolved",method = RequestMethod.GET)
    public int howManyUnresolved(){
        return reportModerationService.howManyUnresolved();
    }

    @RequestMapping(value = "report/getUnresolved",method = RequestMethod.GET)
    public Page<ReportDTO> getUnresolved(@RequestParam int page,
                                         @RequestParam int pageSize,
                                         @RequestParam String sortBy,
                                         @RequestParam String order,
                                         HttpServletResponse response){
        Pageable pageable = PageRequest.of(page,pageSize, Sort.by(sortBy).ascending());
        if(order.contains("desc")){
            pageable = PageRequest.of(page,pageSize, Sort.by(sortBy).descending());
        }
        response.setHeader("totalReports",String.valueOf(reportRepository.countUnresolvedReports()));
        return reportRepository.getUnresolved(pageable);
    }

    @RequestMapping(value = "report/getMyCases",method = RequestMethod.GET)
    public Page<ReportDTO>getMyCases(@RequestParam int page,
                                     @RequestParam int pageSize,
                                     @RequestParam String sortBy,
                                     @RequestParam String order,
                                     HttpServletResponse response) throws BadRequestException {
        Account account = accountRetrievalService.getCurrentUser();
        Pageable pageable = PageRequest.of(page,pageSize, Sort.by(sortBy).ascending());
        if(order.contains("desc")){
            pageable = PageRequest.of(page,pageSize, Sort.by(sortBy).descending());
        }
        response.setHeader("totalReports",String.valueOf(reportRepository.countMyCases(account.getId())));
        Page<ReportDTO> myCases = reportRepository.getMyCases(account.getId(),pageable);
        logger.info("USER CASES COUNT: "+myCases.getTotalElements());
        return myCases;
    }

    @RequestMapping(value = "report/getResolved",method = RequestMethod.GET)
    public Page<ReportDTO>getResolved(@RequestParam int page,
                                      @RequestParam int pageSize,
                                      @RequestParam String sortBy,
                                      @RequestParam String order,
                                      HttpServletResponse response){
        Pageable pageable = PageRequest.of(page,pageSize, Sort.by(sortBy).ascending());
        if(order.contains("desc")){
            pageable = PageRequest.of(page,pageSize, Sort.by(sortBy).descending());
        }
        response.setHeader("totalReports",String.valueOf(reportRepository.countResolved()));
        Page<ReportDTO> myCases = reportRepository.getResolved(pageable);
        logger.info("USER CASES COUNT: "+myCases.getTotalElements());
        return myCases;
    }
    @RequestMapping(value = "report/getUsersInvolved",method = RequestMethod.GET)
    public List<AccountRetrievalServiceInterface.AccountRecord>getUsersInvolved(@RequestParam String id) throws IOException, ClassNotFoundException, MediaNotFoundException {
        Report report = reportRetrievalService.getReport(id);
        Account reporter = accountRetrievalService.getUser(report.getReporterId());
        Account reported = accountRetrievalService.getMediaAuthor(report.getType(),report.getMediaId());
        List<AccountRetrievalServiceInterface.AccountRecord>involved = new ArrayList<>();
        involved.add(accountRetrievalService.generateAccountResponse(reporter));
        involved.add(accountRetrievalService.generateAccountResponse(reported));
        return involved;
    }






}

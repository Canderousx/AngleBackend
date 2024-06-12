package com.example.Angle.Controllers.Auth;


import com.example.Angle.Config.Exceptions.MediaNotFoundException;
import com.example.Angle.Config.Models.Account;
import com.example.Angle.Config.Models.AccountRes;
import com.example.Angle.Config.Models.UserRole;
import com.example.Angle.Config.Responses.SimpleResponse;
import com.example.Angle.Config.SecRepositories.AccountRepository;
import com.example.Angle.Config.SecServices.AccountService;
import com.example.Angle.Models.Comment;
import com.example.Angle.Models.DTO.ReportDTO;
import com.example.Angle.Models.Report;
import com.example.Angle.Models.ReportCategories;
import com.example.Angle.Models.ReportTypes;
import com.example.Angle.Repositories.ReportRepository;
import com.example.Angle.Services.ImageService;
import com.example.Angle.Services.ReportService;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.coyote.BadRequestException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Role;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@RestController
@RequestMapping("/auth/report")
@CrossOrigin(value = {"http://localhost:4200","http://192.168.100.36:4200"})
public class ReportsController {

    @Autowired
    AccountService accountService;

    @Autowired
    ReportRepository reportRepository;



    private final Logger logger = LogManager.getLogger(ReportsController.class);

    @Autowired
    private ReportService reportService;

    @RequestMapping(value = "/getCategories",method = RequestMethod.GET)
    public List<String>getReportCategories(){
        return ReportCategories.getAll();
    }






    @RequestMapping(value = "/{type}",method = RequestMethod.POST)
    public ResponseEntity<SimpleResponse>addReport(@PathVariable String type,@RequestBody String[] reportValues) throws BadRequestException {
        if(reportValues.length != 4){
            throw new BadRequestException("Received report values are invalid!");
        }
        ReportTypes reportType;
        try{
            reportType = ReportTypes.valueOf(type.toUpperCase());
        }catch (IllegalArgumentException e){
            throw new BadRequestException("Invalid report type!");
        }
        reportService.addReport(reportValues,reportType);
        return ResponseEntity.ok(new SimpleResponse("Report has been sent. Thank you!"));
    }

    @RequestMapping(value = "/howManyUnresolved",method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public int howManyUnresolved(){
        return reportService.howManyUnresolved();
    }

    @RequestMapping(value = "/getUnresolved",method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public List<ReportDTO>getUnresolved(@RequestParam int page,
                                        @RequestParam int pageSize,
                                        @RequestParam String sortBy,
                                        @RequestParam String order,
                                        HttpServletResponse response){
        Pageable pageable = PageRequest.of(page,pageSize, Sort.by(sortBy).ascending());
        if(order.contains("desc")){
            pageable = PageRequest.of(page,pageSize, Sort.by(sortBy).descending());
        }
        response.setHeader("totalReports",String.valueOf(reportRepository.countUnresolvedReports()));
        return reportRepository.getUnresolved(pageable).stream().toList();
    }

    @RequestMapping(value = "/getMyCases",method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public List<ReportDTO>getMyCases(@RequestParam int page,
                                     @RequestParam int pageSize,
                                     @RequestParam String sortBy,
                                     @RequestParam String order,
                                     HttpServletResponse response) throws BadRequestException {
        Account account = accountService.getCurrentUser();
        Pageable pageable = PageRequest.of(page,pageSize, Sort.by(sortBy).ascending());
        if(order.contains("desc")){
            pageable = PageRequest.of(page,pageSize, Sort.by(sortBy).descending());
        }
        response.setHeader("totalReports",String.valueOf(reportRepository.countMyCases(account.getId())));
        List<ReportDTO> myCases = reportRepository.getMyCases(account.getId(),pageable).stream().toList();
        logger.info("USER CASES COUNT: "+myCases.size());
        return myCases;
    }

    @RequestMapping(value = "/getResolved",method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public List<ReportDTO>getResolved(@RequestParam int page,
                                     @RequestParam int pageSize,
                                     @RequestParam String sortBy,
                                     @RequestParam String order,
                                     HttpServletResponse response){
        Pageable pageable = PageRequest.of(page,pageSize, Sort.by(sortBy).ascending());
        if(order.contains("desc")){
            pageable = PageRequest.of(page,pageSize, Sort.by(sortBy).descending());
        }
        response.setHeader("totalReports",String.valueOf(reportRepository.countResolved()));
        List<ReportDTO> myCases = reportRepository.getResolved(pageable).stream().toList();
        logger.info("USER CASES COUNT: "+myCases.size());
        return myCases;
    }



    @RequestMapping(value = "/getUsersInvolved",method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public List<AccountRes>getUsersInvolved(@RequestParam String id) throws IOException, ClassNotFoundException, MediaNotFoundException {
        Report report = reportService.getReport(id);
        Account reporter = accountService.getUser(report.getReporterId());
        Account reported = accountService.getMediaAuthor(report.getType(),report.getMediaId());
        List<AccountRes>involved = new ArrayList<>();
        logger.info("USERS INVOLVED: "+reported.getUsername()+" AND "+reporter.getUsername());
        involved.add(accountService.generateAccountResponse(reporter));
        involved.add(accountService.generateAccountResponse(reported));
        return involved;
    }



}

package com.example.Angle.Services;


import com.example.Angle.Config.Exceptions.MediaNotFoundException;
import com.example.Angle.Config.Models.Account;
import com.example.Angle.Config.SecServices.AccountService;
import com.example.Angle.Models.DTO.ReportDTO;
import com.example.Angle.Models.Report;
import com.example.Angle.Models.ReportCategories;
import com.example.Angle.Models.ReportSolutions;
import com.example.Angle.Models.ReportTypes;
import com.example.Angle.Repositories.ReportRepository;
import com.example.Angle.Services.Comments.CommentModerationServiceImpl;
import org.apache.coyote.BadRequestException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;


@Service
public class ReportService {

    private final Logger logger = LogManager.getLogger(ReportService.class);

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private AccountService accountService;

    @Autowired
    private VideoService videoService;

    @Autowired
    private CommentModerationServiceImpl commentModerationServiceImpl;


    public Report getReport(String reportId) throws MediaNotFoundException {
        Report report = reportRepository.findById(reportId).orElse(null);
        if(report == null){
            logger.error("Report doesn't exists!");
            throw new MediaNotFoundException("Internal Server Error");
        }
        return report;
    }


    public void solveReport(ReportSolutions solution, String reason, String reportId) throws MediaNotFoundException, BadRequestException {
        Report report = reportRepository.findById(reportId).orElse(null);
        if(report == null){
            throw new MediaNotFoundException("Report doesn't exist!");
        }
        if(report.getSolution() != null){
            if(report.getSolution().equals(ReportSolutions.MEDIA_BANNED.name()) && !solution.equals(ReportSolutions.ACCOUNT_BANNED.name())){
                logger.info("UNBANNING MEDIA ID: "+report.getMediaId());
                if(report.getType().equals(ReportTypes.VIDEO.name())){
                    videoService.unbanVideo(report.getMediaId());
                    logger.info("Video has been unbanned!");
                }
                if(report.getType().equals(ReportTypes.COMMENT.name())){
                    commentModerationServiceImpl.unbanComment(report.getMediaId());
                    logger.info("Comment has been unbanned!");
                }
            }
            if(report.getSolution().equals(ReportSolutions.ACCOUNT_BANNED.name()) && !solution.equals(ReportSolutions.ACCOUNT_BANNED.name())){
                logger.info("UNBANNING ACCOUNT ID: "+report.getMediaId());
                accountService.unbanAccount(report.getReportedAccountId());
                logger.info("Account has been unbanned!");
            }
        }

        Account account = accountService.getCurrentUser();
        report.setSolution(solution.name());
        report.setReason(reason);
        report.setResolved(true);
        report.setResolvedBy(account.getId());
        report.setDateResolved(new Date());
        reportRepository.save(report);
        logger.info("Report {"+reportId+"} has been resolved!");
    }



    public void addReport(String[]reportValues,ReportTypes type) throws BadRequestException {
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

    private byte[] stringToByte (String string){
        String[] byteValues = string.split(",");
        byte[] byteArray = new byte[byteValues.length];
        for(int i = 0; i < byteArray.length; i ++){
            byteArray[i] = Byte.parseByte(byteValues[i].trim());
        }
        return byteArray;
    }

//    public List<ReportDTO>processReports(List<ReportDTO> reports){
//        reports.forEach(report ->{
//            report.(String.nameStringFromBytes(stringToByte(report.getId())));
//        });
//    }

    public int howManyUnresolved(){
        return reportRepository.howManyUnresolved().get(0);
    }
}

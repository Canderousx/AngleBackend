package com.example.Angle.Services.Reports;

import com.example.Angle.Config.Exceptions.MediaNotFoundException;
import com.example.Angle.Config.Models.Account;
import com.example.Angle.Config.SecServices.Account.AccountAdminService;
import com.example.Angle.Config.SecServices.Account.AccountRetrievalService;
import com.example.Angle.Models.Report;
import com.example.Angle.Models.ReportSolutions;
import com.example.Angle.Models.ReportTypes;
import com.example.Angle.Repositories.ReportRepository;
import com.example.Angle.Services.Comments.CommentModerationService;
import com.example.Angle.Services.Reports.Interfaces.ReportModerationInterface;
import com.example.Angle.Services.Videos.VideoModerationService;
import org.apache.coyote.BadRequestException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;


@Service
public class ReportModerationService implements ReportModerationInterface {

    private final ReportRepository reportRepository;

    private final Logger logger = LogManager.getLogger(ReportModerationService.class);

    private final CommentModerationService commentModerationService;

    private final AccountAdminService accountAdminService;

    private final AccountRetrievalService accountRetrievalService;

    private final VideoModerationService videoModerationService;

    @Autowired
    public ReportModerationService(ReportRepository reportRepository,
                                   CommentModerationService commentModerationService,
                                   AccountAdminService accountAdminService,
                                   VideoModerationService videoModerationService,
                                   AccountRetrievalService accountRetrievalService) {
        this.reportRepository = reportRepository;
        this.videoModerationService = videoModerationService;
        this.commentModerationService = commentModerationService;
        this.accountAdminService = accountAdminService;
        this.accountRetrievalService = accountRetrievalService;
    }

    @Override
    public void solveReport(ReportSolutions solution, String reason, String reportId) throws MediaNotFoundException, BadRequestException {
        Report report = reportRepository.findById(reportId).orElse(null);
        if(report == null){
            throw new MediaNotFoundException("Report doesn't exist!");
        }
        if(report.getSolution() != null){
            if(report.getSolution().equals(ReportSolutions.MEDIA_BANNED.name()) && !solution.equals(ReportSolutions.ACCOUNT_BANNED.name())){
                logger.info("UNBANNING MEDIA ID: "+report.getMediaId());
                if(report.getType().equals(ReportTypes.VIDEO.name())){
                    videoModerationService.unbanVideo(report.getMediaId());
                    logger.info("Video has been unbanned!");
                }
                if(report.getType().equals(ReportTypes.COMMENT.name())){
                    commentModerationService.unbanComment(report.getMediaId());
                    logger.info("Comment has been unbanned!");
                }
            }
            if(report.getSolution().equals(ReportSolutions.ACCOUNT_BANNED.name()) && !solution.equals(ReportSolutions.ACCOUNT_BANNED.name())){
                logger.info("UNBANNING ACCOUNT ID: "+report.getMediaId());
                accountAdminService.unbanAccount(report.getReportedAccountId());
                logger.info("Account has been unbanned!");
            }
        }

        Account account = accountRetrievalService.getCurrentUser();
        report.setSolution(solution.name());
        report.setReason(reason);
        report.setResolved(true);
        report.setResolvedBy(account.getId());
        report.setDateResolved(new Date());
        reportRepository.save(report);
        logger.info("Report {"+reportId+"} has been resolved!");
    }

    @Override
    public int howManyUnresolved() {
        return reportRepository.howManyUnresolved().get(0);
    }
}

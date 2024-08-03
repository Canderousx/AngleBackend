package com.example.Angle.Controllers.Auth;


import com.example.Angle.Config.Exceptions.MediaNotFoundException;
import com.example.Angle.Config.Responses.SimpleResponse;
import com.example.Angle.Config.SecServices.Account.AccountAdminService;
import com.example.Angle.Models.ReportSolutions;
import com.example.Angle.Services.Comments.CommentModerationService;
import com.example.Angle.Services.Reports.ReportModerationService;
import com.example.Angle.Services.Videos.VideoModerationService;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(value = {"http://localhost:4200","http://192.168.100.36:4200"})
@RequestMapping("/auth")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminController {
    private final AccountAdminService accountAdminService;

    private final ReportModerationService reportModerationService;

    private final CommentModerationService commentModerationService;


    private final VideoModerationService videoModerationService;


    @Autowired
    public AdminController(AccountAdminService accountAdminService,
                           ReportModerationService reportModerationService,
                           CommentModerationService commentModerationService,
                           VideoModerationService videoModerationService){
        this.videoModerationService = videoModerationService;
        this.accountAdminService = accountAdminService;
        this.reportModerationService = reportModerationService;
        this.commentModerationService = commentModerationService;
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






}

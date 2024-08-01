package com.example.Angle.Controllers.Auth;


import com.example.Angle.Config.Exceptions.MediaNotFoundException;
import com.example.Angle.Config.Responses.SimpleResponse;
import com.example.Angle.Config.SecServices.AccountService;
import com.example.Angle.Models.ReportSolutions;
import com.example.Angle.Services.Comments.CommentModerationServiceImpl;
import com.example.Angle.Services.ReportService;
import com.example.Angle.Services.VideoService;
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

    private final VideoService videoService;

    private final AccountService accountService;

    private final ReportService reportService;

    private final CommentModerationServiceImpl commentModerationService;

    @Autowired
    public AdminController(VideoService videoService,
                           AccountService accountService,
                           ReportService reportService,
                           CommentModerationServiceImpl commentModerationServiceImpl){
        this.videoService = videoService;
        this.accountService = accountService;
        this.reportService = reportService;
        this.commentModerationService = commentModerationServiceImpl;
    }

    @RequestMapping(value = "/banAccount",method = RequestMethod.POST)
    public ResponseEntity<SimpleResponse> banAccount(@RequestBody String reason,
                                                     @RequestParam String accountId,
                                                     @RequestParam String reportId) throws MediaNotFoundException, BadRequestException {
        this.accountService.banAccount(accountId);
        this.reportService.solveReport(ReportSolutions.ACCOUNT_BANNED,reason,reportId);
        return ResponseEntity.ok(new SimpleResponse("Report has been solved! Good work!"));
    }

    @RequestMapping(value = "/banVideo",method = RequestMethod.POST)
    public ResponseEntity<SimpleResponse> banVideo(@RequestBody String reason,
                                                   @RequestParam String videoId,
                                                   @RequestParam String reportId) throws MediaNotFoundException, BadRequestException {
        this.videoService.banVideo(videoId);
        this.reportService.solveReport(ReportSolutions.MEDIA_BANNED,reason,reportId);
        return ResponseEntity.ok(new SimpleResponse("Report has been solved! Good work!"));
    }
    @RequestMapping(value = "/banComment",method = RequestMethod.POST)
    public ResponseEntity<SimpleResponse> banComment(@RequestBody String reason,
                                                     @RequestParam String commentId,
                                                     @RequestParam String reportId) throws MediaNotFoundException, BadRequestException {
        this.commentModerationService.banComment(commentId);
        this.reportService.solveReport(ReportSolutions.MEDIA_BANNED,reason,reportId);
        return ResponseEntity.ok(new SimpleResponse("Report has been solved! Good work!"));
    }
    @RequestMapping(value = "/cancelReport",method = RequestMethod.POST)
    public ResponseEntity<SimpleResponse> cancelReport(@RequestBody String reason,
                                                       @RequestParam String reportId) throws MediaNotFoundException, BadRequestException {
        this.reportService.solveReport(ReportSolutions.CANCELED,reason,reportId);
        return ResponseEntity.ok(new SimpleResponse("Report has been solved! Good work!"));
    }






}

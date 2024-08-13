package com.example.Angle.Controllers;


import com.example.Angle.Config.Responses.SimpleResponse;
import com.example.Angle.Models.ReportCategories;
import com.example.Angle.Models.ReportTypes;
import com.example.Angle.Services.Reports.ReportSaveService;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController
@RequestMapping("/report")
@CrossOrigin(value = {"http://localhost:4200","http://192.168.100.36:4200","http://142.93.104.248"})
public class ReportController {
    private final  ReportSaveService reportSaveService;


    @Autowired
    public ReportController(ReportSaveService reportSaveService) {
        this.reportSaveService = reportSaveService;
    }

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
        reportSaveService.saveReport(reportValues,reportType);
        return ResponseEntity.ok(new SimpleResponse("Report has been sent. Thank you!"));
    }









}

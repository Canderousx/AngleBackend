package com.example.Angle.Models.DTO;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;


public interface ReportDTO {

    String getId();

    String getReporterId();

    String getContent();

    String getReporter();

    String getReportedAccountId();

    String getType();

    String getMediaId();

    String getCategory();

    Date getDatePublished();

    boolean getResolved();

    String getResolvedBy();

    String getSolution();

    String getReason();
    Date getDateResolved();




}

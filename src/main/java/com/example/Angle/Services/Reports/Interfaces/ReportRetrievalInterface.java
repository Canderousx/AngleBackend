package com.example.Angle.Services.Reports.Interfaces;

import com.example.Angle.Config.Exceptions.MediaNotFoundException;
import com.example.Angle.Config.SecServices.Account.Interfaces.AccountRetrievalServiceInterface;
import com.example.Angle.Models.DTO.ReportDTO;
import com.example.Angle.Models.Report;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;

import java.io.IOException;
import java.util.List;

public interface ReportRetrievalInterface {

    Report getReport(String id) throws MediaNotFoundException;

    Page<ReportDTO>getUnresolved(int page, int pageSize, String sortBy, String order);

    Page<ReportDTO>getMyCases(int page, int pageSize, String sortBy, String order) throws BadRequestException;

    Page<ReportDTO>getResolved(int page, int pageSize, String sortBy, String order);

    List<AccountRetrievalServiceInterface.AccountRecord>getUsersInvolved(String reportId) throws MediaNotFoundException, IOException, ClassNotFoundException;
}

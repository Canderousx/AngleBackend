package com.example.Angle.Repositories;


import com.example.Angle.Models.DTO.ReportDTO;
import com.example.Angle.Models.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface ReportRepository extends JpaRepository<Report, String> {


    @Override
    Optional<Report> findById(String id);

    List<Report>findByReporterId(String reporterId);

    List<Report>findByResolvedIsFalse();

    List<Report>findByResolvedIsTrue();

    List<Report>findByCategory(String category);


    @Query(value = "SELECT r.* FROM report r WHERE r.category = :category AND r.resolved = false",nativeQuery = true)
    List<Report>byCategoryUnresolved(@Param("category")String category);

    @Query(value = "SELECT r.* FROM report r WHERE r.category = :category AND r.resolved = true",nativeQuery = true)
    List<Report>byCategoryResolved(@Param("category")String category);

    @Query(value = "SELECT count(*) FROM report WHERE resolved = false",nativeQuery = true)
    List<Integer> howManyUnresolved();



    @Query(value = "SELECT r.id AS id, r.reporter_Id AS reporterId, r.reported_account_id AS reportedAccountId, r.content AS content, a.username AS reporter, r.type AS type, r.media_id AS mediaId, r.category AS category, r.date_published AS datePublished, r.resolved AS resolved, r.resolved_by AS resolvedBy, r.date_resolved AS dateResolved, r.solution AS solution FROM report r JOIN account a ON r.reporter_Id = a.id WHERE r.resolved = false", nativeQuery = true)
    Page<ReportDTO> getUnresolved(Pageable pageable);


    @Query(value = "SELECT r.id AS id, r.reporter_id AS reporterId, r.reported_account_id AS reportedAccountId, r.content AS content, a1.username AS reporter, r.type AS type, r.media_id AS mediaId, r.category AS category, r.date_published AS datePublished, r.resolved AS resolved, a2.username AS resolvedBy, r.date_resolved AS dateResolved, r.solution AS solution, r.reason AS reason " +
            "FROM report r " +
            "JOIN account a1 ON r.reporter_id = a1.id " +
            "JOIN account a2 ON r.resolved_by = a2.id " +
            "WHERE r.resolved_by = :id", nativeQuery = true)
    Page<ReportDTO>getMyCases(@Param("id")String id,Pageable pageable);

    @Query(value = "SELECT count(*) FROM report WHERE resolved = false", nativeQuery = true)
    int countUnresolvedReports();

    @Query(value = "SELECT count(*) FROM report WHERE resolved = true", nativeQuery = true)
    int countSolvedReports();

    @Query(value = "SELECT count(*) FROM report WHERE resolved_by = :id", nativeQuery = true)
    int countMyCases(@Param("id")String id);

    @Query(value = "SELECT count(*) FROM report WHERE solution IS NOT NULL",nativeQuery = true)
    int countResolved();

    @Query(value = "SELECT r.id AS id, r.reporter_id AS reporterId, r.reported_account_id AS reportedAccountId, r.content AS content, a1.username AS reporter, r.type AS type, r.media_id AS mediaId, r.category AS category, r.date_published AS datePublished, r.resolved AS resolved, a2.username AS resolvedBy, r.date_resolved AS dateResolved, r.solution AS solution, r.reason AS reason " +
            "FROM report r " +
            "JOIN account a1 ON r.reporter_id = a1.id " +
            "JOIN account a2 ON r.resolved_by = a2.id " +
            "WHERE solution IS NOT NULL", nativeQuery = true)
    Page<ReportDTO>getResolved(Pageable pageable);


}

package com.demo.repository;

import com.demo.entity.ExcelData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

@Repository
public interface ExcelDataRepository extends JpaRepository<ExcelData, Long> {
    @Query(value = "SELECT * FROM excel_data WHERE CAST(excel_id AS UNSIGNED) BETWEEN CAST(:startId AS UNSIGNED) AND CAST(:endId AS UNSIGNED)", nativeQuery = true)
    List<ExcelData> findByExcelIdInRange(@Param("startId") String startId, @Param("endId") String endId);
}

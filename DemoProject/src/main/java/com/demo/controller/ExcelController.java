package com.demo.controller;

import com.demo.service.ExcelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.util.Map;

@RestController
@RequestMapping("/api/excel")
@CrossOrigin("*")
public class ExcelController {

    @Autowired
    private ExcelService excelService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadExcel(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Please select a file to upload."));
        }

        try {
            excelService.saveExcelData(file);
            return ResponseEntity.ok(Map.of("message", "File uploaded and data stored successfully!"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to process Excel file: " + e.getMessage()));
        }
    }

    @PostMapping("/send-sms")
    public ResponseEntity<?> sendSms(@RequestBody Map<String, String> request) {
        String startId = request.get("startId");
        String endId = request.get("endId");

        if (startId == null || endId == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Start ID and End ID are required."));
        }

        try {
            int sentCount = excelService.sendSmsInRange(startId, endId);
            return ResponseEntity
                    .ok(Map.of("message", "SMS sending process completed. Sent to " + sentCount + " records."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to send SMS: " + e.getMessage()));
        }
    }

    @GetMapping("/download-logs")
    public ResponseEntity<byte[]> downloadLogs() {
        byte[] csvData = excelService.exportSmsLogsToCsv();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=sms_logs.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvData);
    }
}

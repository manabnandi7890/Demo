package com.demo.service;

import com.demo.entity.ExcelData;
import com.demo.entity.SmsLog;
import com.demo.repository.ExcelDataRepository;
import com.demo.repository.SmsLogRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.web.client.RestTemplate;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class ExcelService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ExcelDataRepository repository;

    @Autowired
    private SmsLogRepository smsLogRepository;

    public void saveExcelData(MultipartFile file) throws IOException {
        List<ExcelData> excelDataList = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            // Skip header row
            if (rows.hasNext()) {
                rows.next();
            }

            while (rows.hasNext()) {
                Row currentRow = rows.next();
                ExcelData excelData = new ExcelData();

                excelData.setExcelId(getCellValueAsString(currentRow.getCell(0)));
                excelData.setName(getCellValueAsString(currentRow.getCell(1)));
                excelData.setEmail(getCellValueAsString(currentRow.getCell(2)));
                excelData.setPhone(getCellValueAsString(currentRow.getCell(3)));
                excelData.setCity(getCellValueAsString(currentRow.getCell(4)));

                excelDataList.add(excelData);
            }
        }

        repository.saveAll(excelDataList);
    }

    public int sendSmsInRange(String startId, String endId) {
        List<ExcelData> dataList = repository.findByExcelIdInRange(startId, endId);
        int sentCount = 0;

        for (ExcelData data : dataList) {
            if (data.getPhone() != null && !data.getPhone().isEmpty()) {
            	String message = "Dear Customer, Your One Time Password is 12345. CMT";
                try {
                    String encodedMsg = URLEncoder.encode(message, StandardCharsets.UTF_8);

                    String url = String.format(
                            "https://sms.cell24x7.com/otpReceiver/sendSMS?user=testdemo&pwd=apidemo&sender=CMTLTD&mobile=%s&msg=%s&mt=0",
                            data.getPhone(), encodedMsg);

                    restTemplate.getForObject(url, String.class);
                    sentCount++;

                    // Log the SMS
                    SmsLog log = new SmsLog(data.getExcelId(), data.getPhone(), message, "SUCCESS",
                            java.time.LocalDateTime.now());
                    smsLogRepository.save(log);
                } catch (Exception e) {
                    System.err.println("Failed to send SMS to " + data.getPhone() + ": " + e.getMessage());
                    // Log the failure
                    SmsLog log = new SmsLog(data.getExcelId(), data.getPhone(),
                    		message, "FAILED: " + e.getMessage(),
                            java.time.LocalDateTime.now());
                    smsLogRepository.save(log);
                }
            }
        }
        return sentCount;
    }

    public byte[] exportSmsLogsToCsv() {
        StringBuilder csvContent = new StringBuilder();
        csvContent.append("ID,Excel ID,Mobile,Message,Status,Timestamp\n");

        List<SmsLog> logs = smsLogRepository.findAll();
        for (SmsLog log : logs) {
            csvContent.append(String.format("%d,%s,%s,\"%s\",%s,%s\n",
                    log.getId(), log.getExcelId(), log.getMobile(),
                    log.getMessage().replace("\"", "\"\""),
                    log.getStatus(), log.getSentAt().toString()));
        }

        return csvContent.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf((long) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }
}

package lk.mobitel.msmsenterprisewebbeapi.controller;
import lk.mobitel.msmsenterprisewebbeapi.service.LoggingService;
import lk.mobitel.msmsenterprisewebbeapi.service.SentMessageService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
public class SentMessageController {
    @Autowired
    SentMessageService sentMessageService;
    @Autowired
    LoggingService loggingService;

    @GetMapping("/getAllSentItems")
    public ResponseEntity<List<?>> getAllSentItems(@RequestHeader HttpHeaders headers) {
        int customer = Integer.parseInt(Objects.requireNonNull(headers.getFirst("customer")));
        int userId = Integer.parseInt(Objects.requireNonNull(headers.getFirst("userId")));
        int userType = Integer.parseInt(Objects.requireNonNull(headers.getFirst("userType")));
        String userName = Objects.requireNonNull(headers.getFirst("userName"));
        String sessionId = Objects.requireNonNull(headers.getFirst("sessionId"));

        loggingService.logActivity("", sessionId, customer, userName, "getAllSentItems", "Received request with data");

        List<?> response = sentMessageService.getAllSentItems(customer, userId, userType);

        if (response.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
        } else {
            loggingService.logActivity("", sessionId, customer, userName, "getAllSentItems", "Returning list");
            return ResponseEntity.ok(response);
        }
    }

    @PostMapping("/getSentItemsWithinDates")
    public ResponseEntity<?> getSentItemsWithinDates(@RequestBody DownloadRequest downloadRequest) {
        String fromDateStr = downloadRequest.getFromDate() + " 00:00:00.000000";
        String toDateStr = downloadRequest.getToDate() + " 00:00:00.000000";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");
        LocalDateTime fromDate = LocalDateTime.parse(fromDateStr, formatter);
        LocalDateTime toDate = LocalDateTime.parse(toDateStr, formatter);
        Integer customer = downloadRequest.getCustomer();
        Integer userType = downloadRequest.getUserType();
        Integer userId = downloadRequest.getUserId();
        String userName = downloadRequest.getUserName();
        String sessionId = downloadRequest.getSessionId();

        loggingService.logActivity("", sessionId, customer, userName, "getSentItemsWithinDates", "Received request with data");

        Integer TotalItems = sentMessageService.download_sent_items_count(fromDate, toDate, customer, userType, userId);
        if (TotalItems == 0){
            return ResponseEntity.status(300).body("EMPTY RESULT");
        } else {
            List<Map<String, Object>> result = sentMessageService.download_sent_items(fromDate, toDate, customer, userType,userId);

            loggingService.logActivity("", sessionId, customer, userName, "getSentItemsWithinDates", "Returning list");

            return ResponseEntity.ok(result);
        }
    }

    @PostMapping("/downloadSentItemsWithinDates")
    public ResponseEntity<?> downloadSentItemsWithinDates(@RequestBody DownloadRequest downloadRequest) {
        String fromDateStr = downloadRequest.getFromDate() + " 00:00:00.000000";
        String toDateStr = downloadRequest.getToDate() + " 00:00:00.000000";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");
        LocalDateTime fromDate = LocalDateTime.parse(fromDateStr, formatter);
        LocalDateTime toDate = LocalDateTime.parse(toDateStr, formatter);
        Integer customer = downloadRequest.getCustomer();
        Integer userType = downloadRequest.getUserType();
        Integer userId = downloadRequest.getUserId();
        String userName = downloadRequest.getUserName();
        String sessionId = downloadRequest.getSessionId();

        loggingService.logActivity("", sessionId, customer, userName, "downloadSentItemsWithinDates", "Received request with data");

        Integer TotalItems = sentMessageService.download_sent_items_count(fromDate, toDate, customer, userType, userId);
        if (TotalItems == 0){
            return ResponseEntity.status(300).body("EMPTY RESULT");
        } else {
            List<Map<String, Object>> result = sentMessageService.download_sent_items(fromDate, toDate, customer, userType, userId);
            try {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                OutputStreamWriter writer = new OutputStreamWriter(outputStream);

                // Write CSV data to the output stream
                writer.write("Recipient" + "," + "Sender_Alias" + "," + "Message" + "," + "Message_Type" + "," + "Time" + "\n");
                for (Map<String, Object> entry : result) {
                    writer.write(entry.get("recipient") + "," + entry.get("alias") + "," + entry.get("message") + "," + entry.get("type") + "," + entry.get("timeSent") + "\n");
                }
                writer.flush();

                // Set headers for file download
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                headers.setContentDispositionFormData("attachment", "sentItems_report.csv");

                loggingService.logActivity("", sessionId, customer, userName, "downloadSentItemsWithinDates", "Returning data");

                return new ResponseEntity<>(outputStream.toByteArray(), headers, HttpStatus.OK);
            } catch (IOException e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to generate file: " + e.getMessage());
            }
        }
    }

    @PostMapping("/downloadMonthlyReport")
    public ResponseEntity<?> downloadSentItemsFullReport(@RequestBody DownloadRequest downloadRequest){
        Integer customer = downloadRequest.getCustomer();
        String monthDate = downloadRequest.getMonthDate();
        Integer userType = downloadRequest.getUserType();
        String userName = downloadRequest.getUserName();
        String sessionId = downloadRequest.getSessionId();

        loggingService.logActivity("", sessionId, customer, userName, "downloadMonthlyReport", "Received request with data");

        Map<String, Object> result = new HashMap<>();
        if (userType == 0 || userType == 101) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
            LocalDate parsedMonth = LocalDate.parse(monthDate.substring(0, 7), DateTimeFormatter.ofPattern("yyyy-MM"));

            // Calculate start date (first day of previous month)
            LocalDate startDate = parsedMonth.minusMonths(1).withDayOfMonth(1);
            // Calculate end date (last day of specified month)
            LocalDate endDate = parsedMonth.withDayOfMonth(parsedMonth.lengthOfMonth());

            String year = String.valueOf(endDate.getYear());
            String month = endDate.getMonth().toString();

            String fileDownload = "https://msmsenterprise.mobitel.lk/mSMSReports/ESMSMonthlyReports/" +
                    year + "/" + month + "/ESMS_" + customer + "_" + startDate + "_" + endDate + ".zip";

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.getForEntity(fileDownload, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                result.put("status", "SUCCESS");
                result.put("response", fileDownload);
                result.put("year", year);
                result.put("month", month);
            } else {
                result.put("status", "FAILED");
                result.put("response", "File is not available!");
                result.put("file", false);
                result.put("endDate", fileDownload);
            }
        } else {
            result.put("status", "FAILED");
            result.put("response", "Permission denied!");
        }

        if(result.get("status") == "SUCCESS"){
            loggingService.logActivity("", sessionId, customer, userName, "downloadMonthlyReport", "DownloadMonthlyReport SUCCESS");
            return ResponseEntity.ok(result);
        }else {
            return ResponseEntity.status(300).body(result);
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DownloadRequest{
        private String monthDate;
        private Integer customer;
        private Integer userType;
        private Integer userId;
        private LocalDate fromDate;
        private LocalDate toDate;
        private String userName;
        private String sessionId;
    }

}

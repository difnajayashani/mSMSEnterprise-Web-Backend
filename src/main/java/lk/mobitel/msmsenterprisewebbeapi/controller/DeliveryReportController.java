package lk.mobitel.msmsenterprisewebbeapi.controller;

import lk.mobitel.msmsenterprisewebbeapi.model.esms.DeliveryReport;
import lk.mobitel.msmsenterprisewebbeapi.service.DeliveryReportService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
public class DeliveryReportController {
    @Autowired
    DeliveryReportService deliveryReportService;

    @GetMapping("/getAllDeliveryReports")
    public ResponseEntity<List<DeliveryReport>> getAllDeliveryReports() {
        List<DeliveryReport> deliveryReports = deliveryReportService.getAllDeliveryReports();

        if (deliveryReports != null) {
            return new ResponseEntity<>(deliveryReports, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/getDeliveryReportsWithinDates")
    public ResponseEntity<?> getDeliveryReportsWithinDates(@RequestBody DeliveryRequest deliveryRequest) {
        String alias = deliveryRequest.getSenderAlias();
        Date fromDate = deliveryRequest.getFromDate();
        Date toDate = deliveryRequest.getToDate();
        Integer customer = deliveryRequest.getCustomer();

        Integer Total_SMS = deliveryReportService.download_delivery_reports_count(alias, fromDate, toDate, customer);
        if (Total_SMS == 0){
            return ResponseEntity.status(300).body("EMPTY RESULT");
        }else {
            List<Map<String, Object>> result = deliveryReportService.download_delivery_reports(alias,fromDate,toDate,customer);
            return ResponseEntity.ok(result);
        }
    }

    @PostMapping("/downloadDeliveryReportsWithinDates")
    public ResponseEntity<?> downloadDeliveryReportsWithinDates(@RequestBody DeliveryRequest deliveryRequest) {
        String alias = deliveryRequest.getSenderAlias();
        Date fromDate = deliveryRequest.getFromDate();
        Date toDate = deliveryRequest.getToDate();
        Integer customer = deliveryRequest.getCustomer();

        Integer Total_SMS = deliveryReportService.download_delivery_reports_count(alias, fromDate, toDate, customer);
        if (Total_SMS == 0){
            return ResponseEntity.status(300).body("EMPTY RESULT");
        } else {
            List<Map<String, Object>> result = deliveryReportService.download_delivery_reports(alias, fromDate, toDate, customer);
            try {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                OutputStreamWriter writer = new OutputStreamWriter(outputStream);

                // Write CSV data to the output stream
                writer.write("sender" + "," + "recipient" + "," + "status" + "," + "time" + "\n");
                for (Map<String, Object> entry : result) {
                    writer.write(entry.get("sender") + "," + entry.get("recipient") + "," + entry.get("status") + "," + entry.get("time") + "\n");
                }
                writer.flush();

                // Set headers for file download
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                headers.setContentDispositionFormData("attachment", "delivery_report.csv");

                return new ResponseEntity<>(outputStream.toByteArray(), headers, HttpStatus.OK);
            } catch (IOException e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to generate file: " + e.getMessage());
            }
        }
    }

    @Getter
    @Setter
    public static class DeliveryRequest{
        private String senderAlias;
        private Date fromDate;
        private Date toDate;
        private Integer customer;
    }
}

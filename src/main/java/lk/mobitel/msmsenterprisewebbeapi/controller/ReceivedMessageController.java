package lk.mobitel.msmsenterprisewebbeapi.controller;

import lk.mobitel.msmsenterprisewebbeapi.model.esms.ReceivedMessage;
import lk.mobitel.msmsenterprisewebbeapi.service.LoggingService;
import lk.mobitel.msmsenterprisewebbeapi.service.ReceivedMessageService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
public class ReceivedMessageController {
    @Autowired
    ReceivedMessageService receivedMessageService;

    @Autowired
    LoggingService loggingService;

    @PostMapping("/getAllReceivedItems")
    public ResponseEntity<?> getAllReceivedItems(@RequestBody MessageRequest messageRequest){
        int customer = messageRequest.getCustomer();
        int userId = messageRequest.getUserId();
        int userType = messageRequest.getUserType();
        String currentInbox = messageRequest.getCurrentInbox();
        String sessionId = messageRequest.getSessionId();
        String userName = messageRequest.getUserName();

        loggingService.logActivity("", sessionId, customer, userName, "getAllReceivedItems", "Received request with data");

        if(currentInbox == "" || currentInbox == null){
            List<Map<String,Object>> response;
            response = receivedMessageService.getAllInboxes(customer,userId);
            if (!response.isEmpty() && response.get(0).get("inbox") != null) {
                currentInbox = (String) response.get(0).get("inbox");
            }
        }

        List<ReceivedMessage> response = receivedMessageService.getAllReceivedItems(currentInbox,customer,userId,userType);
        if(response.isEmpty()){
            return ResponseEntity.status(300).body("No Received Message, check the inbox");
        }else {
            loggingService.logActivity("", sessionId, customer, userName, "getAllReceivedItems", "fetching was successful");
            return ResponseEntity.ok(response);
        }
    }

    @PostMapping("/downloadInboxWithinDates")
    public ResponseEntity<?> downloadInboxWithinDates(@RequestBody MessageRequest messageRequest){
        int customer = messageRequest.getCustomer();
        int userId = messageRequest.getUserId();
        int userType = messageRequest.getUserType();
        Date fromDate = messageRequest.getFromDate();
        Date toDate = messageRequest.getToDate();
        String currentInbox = messageRequest.getCurrentInbox();
        String sessionId = messageRequest.getSessionId();
        String userName = messageRequest.getUserName();

        loggingService.logActivity("", sessionId, customer, userName, "downloadInboxWithinDates", "Received request with data");

        if(currentInbox == "" || currentInbox == null){
            List<Map<String,Object>> response;
            response = receivedMessageService.getAllInboxes(customer,userId);
            if (!response.isEmpty() && response.get(0).get("inbox") != null) {
                currentInbox = (String) response.get(0).get("inbox");
            }
        }
        List<ReceivedMessage> response = receivedMessageService.downloadInboxWithinDates(currentInbox,fromDate,toDate,customer,userId,userType);
        if(response.isEmpty()){
            return ResponseEntity.status(300).body("No Received Message, check the inbox");
        }else {
            try {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                OutputStreamWriter writer = new OutputStreamWriter(outputStream);

                // Write CSV data to the output stream
                writer.write("sender" + "," + "recipient" + "," + "message" + "," + "time" + "\n");
                for (ReceivedMessage entry : response) {
                    writer.write(entry.getSender() + "," + entry.getRecipient() + "," + entry.getMessage() + "," + entry.getTime() + "\n");
                }
                writer.flush();

                // Set headers for file download
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                headers.setContentDispositionFormData("attachment", "inbox_report.csv");

                loggingService.logActivity("", sessionId, customer, userName, "downloadInboxWithinDates", "fetching was successful");

                return new ResponseEntity<>(outputStream.toByteArray(), headers, HttpStatus.OK);
            } catch (IOException e) {
                e.printStackTrace();
                return ResponseEntity.status(300).body("Failed to generate file: " + e.getMessage());
            }
        }
    }

    @Getter
    @Setter
    public static class MessageRequest{
        private String currentInbox;
        private Integer customer;
        private Integer userId;
        private Integer userType;
        private Date fromDate;
        private Date toDate;
        private String userName;
        private String sessionId;
    }
}

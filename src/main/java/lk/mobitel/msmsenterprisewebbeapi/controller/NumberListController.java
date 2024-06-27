package lk.mobitel.msmsenterprisewebbeapi.controller;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import lk.mobitel.msmsenterprisewebbeapi.model.esms.NumberList;
import lk.mobitel.msmsenterprisewebbeapi.service.LoggingService;
import lk.mobitel.msmsenterprisewebbeapi.service.NewSMSService;
import lk.mobitel.msmsenterprisewebbeapi.service.NumberListService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
public class NumberListController {
    @Autowired
    NumberListService numberListService;
    @Autowired
    NewSMSService newSMSService;
    @Autowired
    LoggingService loggingService;

    @PostMapping("/validateNumberList")
    public ResponseEntity<?> validateNumberListFromCsv(@RequestHeader HttpHeaders headers, @RequestParam("file") MultipartFile file){
        int customer = Integer.parseInt(Objects.requireNonNull(headers.getFirst("customer")));
        int enableIdd = Integer.parseInt(Objects.requireNonNull(headers.getFirst("enableIdd")));
        int enableM2o = Integer.parseInt(Objects.requireNonNull(headers.getFirst("enableM2o")));
        int enableM2all = Integer.parseInt(Objects.requireNonNull(headers.getFirst("enableM2all")));
        String sessionId = headers.getFirst("sessionId");
        String userName = headers.getFirst("userName");

        Map<String,Object> result = new HashMap<>();
        Map<String,Object> response = new HashMap<>();
        String status;
        String ClientIP = "";
        if (file.isEmpty()) {
            status = "Error: File is empty";
        }

        String fileName = file.getOriginalFilename();
        if (!fileName.endsWith(".csv") && !fileName.endsWith(".xls") && !fileName.endsWith(".xlsx")) {
            response.put("status","Error: Invalid file format");
            String log = "Invalid numberList file format.";
            loggingService.logActivity(ClientIP,sessionId,customer,userName,"upload_numberList_file",log);
        }

        String uploadDir = "C:/Users/saranjanr/Desktop/Esms_files/";
        int dotIndex = fileName.lastIndexOf('.');
        String name = fileName.substring(0, dotIndex);
        String extension = fileName.substring(dotIndex + 1);
        String dateFile = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String timeFile = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmm"));
        String fileNameFinal = name + "_" + customer + "_" + dateFile + "_" + timeFile + "." + extension;

        try {
            File destFile = new File(uploadDir + fileNameFinal);
            file.transferTo(destFile);

            response.put("status","File uploaded successfully");
            String log = "NumberList file - " + fileNameFinal + " has been uploaded successfully.";
            loggingService.logActivity(ClientIP,sessionId,customer,userName,"upload_numberList_file",log);
        } catch (IOException e) {
            e.printStackTrace();
            response.put("status","Error occurred during file upload");
            String log = "NumberList File upload Error - " + fileName;
            loggingService.logActivity(ClientIP,sessionId,customer,userName,"upload_numberList_file",log);
        }

        int count = 0;
        int validCount = 0;
        int invalidCount = 0;

        List<String> validRecipientList = new ArrayList<>();
        if(response.get("status").equals("File uploaded successfully")){
            if(fileNameFinal.endsWith(".csv")){
                String inputFilePath = "C:/Users/saranjanr/Desktop/Esms_files/" + fileName;
                try (CSVReader reader = new CSVReader(new FileReader(inputFilePath))) {
                    List<String[]> rows = reader.readAll();

                    for (String[] row : rows) {
                        for (String cell : row) {
                            Map<String, String> typ = newSMSService.getRecipientType(cell);
                            String type = typ.get("type");
//                            System.out.println("type:" + type + " " + cell);
                            if ("0".equals(type)) {
                                invalidCount++;
                            } else if (enableIdd == 0 && "IDD".equals(type)) {
                                invalidCount++;
                            } else if (enableM2o == 0 && enableM2all == 0 && "M2O".equals(type)) {
                                invalidCount++;
                            } else {
                                validCount++;
                                if (!validRecipientList.contains(cell)) {
                                    validRecipientList.add(cell);
                                }
                            }
                            count++;
                        }
                    }

                    result.put("status", "SUCCESS");
                    result.put("response", "NumberList uploaded successfully");
                    result.put("count", count);
                    result.put("valid_count", validCount);
                    result.put("invalid_count", invalidCount);
                    result.put("valid_added_count", validRecipientList.size());
                    result.put("duplicate_count", validCount - validRecipientList.size());
                    result.put("fileName",fileNameFinal);

                    String log = "NumberList validated successfully | count - " + count + " | valid_count - " + validCount + " | valid_added_count - " + validRecipientList.size() + " | invalid_count - " + invalidCount + " | duplicate_count - " + (validCount - validRecipientList.size());
                    loggingService.logActivity(ClientIP, sessionId, customer, userName, "upload_numberList_file",log);
//                    System.out.println(count + " " + validCount + " " +validRecipientList.size() + " " +invalidCount );
                    response.put("status","File validated successfully");
                    response.put("result",result);
                } catch (IOException | CsvException e) {
                    e.printStackTrace();
                    response.put("status","NumberList validation failed");
                }
            }else {
                response.put("status","NumberList not in csv format");
            }
        }
        response.put("result",result);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/addNumberList")
    public ResponseEntity<?> addNumberList(@RequestBody NumberListRequest numberListRequest, @RequestHeader HttpHeaders headers){
        int customer = Integer.parseInt(Objects.requireNonNull(headers.getFirst("customer")));
        int enableIdd = Integer.parseInt(Objects.requireNonNull(headers.getFirst("enableIdd")));
        int enableM2o = Integer.parseInt(Objects.requireNonNull(headers.getFirst("enableM2o")));
        int enableM2all = Integer.parseInt(Objects.requireNonNull(headers.getFirst("enableM2all")));
        String sessionId = headers.getFirst("sessionId");
        String userName = headers.getFirst("userName");
        Integer userId = Integer.valueOf(headers.getFirst("userId"));
        Integer userType = Integer.valueOf(headers.getFirst("userType"));

        String fileName = numberListRequest.getFileName();
        String additionalNumber = numberListRequest.getAdditionalNumber();
        Map<String,Object> responseArray = new HashMap<>();
        if(fileName == null || fileName.isEmpty()){
            responseArray.put("status","FAILED");
            responseArray.put("response","Invalid File! Please upload a correct file");
        }else {
            String[] filenameParts = fileName.split("\\.");
            String filenameWithoutExtension = filenameParts[0]; // Access the filename without the extension
            String fileExtension = filenameParts[1]; // Access the file extension

            if(Objects.equals(fileExtension, "csv")){
                Map<String,Object> result = numberListService.loadFromCsv(fileName,numberListRequest.getNumberListName(),customer,userName,userId,userType,sessionId,enableIdd,enableM2o,enableM2all,additionalNumber);
                responseArray = result;
            } else if (Objects.equals(fileExtension, "xls") || Objects.equals(fileExtension, "xlsx")) {

            }else {
                responseArray.put("status","FAILED");
                responseArray.put("response","Invalid File! Please upload a correct file");
            }
        }
        if(responseArray.get("status") == "SUCCESS"){
            return ResponseEntity.ok(responseArray.get("response"));
        }else {
            System.out.println("response" + responseArray.get("response"));
            return ResponseEntity.status(300).body(responseArray.get("response"));
        }
    }

    @GetMapping("/getAllNumberLists")
    public ResponseEntity<List<NumberList>> getAllNumberLists(@RequestHeader HttpHeaders headers){
        int customer = Integer.parseInt(Objects.requireNonNull(headers.getFirst("customer")));
        int userId = Integer.parseInt(Objects.requireNonNull(headers.getFirst("userId")));
        int userType = Integer.parseInt(Objects.requireNonNull(headers.getFirst("userType")));
        String sessionId = headers.getFirst("sessionId");
        String userName = headers.getFirst("userName");
        String sourceIP = "";
        List<NumberList> numberLists = numberListService.getAllNumberLists(customer, userId, userType);
        String log = "All NumberLists requested by " + userName;
        loggingService.logActivity(sourceIP, sessionId, customer, userName, "get_all_numberlists",log);

        if (numberLists != null) {
            return new ResponseEntity<>(numberLists, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/getNumberLists")
    public ResponseEntity<?> getNumberLists(@RequestHeader HttpHeaders headers){
        int customer = Integer.parseInt(Objects.requireNonNull(headers.getFirst("customer")));
        int userId = Integer.parseInt(Objects.requireNonNull(headers.getFirst("userId")));
        int userType = Integer.parseInt(Objects.requireNonNull(headers.getFirst("userType")));

        List<Map<String,Object>> numberLists = numberListService.getNumberLists(customer,userId,userType);
        if(numberLists.isEmpty()){
            return ResponseEntity.status(300).body("FAILED");
        }else {
            return ResponseEntity.ok(numberLists);
        }
    }

    @PutMapping("/editNumberList")
    public ResponseEntity<?> editNumberList(@RequestBody NumberListRequest numberListRequest, @RequestHeader HttpHeaders headers){
        String sessionId = headers.getFirst("sessionId");
        String userName = headers.getFirst("userName");
        Integer userId = Integer.valueOf(headers.getFirst("userId"));
        Integer userType = Integer.valueOf(headers.getFirst("userType"));
        Integer customer = Integer.valueOf(headers.getFirst("customer"));

        String additionalNumber = numberListRequest.getAdditionalNumber();
        String numberListName = numberListRequest.getNumberListName();
        Integer numberListId = numberListRequest.getNumberListId();
        String sourceIP = "";
        String response = "";
        Object numberListCount = numberListService.validate_numberList(customer,userId,userType,numberListId);
        if((Long) numberListCount < 1){
            response = "INVALID NUMBERLIST";
            String log = "Invalid numberList - " + numberListId;
            loggingService.logActivity(sourceIP, sessionId, customer, userName, "update_numberlist",log);
        }else {
            response = numberListService.update_numberList(numberListId,additionalNumber);
            String log = "NumberList updated - " + numberListId;
            loggingService.logActivity(sourceIP, sessionId, customer, userName, "update_numberlist",log);
        }

        if(Objects.equals(response, "Success")){
            return ResponseEntity.ok("Success");
        }else {
            return ResponseEntity.status(300).body(response);
        }
    }

    @DeleteMapping("/deleteNumberList")
    public ResponseEntity<?> deleteNumberList(@RequestHeader HttpHeaders headers){
        int customer = Integer.parseInt(Objects.requireNonNull(headers.getFirst("customer")));
        int numberListId = Integer.parseInt(Objects.requireNonNull(headers.getFirst("id")));
        int userId = Integer.parseInt(Objects.requireNonNull(headers.getFirst("userId")));
        int userType = Integer.parseInt(Objects.requireNonNull(headers.getFirst("userType")));
        String sessionId = headers.getFirst("sessionId");
        String userName = headers.getFirst("userName");
        String sourceIP = "";
        String response = "";
        Object numberListCount = numberListService.validate_numberList(customer,userId,userType,numberListId);
        if((Long) numberListCount < 1){
            String log = "Invalid numberList - " + numberListId;
            loggingService.logActivity(sourceIP, sessionId, customer, userName, "delete_numberlist",log);
            response = "INVALID NUMBERLIST";
        }else {
            response = numberListService.delete_numberList(numberListId);
            String log = "NumberList deleted " + response;
            loggingService.logActivity(sourceIP, sessionId, customer, userName, "delete_numberlist",log);
        }

        if(Objects.equals(response, "Success")){
            return ResponseEntity.ok(response);
        }else {
            return ResponseEntity.status(300).body(response);
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NumberListRequest{
        private Integer numberListId;
        private String fileName;
        private String numberListName;
        private String additionalNumber;
    }
}

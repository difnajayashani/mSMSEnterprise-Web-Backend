package lk.mobitel.msmsenterprisewebbeapi.controller;

import lk.mobitel.msmsenterprisewebbeapi.model.esms.Alias;
import lk.mobitel.msmsenterprisewebbeapi.service.AliasService;
import lk.mobitel.msmsenterprisewebbeapi.service.LoggingService;
import lk.mobitel.msmsenterprisewebbeapi.service.UserService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
public class AliasController {
    @Autowired
    AliasService aliasService;
    @Autowired
    UserService userService;
    @Autowired
    LoggingService loggingService;

    @PostMapping("/getAlias")
    public ResponseEntity<?> getAlias(@RequestBody RequestData requestData) {
        try {
            int userId = requestData.getUserId();
            long customer = requestData.getCustomer();
            int userType = requestData.getUserType();
            String sessionId = requestData.getSessionId();
            String userName = requestData.getUserName();

            loggingService.logActivity("", sessionId, (int) customer, userName, "getAlias", "Received request with data");

            Object sessionDetailsObj = userService.getCISession(sessionId);

            if (!"INVALID SESSION".equals(sessionDetailsObj)) {
                List<Alias> alias = aliasService.get_alias(userId, customer);
                if(alias.isEmpty()){
                    return ResponseEntity.status(200).body("Something Failed");
                } else {
                    // Log returning alias list
                    loggingService.logActivity("", sessionId, (int) customer, userName, "getAlias", "Returning alias list");
                    return ResponseEntity.ok(alias);
                }
            } else {
                return ResponseEntity.status(200).body("Session is invalid");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error");
        }
    }


    @GetMapping("/getInboxes")
    public ResponseEntity<?> getInboxes(@RequestHeader HttpHeaders headers){
        int userId = Integer.parseInt(Objects.requireNonNull(headers.getFirst("userid")));
        int userType = Integer.parseInt(Objects.requireNonNull(headers.getFirst("usertype")));
        int customer = Integer.parseInt(Objects.requireNonNull(headers.getFirst("customer")));
        String userName = Objects.requireNonNull(headers.getFirst("username"));
        String sessionId = Objects.requireNonNull(headers.getFirst("sessionid"));


        loggingService.logActivity("", sessionId, (int) customer, userName, "getInboxes", "Received request");

        List<Map<String, Object>> inboxes = aliasService.get_inboxes(userId,userType,customer);
        if(inboxes.isEmpty()){
            return ResponseEntity.status(300).body("FAILED");
        }else {
            loggingService.logActivity("", sessionId, (int) customer, userName, "getInboxes", "Returning inboxes");
            return ResponseEntity.ok(inboxes);
        }
    }

    @Getter
    @Setter
    public static class RequestData {
        private int userId;
        private long customer;
        private int userType;
        private String sessionId;
        private String userName;
    }

}

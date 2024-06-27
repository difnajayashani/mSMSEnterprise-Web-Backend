package lk.mobitel.msmsenterprisewebbeapi.controller;

import jakarta.servlet.http.HttpServletRequest;
import lk.mobitel.msmsenterprisewebbeapi.model.esms.Alias;
import lk.mobitel.msmsenterprisewebbeapi.service.AliasService;
import lk.mobitel.msmsenterprisewebbeapi.service.LoggingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@RestController
//@RequestMapping("/mSMSEnterpriseWebBEAPI")
public class TestController {

    @Autowired
    AliasService aliasService;
    @Autowired
    LoggingService loggingService;

    @GetMapping("/hello")
    public String sayHello() {
        return "Hello,,,,,, from mSMSEnterprise RESTful API!";
    }

    @PostMapping("/hashPassword")
    public static String hashPassword(@RequestBody String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.trim().getBytes());

            // Convert byte array to a string
            String hashedPassword = Base64.getEncoder().encodeToString(hash);
            return hashedPassword;

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return password;
        }
    }

    @GetMapping("/getAlas1")
    public ResponseEntity<?> getAlas1(@RequestHeader Map<String, String> allHeaders) {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("userId", allHeaders.get("userid"));
            response.put("customer", allHeaders.get("customer"));
            response.put("userType", allHeaders.get("usertype"));
            response.put("sessionId", allHeaders.get("sessionid"));
//            response.put("Keys",allHeaders.keySet());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e);
        }
    }

    @GetMapping("/getAlas4")
    public ResponseEntity<?> getAlas4(@RequestHeader Map<String, String> allHeaders) {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("userId", allHeaders.get("userid"));
            response.put("customer", allHeaders.get("customer"));
            response.put("userType", allHeaders.get("usertype"));
            response.put("sessionId", allHeaders.get("sessionid"));
//            response.put("Keys",allHeaders.keySet());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e);
        }
    }

    @GetMapping("/getAlas2")
    public ResponseEntity<?> getAlas2(@RequestHeader HttpHeaders headers) {
        try {
            int userId = Integer.parseInt(Objects.requireNonNull(headers.getFirst("userid")));
            long customer = Long.parseLong(Objects.requireNonNull(headers.getFirst("customer")));
            int userType = Integer.parseInt(Objects.requireNonNull(headers.getFirst("usertype")));
            String sessionId = headers.getFirst("sessionid");

            Map<String, Object> response = new HashMap<>();
            response.put("userId", userId);
            response.put("customer", customer);
            response.put("userType", userType);
            response.put("sessionId", sessionId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error");
        }
    }

    @GetMapping("/getAlas3")
    public ResponseEntity<?> getAlas3(HttpServletRequest request) {
        try {
            int userId = Integer.parseInt(request.getHeader("user_id"));
            long customer = Long.parseLong(request.getHeader("customer"));
            int userType = Integer.parseInt(request.getHeader("userType"));
            String sessionId = request.getHeader("sessionId");

            Map<String, Object> response = new HashMap<>();
            response.put("userId", userId);
            response.put("customer", customer);
            response.put("userType", userType);
            response.put("sessionId", sessionId);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error");
        }
    }
}

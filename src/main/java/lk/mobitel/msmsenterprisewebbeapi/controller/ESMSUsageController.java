package lk.mobitel.msmsenterprisewebbeapi.controller;

import lk.mobitel.msmsenterprisewebbeapi.model.esms.CgMonthlySmsCount;
import lk.mobitel.msmsenterprisewebbeapi.service.ESMSUsageService;
import lk.mobitel.msmsenterprisewebbeapi.service.LoggingService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
public class ESMSUsageController {
    @Autowired
    ESMSUsageService esmsUsageService;
    @Autowired
    LoggingService loggingService;

    @PostMapping("/getMonthlyUsage")
    public ResponseEntity<?> getMonthlyUsage(@RequestBody MonthlyUsageRequest monthlyUsageRequest){
        String accountNo = monthlyUsageRequest.getAccountNo();
        LocalDate billingMonth = monthlyUsageRequest.getBillingMonth();
        String sessionId = monthlyUsageRequest.getSessionId();
        int customer = monthlyUsageRequest.getCustomer();
        String userName = monthlyUsageRequest.getUserName();

        loggingService.logActivity("", sessionId, customer, userName, "getMonthlyUsage", "Received request with data");

        List<CgMonthlySmsCount> cgMonthlySmsCounts = esmsUsageService.get_monthly_usage(accountNo,billingMonth);
        if(cgMonthlySmsCounts.isEmpty()){
            return ResponseEntity.status(300).body("No Data Exist, Check the input credentials");
        }else {
            loggingService.logActivity("", sessionId, customer, userName, "getMonthlyUsage", "Returning data");
            return ResponseEntity.ok(cgMonthlySmsCounts);
        }
    }

    @PostMapping("/getMonthlyUsagePerCustomer")
    public ResponseEntity<?> getMonthlyUsagePerCustomer(@RequestBody MonthlyUsageRequest monthlyUsageRequest){
        String accountNo = monthlyUsageRequest.getAccountNo();
        String sessionId = monthlyUsageRequest.getSessionId();
        int customer = monthlyUsageRequest.getCustomer();
        String userName = monthlyUsageRequest.getUserName();

        loggingService.logActivity("", sessionId, customer, userName, "getMonthlyUsagePerCustomer", "Received request with data");

        List<CgMonthlySmsCount> cgMonthlySmsCounts = esmsUsageService.get_monthly_usage_perCustomer(accountNo);
        if(cgMonthlySmsCounts.isEmpty()){
            return ResponseEntity.status(300).body("No Data Exists, Check the input credentials");
        }else {
            loggingService.logActivity("", sessionId, customer, userName, "getMonthlyUsagePerCustomer", "Returning data");
            return ResponseEntity.ok(cgMonthlySmsCounts);
        }
    }

    @PostMapping("/getMonthlyUsagePerMonth")
    public ResponseEntity<?> getMonthlyUsagePerMonth(@RequestBody MonthlyUsageRequest monthlyUsageRequest){
        LocalDate billingMonth = monthlyUsageRequest.getBillingMonth();
        String sessionId = monthlyUsageRequest.getSessionId();
        int customer = monthlyUsageRequest.getCustomer();
        String userName = monthlyUsageRequest.getUserName();

        loggingService.logActivity("", sessionId, customer, userName, "getMonthlyUsagePerMonth", "Received request with data");

        List<CgMonthlySmsCount> cgMonthlySmsCounts = esmsUsageService.get_monthly_usage_perMonth(billingMonth);
        if(cgMonthlySmsCounts.isEmpty()){
            return ResponseEntity.status(300).body("No Data Exists, Check the input credentials");
        }else {
            loggingService.logActivity("", sessionId, customer, userName, "getMonthlyUsagePerMonth", "Returning data");
            return ResponseEntity.ok(cgMonthlySmsCounts);
        }
    }

    @PostMapping("/getAccountStatus")
    public ResponseEntity<?> getLastUsage(@RequestBody MonthlyUsageRequest monthlyUsageRequest){
        Integer accountNo = monthlyUsageRequest.getAccountNo2();
        String sessionId = monthlyUsageRequest.getSessionId();
        int customer = monthlyUsageRequest.getCustomer();
        String userName = monthlyUsageRequest.getUserName();

        loggingService.logActivity("", sessionId, customer, userName, "getAccountStatus", "Received request with data");

        Map<String,Object> response = esmsUsageService.get_last_usage(accountNo);
        if(response.isEmpty()){
            return ResponseEntity.status(300).body("No Data Exists, Check the input credentials");
        }else {
            loggingService.logActivity("", sessionId, customer, userName, "getAccountStatus", "Returning data");
            return ResponseEntity.ok(response);
        }
    }

    @PostMapping("/getAvailableUserLogins")
    public ResponseEntity<?> getAvailableUserLogins(@RequestBody MonthlyUsageRequest monthlyUsageRequest){
        Integer accountNo = monthlyUsageRequest.getAccountNo2();
        String sessionId = monthlyUsageRequest.getSessionId();
        int customer = monthlyUsageRequest.getCustomer();
        String userName = monthlyUsageRequest.getUserName();

        loggingService.logActivity("", sessionId, customer, userName, "getAvailableUserLogins", "Received request with data");

        List<Map<String,String>> response = esmsUsageService.get_user_logins(accountNo);
        if(response.isEmpty()){
            return ResponseEntity.status(300).body("No Data Exists, Check the input credentials");
        }else {
            loggingService.logActivity("", sessionId, customer, userName, "getAvailableUserLogins", "Returning data");
            return ResponseEntity.ok(response);
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyUsageRequest{
        private String accountNo;
        private Integer accountNo2;
        private LocalDate billingMonth;
        private Integer customer;
        private String sessionId;
        private String userName;
    }
}

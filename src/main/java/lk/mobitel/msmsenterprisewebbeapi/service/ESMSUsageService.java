package lk.mobitel.msmsenterprisewebbeapi.service;

import lk.mobitel.msmsenterprisewebbeapi.model.esms.CgMonthlySmsCount;
import lk.mobitel.msmsenterprisewebbeapi.model.esms.CustomerProfile;
import lk.mobitel.msmsenterprisewebbeapi.repository.esms.CgDailySmsCountRepository;
import lk.mobitel.msmsenterprisewebbeapi.repository.esms.CgMonthlySmsCountRepository;
import lk.mobitel.msmsenterprisewebbeapi.repository.esms.CustomerProfileRepository;
import lk.mobitel.msmsenterprisewebbeapi.repository.esms.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ESMSUsageService {
    @Autowired
    CustomerProfileRepository customerProfileRepository;
    @Autowired
    CgMonthlySmsCountRepository cgMonthlySmsCountRepository;
    @Autowired
    CgDailySmsCountRepository cgDailySmsCountRepository;

    @Autowired
    UserRepository userRepository;

    public Map<String, Object> getLastUsage(Integer accountNo) {
        CustomerProfile customerProfile = customerProfileRepository.findByAccountNo(accountNo);

        if (customerProfile == null) {
            return null; // Handle if customer profile does not exist
        }

        String accountStatus = (customerProfile.getStatus() == 1) ? "Active" : "Deactivated";
        String accountHaltStatus = (customerProfile.getHaltServices() == 1) ? "Temporary Halted" : "Not Halted";

//        DailySmsCount lastUsage = customerProfileRepository.findFirstByCustomerAndTotalGreaterThanOrderByDateDesc(accountNo);
//
//        Map<String, Object> result = new HashMap<>();
//        result.put("accNo", customerProfile.getAccountNo());
//        result.put("usageDate", (lastUsage != null) ? lastUsage.getDate() : null);
//        result.put("status", accountStatus);
//        result.put("tempStatus", accountHaltStatus);
//        result.put("name", customerProfile.getName());

        return null;
    }

    public List<CgMonthlySmsCount> get_monthly_usage(String accountNo, LocalDate billingMonth) {
        return cgMonthlySmsCountRepository.get_monthly_usage(accountNo,billingMonth);
    }

    public List<CgMonthlySmsCount> get_monthly_usage_perCustomer(String accountNo) {
        return cgMonthlySmsCountRepository.get_monthly_usage_perCustomer(accountNo);
    }

    public List<CgMonthlySmsCount> get_monthly_usage_perMonth(LocalDate billingMonth) {
        return cgMonthlySmsCountRepository.get_monthly_usage_perMonth(billingMonth);
    }

    public Map<String,Object> get_last_usage(Integer accountNo) {
        Map<String,Object> result1 = customerProfileRepository.getCustomerProfile(accountNo);
        Map<String,Object> result2 = cgDailySmsCountRepository.DateFromCgDailySmsCount(accountNo.toString());

        Map<String,Object> response = new HashMap<>();
        if((Integer) result1.get("status") == 1){
            response.put("accountStatus","Active");
        }else {
            response.put("accountStatus","Deactivated");
        }

        if((Integer) result1.get("haltServices") == 1){
            response.put("accountHaltStatus","Temporary Halted");
        }else {
            response.put("accountHaltStatus","Not halted");
        }

        response.put("accountNo",accountNo);
        response.put("accountName",result1.get("name"));
        response.put("usageDate",result2.get("date"));
//        System.out.println(result1.get("accountNo") + " " + result1.get("name") + " " + result1.get("status") + " " + result1.get("haltServices") + " " + result1.get("dateDeactivated") + " " + result1.get("dateReactivated") + " " + result2.get("date"));
        return response;
    }

    public List<Map<String,String>> get_user_logins(Integer accountNo) {
        return userRepository.get_user_logins(accountNo);
    }
}

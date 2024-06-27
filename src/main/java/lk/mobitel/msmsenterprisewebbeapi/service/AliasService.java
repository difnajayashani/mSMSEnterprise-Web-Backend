package lk.mobitel.msmsenterprisewebbeapi.service;

import lk.mobitel.msmsenterprisewebbeapi.model.esms.Alias;
import lk.mobitel.msmsenterprisewebbeapi.model.esms.LongNumber;
import lk.mobitel.msmsenterprisewebbeapi.repository.esms.AliasRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AliasService {
    @Autowired
    AliasRepository aliasRepository;

    public String verifyAlias(Integer customer, int userId, String alias) {
        String output = "INVALID";
        Alias response = new Alias();
        if (customer == 80808080) {
            // Use the repository method to check if the alias exists for the given customer
            response = aliasRepository.existsByCustomerAndAlias(customer, alias);
        } else {
            // Use the repository method to check if the alias exists for the given customer and userId
            response = aliasRepository.existsByCustomerAndUserIdAndAlias(customer, userId, alias);
        }

        if(response != null)
        {
            output = "VALID";
        }
        return output;
    }

    public List<Alias> get_alias(int userId, Long customer) {
        List<Alias> response = new ArrayList<>();
        if(customer == 111111){
            response = aliasRepository.getAlias();
        }else if(customer == 80808080){
            response = aliasRepository.getAliasWithCustomer(customer);
        }else {
            response = aliasRepository.getAliasWithCustomerAndUserId(customer,userId);
        }
        return response;
    }

    public List<Map<String, Object>> get_inboxes(int userId, int userType, int customer) {
        List<Map<String, Object>> response;
        if (customer == 111111) {
            response = aliasRepository.getInbox();
        } else {
            response = aliasRepository.getInboxWithCustomerAndUserId(customer, userId);
        }
        return response;
    }

    public List<Alias> getAlias() {
        return aliasRepository.getAliasCustomerAndUserId();
    }
}

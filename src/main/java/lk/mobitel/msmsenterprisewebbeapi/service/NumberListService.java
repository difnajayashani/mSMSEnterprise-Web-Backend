package lk.mobitel.msmsenterprisewebbeapi.service;

import lk.mobitel.msmsenterprisewebbeapi.model.esms.NumberList;
import lk.mobitel.msmsenterprisewebbeapi.model.esms.NumberListArchive;
import lk.mobitel.msmsenterprisewebbeapi.model.esms.Numbers;
import lk.mobitel.msmsenterprisewebbeapi.repository.esms.NumberListArchiveRepository;
import lk.mobitel.msmsenterprisewebbeapi.repository.esms.NumberListRepository;
import lk.mobitel.msmsenterprisewebbeapi.repository.esms.NumbersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

@Service
public class NumberListService {
    @Autowired
    NumberListRepository numberListRepository;
    @Autowired
    NewSMSService newSMSService;
    @Autowired
    NumbersRepository numbersRepository;
    @Autowired
    NumberListArchiveRepository numberListArchiveRepository;
    @Autowired
    LoggingService loggingService;

    public List<NumberList> getAllNumberLists(Integer customer, Integer user, Integer userType) {
        if (userType == 0) {
            return numberListRepository.getAllNumberListsForCustomer(customer);
        } else if (userType == 101 || userType == 10) {
            return numberListRepository.getAllNumberListsForUser(customer, user);
        }
        return Collections.emptyList();
    }

    public Map<String, Object> loadFromCsv(String fileName, String numberListName, Integer customer, String userName, Integer userId, Integer userType, String sessionId, Integer enableIdd, Integer enableM2o, Integer enableM2all,String additionalNumber) {
        String inputFileName = "C:/Users/saranjanr/Desktop/Esms_files/" + fileName;
        int count = 0;
        int validCount = 0;
        int invalidCount = 0;
        String sourceIP = "";
        List<String> validRecipientList = new ArrayList<>();
        Map<String,Object> result = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(inputFileName))) {
            NumberList numberListExist = numberListRepository.numberListExist(customer, numberListName);
            if (numberListExist != null) {
                result.put("status", "FAILED");
                result.put("response", "NumberList name already exists!");
                String log = "NumberList name already exists - " + numberListExist.getName();
                loggingService.logActivity(sourceIP, sessionId, customer, userName, "load_from_csv",log);
            } else {
                numberListRepository.insertNumberList(numberListName, customer, userId, new Date());
                NumberList numberList = numberListRepository.numberListExist(customer, numberListName);
                int id = numberList.getId();
                String line;
                while ((line = br.readLine()) != null) {
                    String[] lineData = line.split(",");
                    String recipient = lineData[0];
                    System.out.println("recipient::::" + recipient);
                    Map<String, String> typ = newSMSService.getRecipientType(recipient);
                    String type = typ.get("type");
                    System.out.println("type:" + type + " " + recipient);
                    if ("0".equals(type)) {
                        invalidCount++;
                    } else if (enableIdd == 0 && "IDD".equals(type)) {
                        invalidCount++;
                    } else if (enableM2o == 0 && enableM2all == 0 && "M2O".equals(type)) {
                        invalidCount++;
                    } else {
                        validCount++;
                        if (!validRecipientList.contains(recipient)) {
                            validRecipientList.add(recipient);
                            numbersRepository.addNumber(recipient, type, id);
                        }
                    }
                    count++;
                }

                if(additionalNumber != null){
                    String[] additionalNumberArray = additionalNumber.split(",");
                    for (String number : additionalNumberArray) {
                        Map<String, String> typ = newSMSService.getRecipientType(number);
                        String type = typ.get("type");
                        if ("0".equals(type) || (enableIdd == 0 && "IDD".equals(type)) || (enableM2o == 0 && enableM2all == 0 && "M2O".equals(type))) {
                            invalidCount++;
                        } else {
                            validCount++;
                            if (!validRecipientList.contains(number)) {
                                validRecipientList.add(number);
                                numbersRepository.addNumber(number, type, id);
                            }
                        }
                        count ++;
                    }
                }

                if(count == 0){
                    result.put("status", "FAILED");
                    result.put("response", "File is blank. Please upload a correct file!");
                    String log = "NumberList File is blank - " + fileName;
                    loggingService.logActivity(sourceIP, sessionId, customer, userName, "load_from_csv",log);
                }else {
                    numberListRepository.updateNumberList(id,validRecipientList.size());
                    result.put("status", "SUCCESS");
                    result.put("response", "NumberList uploaded successfully");
                    result.put("count", count);
                    result.put("valid_count", validCount);
                    result.put("invalid_count", invalidCount);
                    result.put("valid_added_count", validRecipientList.size());
                    result.put("duplicate_count", validCount - validRecipientList.size());

                    String log = "NumberList uploaded successfully | count - " + count + " | valid_count - " + validCount + " | valid_added_count - " + validRecipientList.size() + " | invalid_count - " + invalidCount + " | duplicate_count - " + (validCount - validRecipientList.size());
                    loggingService.logActivity(sourceIP, sessionId, customer, userName, "load_from_csv",log);
                }
            }
        } catch (IOException e) {
            result.put("status", "FAILED");
            result.put("response", "Error loading file " + inputFileName + ": " + e.getMessage());
        }
        System.out.println("status" + result.get("status"));
        System.out.println("response" + result.get("response"));
        return result;
    }

    public Object validate_numberList(Integer customer, Integer userId, Integer userType, Integer numberListId) {
        Map<String,Object> result = null;
        if(userType == 0){
            result = numberListRepository.validateNumberList(customer,numberListId);
        } else if (userType == 101 || userType == 10) {
            result = numberListRepository.validateNumberListByUserId(customer,userId,numberListId);
        }
        return result.get("count");
    }

    public String update_numberList(Integer numberListId, String additionalNumber) {
        if(!Objects.equals(additionalNumber, "")){
            //        numberListRepository.update_numberList(numberListId,numberListName);
            numbersRepository.deleteNumbers(numberListId);

            String[] numbers = additionalNumber.split(",");
            int count = 0;

            for (String number : numbers) {
                Map<String,String> result = newSMSService.getRecipientType(number);
                String type = result.get("type");

                if (!"0".equals(type)) {
                    Numbers entity = new Numbers();
                    entity.setMsisdn(number.trim());
                    entity.setType(type);
                    entity.setNumberListId(numberListId);
                    numbersRepository.save(entity);
                    count++;
                }
            }
            numberListRepository.updateNumberList(numberListId,count);
            return "Success";
        }else {
            return "AdditionalNumber Empty";
        }

    }

    public String delete_numberList(int numberListId) {
        List<NumberList> numberLists = numberListRepository.findAllById(Collections.singleton(numberListId));
        List<NumberListArchive> numberListArchives = new ArrayList<>();
        for(NumberList numberList : numberLists){
            NumberListArchive numberListArchive = new NumberListArchive();
            numberListArchive.setId(numberList.getId());
            numberListArchive.setName(numberList.getName());
            numberListArchive.setUser(numberList.getUser());
            numberListArchive.setCustomer(numberList.getCustomer());
            numberListArchive.setSize(numberList.getSize());
            numberListArchive.setCreatedDate(numberList.getCreatedDate());

            numberListArchives.add(numberListArchive);
        }
        numberListArchiveRepository.saveAll(numberListArchives);
        numbersRepository.deleteNumbers(numberListId);
        numberListRepository.deleteById(numberListId);

        return "Success";
    }

    public List<Map<String, Object>> getNumberLists(int customer, int userId, int userType) {
        List<Map<String, Object>> response = new ArrayList<>();
        if(userType == 0){
            response = numberListRepository.get_numberlist(customer);
        }else if(userType == 101 || userType == 10){
            response = numberListRepository.get_numberListWithUserId(customer,userId);
        }
        return response;
    }
}
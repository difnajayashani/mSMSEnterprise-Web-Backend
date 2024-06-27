package lk.mobitel.msmsenterprisewebbeapi.service;

import lk.mobitel.msmsenterprisewebbeapi.model.TrialUserDTO;
import lk.mobitel.msmsenterprisewebbeapi.model.esms.Alias;
import lk.mobitel.msmsenterprisewebbeapi.model.esms.TestUser;
import lk.mobitel.msmsenterprisewebbeapi.model.esms.User;
import lk.mobitel.msmsenterprisewebbeapi.model.esms.UserRole;
import lk.mobitel.msmsenterprisewebbeapi.repository.esms.AliasRepository;
import lk.mobitel.msmsenterprisewebbeapi.repository.esms.TestUserRepository;
import lk.mobitel.msmsenterprisewebbeapi.repository.esms.UserRepository;
import lk.mobitel.msmsenterprisewebbeapi.repository.esms.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.List;

@Service
public class TrialUserService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    UserRoleRepository userRoleRepository;
    @Autowired
    TestUserRepository testUserRepository;
    @Autowired
    AliasRepository aliasRepository;
    public String addTrialUser(TrialUserDTO trialUser) {
        User user = userRepository.getUserByUserName(trialUser.getUserName());
        if (user != null) {
            return "USERNAMEEXISTS";
        } else {
            Date dateCreated = new Date();
            String hashedPassword = hashPassword(trialUser.getPassword());
            Integer response = userRepository.addTrialUserToUser(trialUser.getFirstName(),trialUser.getLastName(),trialUser.getUserName(),trialUser.getEmail(),trialUser.getContactPhone(),104,2147483647,hashedPassword,hashedPassword,dateCreated);

            Integer userId = userRepository.getUserIdByUserName(trialUser.getUserName());

            Date date = new Date();
            testUserRepository.saveTestUser(userId,date,trialUser.getEnd(),trialUser.getM2mAllowance(),trialUser.getM2oAllowance(),trialUser.getIddAllowance(),trialUser.getM2mAllowance(),trialUser.getM2oAllowance(),trialUser.getIddAllowance());

            UserRole userRole = new UserRole();
            userRole.setRoleId(104);
            userRole.setUserId(userId);
            userRoleRepository.save(userRole);

            Alias alias = new Alias();
            alias.setAlias("TEST");
            alias.setCustomer(2147483647L);
            alias.setUserId(userId);
            Timestamp ts = Timestamp.from(Instant.now());
            alias.setDateCreated(ts);
            aliasRepository.save(alias);

            return "SUCCESS";
        }
    }

    public String deleteTrialUserById(Integer userId) {
        if (userRepository.existsById(userId)) {
            Integer id = userRoleRepository.findIdByUserId(userId);
            if (id != null) {
                userRoleRepository.deleteById(id);
            }

            Integer aliasId = aliasRepository.findByUserId(userId);
            if(aliasId != null){
                aliasRepository.deleteById(aliasId);
            }

            Integer testUserId = testUserRepository.findByUserId(userId);
            if(testUserId != null){
                testUserRepository.deleteById(testUserId);
            }

            userRepository.deleteById(userId);

            return "SUCCESS";
        } else {
            return "Trial_USER_NOT_FOUND";
        }
    }

    public List<Object> getAllTrialUsers() {
        java.util.Date utilDate = new java.util.Date();
        java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());
        return userRepository.getAllTrialUsers(sqlDate);
    }

    public String update_trial_user(String firstName, String lastName, String userName, Integer customer, String email, String contactPhone, String password, Integer userId) {
        String hashedPassword = hashPassword(password);
        userRepository.updateUser(firstName,lastName,userName,email,userId,hashedPassword);

        List<UserRole> userRoles = userRoleRepository.getTrialUsers(userId);
        if(userRoles == null){
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(104);
            userRoleRepository.save(userRole);
        }else {
            userRoleRepository.updateUserRole(userId,104);
        }
        return "Success";
    }

    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());

            // Convert byte array to a string
            String hashedPassword = Base64.getEncoder().encodeToString(hash);
            return hashedPassword;

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return password;
        }
    }
}

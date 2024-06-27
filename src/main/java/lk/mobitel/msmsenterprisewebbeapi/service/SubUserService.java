package lk.mobitel.msmsenterprisewebbeapi.service;

import lk.mobitel.msmsenterprisewebbeapi.model.esms.User;
import lk.mobitel.msmsenterprisewebbeapi.model.esms.UserRole;
import lk.mobitel.msmsenterprisewebbeapi.repository.esms.TestUserRepository;
import lk.mobitel.msmsenterprisewebbeapi.repository.esms.UserRepository;
import lk.mobitel.msmsenterprisewebbeapi.repository.esms.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.crypto.Data;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.List;

@Service
public class SubUserService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    TestUserRepository testUserRepository;
    @Autowired
    UserRoleRepository userRoleRepository;

    public String deleteSubUserById(Integer userId) {
        if (userRepository.existsById(userId)) {
            Integer id = userRoleRepository.findIdByUserId(userId);

            if (id != null) {
                userRoleRepository.deleteById(id);
            }

            userRepository.deleteById(userId);

            return "SUCCESS";
        } else {
            return "USER_NOT_FOUND";
        }
    }

    public List<User> getAllSubUsers() {
       return userRepository.getAllSubUsers();
    }

    public String addSubUser(String firstName, String lastName, String userName, String email, String contactPhone, Integer customer) {
        User user = userRepository.getUserByUserName(userName);
        if (user != null) {
            return "USERNAMEEXISTS";
        } else {
            Date currentDate = new Date();
            userRepository.addSubUserToUser(firstName,lastName,userName,customer,contactPhone,email,currentDate,103);
            Integer userId = userRepository.getUserIdByUserName(userName);

            UserRole userRole = new UserRole();
            userRole.setRoleId(103);
            userRole.setUserId(userId);
            userRoleRepository.save(userRole);
            return "SUCCESS";
        }
    }

    public String update_sub_user(String firstName, String lastName, String userName, Integer customer, String email, String contactPhone, Integer userId) {
        userRepository.updateSubUser(firstName,lastName,userName,email,userId);
//        testUserRepository.updateTestUser(userId);

        List<UserRole> userRoles = userRoleRepository.getSubUsers(userId);
        if(userRoles == null){
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(103);
            userRoleRepository.save(userRole);
        }else {
            userRoleRepository.updateUserRole(userId,103);
        }
        return "Success";
    }
}

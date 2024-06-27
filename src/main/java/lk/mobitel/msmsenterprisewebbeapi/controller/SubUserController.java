package lk.mobitel.msmsenterprisewebbeapi.controller;
import lk.mobitel.msmsenterprisewebbeapi.model.esms.User;
import lk.mobitel.msmsenterprisewebbeapi.service.SubUserService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
public class SubUserController {
    @Autowired
    SubUserService subUserService;

    @PostMapping("/addSubUser")
    public ResponseEntity<?> addSubUser(@RequestBody SubUserRequest subUserRequest){
        String response = subUserService.addSubUser(subUserRequest.getFirstName(),subUserRequest.getLastName(),subUserRequest.getUserName(),subUserRequest.getEmail(),subUserRequest.getContactPhone(),subUserRequest.getCustomer());

        if ("SUCCESS".equals(response)) {
            System.out.println("Sub user added");
            return ResponseEntity.ok(response);
        } else {
            System.out.println(response);
            return ResponseEntity.status(300).body(response);
        }

    }

    @DeleteMapping("/deleteSubUser")
    public ResponseEntity<String> deleteSubUser(@RequestHeader HttpHeaders headers) {
        int userId = Integer.parseInt(Objects.requireNonNull(headers.getFirst("id")));
        String response = subUserService.deleteSubUserById(userId);

        if ("SUCCESS".equals(response)) {
            return new ResponseEntity<>("User deleted successfully", HttpStatus.OK);
        } else if ("USER_NOT_FOUND".equals(response)) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>("Failed to delete user", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/getAllSubUsers")
    public ResponseEntity<List<User>> getAllSubUsers() {
        List<User> subUsers = subUserService.getAllSubUsers();

        if (subUsers != null) {
            return new ResponseEntity<>(subUsers, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/editSubUser")
    public ResponseEntity<?> editSubUser(@RequestBody SubUserRequest subUserRequest){
        String response = subUserService.update_sub_user(subUserRequest.getFirstName(),subUserRequest.getLastName(),subUserRequest.getUserName(),subUserRequest.getCustomer(),subUserRequest.getEmail(),subUserRequest.getContactPhone(),subUserRequest.getUserId());
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
    public static class SubUserRequest{
        private Integer userId;
        private String firstName;
        private String lastName;
        private String email;
        private String userName;
        private String contactPhone;
        private Integer customer;
    }
}

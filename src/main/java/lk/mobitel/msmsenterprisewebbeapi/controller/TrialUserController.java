package lk.mobitel.msmsenterprisewebbeapi.controller;

import lk.mobitel.msmsenterprisewebbeapi.model.TrialUserDTO;
import lk.mobitel.msmsenterprisewebbeapi.service.TrialUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
public class TrialUserController {
    @Autowired
    TrialUserService trialUserService;

    @PostMapping("/addTrialUser")
    public ResponseEntity<?> addTrialUser(@RequestBody TrialUserDTO trialUser){
        String response = trialUserService.addTrialUser(trialUser);

        if ("SUCCESS".equals(response)) {
            System.out.println("Trial user added");
            return ResponseEntity.ok(response);
        } else {
            System.out.println(response);
            return ResponseEntity.status(300).body(response);
        }

    }

    @DeleteMapping("/deleteTrialUser")
    public ResponseEntity<String> deleteTrialUser(@RequestHeader HttpHeaders headers) {
        int userId = Integer.parseInt(Objects.requireNonNull(headers.getFirst("id")));
        String response = trialUserService.deleteTrialUserById(userId);

        if ("SUCCESS".equals(response)) {
            return new ResponseEntity<>("User deleted successfully", HttpStatus.OK);
        } else if ("USER_NOT_FOUND".equals(response)) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>("Failed to delete user", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/getAllTrialUsers")
    public ResponseEntity<List<Object>> getAllTrialUsers() {
        List<Object> trialUsers = trialUserService.getAllTrialUsers();

        if (trialUsers != null) {
            return new ResponseEntity<>(trialUsers, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/editTrialUser")
    public ResponseEntity<?> editTrialUser(@RequestBody TrialUserDTO trialUserDTO){
        String response = trialUserService.update_trial_user(trialUserDTO.getFirstName(),trialUserDTO.getLastName(),trialUserDTO.getUserName(),trialUserDTO.getCustomer(),trialUserDTO.getEmail(),trialUserDTO.getContactPhone(),trialUserDTO.getPassword(),trialUserDTO.getId());
        if(Objects.equals(response, "Success")){
            return ResponseEntity.ok(response);
        }else {
            return ResponseEntity.status(300).body(response);
        }
    }
}

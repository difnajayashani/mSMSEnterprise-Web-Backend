package lk.mobitel.msmsenterprisewebbeapi.controller;

import lk.mobitel.msmsenterprisewebbeapi.model.esms.Component;
import lk.mobitel.msmsenterprisewebbeapi.model.esms.ComponentRole;
import lk.mobitel.msmsenterprisewebbeapi.model.esms.Role;
import lk.mobitel.msmsenterprisewebbeapi.model.esms.UserRole;
import lk.mobitel.msmsenterprisewebbeapi.service.ComponentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class ComponentsController {
    @Autowired
    ComponentService componentService;

    @PostMapping("/addComponent")
    public ResponseEntity<Component> addComponent(@RequestBody Component component){
        Component component1 = componentService.addComponent(component);

        if (component1 != null){
            System.out.println("component added");
            return new ResponseEntity<>(HttpStatus.OK);
        }else {
            System.out.println("Failed");
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/viewComponent")
    public Iterable<Component> getAllComponents(){return componentService.getAllComponents();}

    @PostMapping("/addRole")
    public ResponseEntity<Role> addRole(@RequestBody Role role){
        Role role1 = componentService.addRole(role);

        if(role1 != null){
            System.out.println("Role added");
            return new ResponseEntity<>(HttpStatus.OK);
        }else {
            System.out.println("Failed");
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/viewRole")
    public Iterable<UserRole> getAllRoles(){return componentService.getAllRoles();}

    @PostMapping("/addComponentRole")
    public ResponseEntity<ComponentRole> addComponentRole(@RequestBody ComponentRole componentRole){
        ComponentRole componentRole1 = componentService.addComponentRole(componentRole);

        if (componentRole1 != null){
            System.out.println("ComponentRole added");
            return new ResponseEntity<>(HttpStatus.OK);
        }else {
            System.out.println("Failed");
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/viewComponentRole")
    public Iterable<ComponentRole> getAllComponentRoles(){return componentService.getAllComponentRoles();}

    @PostMapping("/addUserRole")
    public ResponseEntity<UserRole> addUserRole(@RequestBody UserRole userRole){
        UserRole userRole1 = componentService.addUserRole(userRole);

        if(userRole1 != null){
            System.out.println("UserRole added");
            return new ResponseEntity<>(HttpStatus.OK);
        }else {
            System.out.println("Failed");
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/viewUserRole")
    public Iterable<UserRole> getAllUserRoles(){return componentService.getAllUserRoles();}

    @GetMapping("/viewComponents/{userId}")
    public List<Object[]> getByUserId(@PathVariable Integer userId) {
        return componentService.getComponentsById(userId);
    }

    @GetMapping("/user-roles")
    public Object getUserRolesWithRoleData() {
        List<Object> f1 = componentService.getUserRolesWithRoleData();
        Map<String, Object> f2 = (Map<String, Object>) f1.get(0);
        Object roleId = f2.get("roleId");
        return roleId;
    }


}

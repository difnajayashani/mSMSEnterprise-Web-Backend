package lk.mobitel.msmsenterprisewebbeapi.service;

import lk.mobitel.msmsenterprisewebbeapi.model.esms.Component;
import lk.mobitel.msmsenterprisewebbeapi.model.esms.ComponentRole;
import lk.mobitel.msmsenterprisewebbeapi.model.esms.Role;
import lk.mobitel.msmsenterprisewebbeapi.model.esms.UserRole;
import lk.mobitel.msmsenterprisewebbeapi.repository.esms.ComponentRepository;
import lk.mobitel.msmsenterprisewebbeapi.repository.esms.ComponentRoleRepository;
import lk.mobitel.msmsenterprisewebbeapi.repository.esms.RoleRepository;
import lk.mobitel.msmsenterprisewebbeapi.repository.esms.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ComponentService {
    @Autowired
    ComponentRepository componentRepository;
    @Autowired
    ComponentRoleRepository componentRoleRepository;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    UserRoleRepository userRoleRepository;

    public Component addComponent(Component component) {
        return componentRepository.save(component);
    }

    public Iterable<Component> getAllComponents() {
        return componentRepository.findAll();
    }

    public Role addRole(Role role) {
        return roleRepository.save(role);
    }

    public Iterable<UserRole> getAllRoles() {
        return userRoleRepository.findAll();
    }

    public ComponentRole addComponentRole(ComponentRole componentRole) {
        return componentRoleRepository.save(componentRole);
    }

    public Iterable<ComponentRole> getAllComponentRoles() {
        return componentRoleRepository.findAll();
    }

    public UserRole addUserRole(UserRole userRole) {
        return userRoleRepository.save(userRole);
    }

    public Iterable<UserRole> getAllUserRoles() {
        return userRoleRepository.findAll();
    }

    public List<Object[]> getComponentsById(Integer userId) {
        UserRole userRole = userRoleRepository.getUserRoleById(userId);
        return componentRepository.findComponentsByRoleId(userRole.getRoleId());
    }

    public List<Object> getUserRolesWithRoleData() {
        return componentRepository.getUserRolesWithRoleData();
    }
}

package lk.mobitel.msmsenterprisewebbeapi.repository.esms;

import lk.mobitel.msmsenterprisewebbeapi.model.esms.Component;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComponentRepository extends JpaRepository<Component, Integer> {
    @Query("SELECT c, cr FROM Component c JOIN ComponentRole cr ON c.componentId = cr.componentId WHERE cr.roleId = :roleId")
    List<Object[]> findComponentsByRoleId(@Param("roleId") Integer roleId);

    @Query("SELECT new map(ur.userId as userId, ur.roleId as roleId, r.name as roleName) FROM UserRole ur INNER JOIN Role r ON ur.roleId = r.id")
    List<Object> getUserRolesWithRoleData();
}
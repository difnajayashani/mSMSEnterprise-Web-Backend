package lk.mobitel.msmsenterprisewebbeapi.repository.esms;

import jakarta.transaction.Transactional;
import lk.mobitel.msmsenterprisewebbeapi.model.esms.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole,Integer> {

    @Transactional
    @Modifying
    @Query("INSERT INTO UserRole(roleId, userId) VALUES (:roleId, :userId)")
    void addToUserRole(@Param("roleId") Integer roleId, @Param("userId") Integer userId);

    @Query("SELECT ur FROM UserRole ur WHERE ur.userId = :userId")
    UserRole getUserRoleById(@Param("userId") Integer userId);

    @Query("SELECT ur.id FROM UserRole ur WHERE ur.userId = :userId")
    Integer findIdByUserId(@Param("userId") Integer userId);

    @Query("SELECT ur FROM UserRole ur WHERE ur.userId = :userId")
    List<UserRole> getSubUsers(@Param("userId") Integer userId);

    @Transactional
    @Modifying
    @Query("UPDATE UserRole SET roleId = :i WHERE userId = :userId")
    void updateUserRole(@Param("userId") Integer userId,@Param("i") int i);

    @Query("SELECT ur FROM UserRole ur WHERE ur.userId = :userId")
    List<UserRole> getTrialUsers(@Param("userId") Integer userId);
}

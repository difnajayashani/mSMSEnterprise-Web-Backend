package lk.mobitel.msmsenterprisewebbeapi.repository.esms;

import jakarta.transaction.Transactional;
import lk.mobitel.msmsenterprisewebbeapi.model.esms.TestUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.Date;
import java.util.List;
import java.util.Map;

@Repository
public interface TestUserRepository extends JpaRepository<TestUser,Integer> {
    @Query("SELECT tu.id FROM TestUser tu WHERE tu.user = :userId")
    Integer findByUserId(@Param("userId") Integer userId);

    @Query("SELECT CASE WHEN t.end < CURRENT_TIMESTAMP THEN true ELSE false END AS expired FROM TestUser t WHERE t.user = :userId")
    List<Map<String, Object>> isTestUserExpired(@Param("userId") Integer userId);

    @Query("SELECT tu.m2mBalance AS m2mBalance, tu.m2oBalance AS m2oBalance, tu.iddBalance AS iddBalance FROM TestUser tu WHERE tu.user = :userId")
    List<Map<String, Object>> getTestUserDetails(@Param("userId") Integer userId);

    @Transactional
    @Modifying
    @Query("UPDATE TestUser tu SET tu.m2mBalance = (m2mBalance - 1) WHERE tu.user = :userId")
    void UpdateTUM2MBalance(@Param("userId") Integer userId);

    @Transactional
    @Modifying
    @Query("UPDATE TestUser tu SET tu.m2oBalance = (m2oBalance - 1) WHERE tu.user = :userId")
    void UpdateTUM20Balance(@Param("userId") Integer userId);

    @Transactional
    @Modifying
    @Query("UPDATE TestUser tu SET tu.iddBalance = (iddBalance - 1) WHERE tu.user = :userId")
    void UpdateTUIDDBalance(Integer userId);

    @Transactional
    @Modifying
    @Query("INSERT INTO TestUser(user, start, end, m2m, m2o,idd,m2mBalance,m2oBalance,iddBalance) " +
            "VALUES (:id, :start, :end, :m2m, :m2o, :idd, :m2mAllowance, :m2oAllowance, :iddAllowance)")
    void saveTestUser(@Param("id") Integer id, @Param("start")Date start, @Param("end") Date end, @Param("m2m") Integer m2m, @Param("m2o") Integer m2o, @Param("idd") Integer idd, @Param("m2mAllowance") Integer m2mAllowance, @Param("m2oAllowance") Integer m2oAllowance, @Param("iddAllowance") Integer iddAllowance);
}

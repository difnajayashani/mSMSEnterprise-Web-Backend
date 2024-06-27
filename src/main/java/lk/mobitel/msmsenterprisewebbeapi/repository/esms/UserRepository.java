package lk.mobitel.msmsenterprisewebbeapi.repository.esms;

import jakarta.transaction.Transactional;
import lk.mobitel.msmsenterprisewebbeapi.model.esms.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Repository
public interface UserRepository extends JpaRepository<User,Integer> {
    @Query("SELECT u FROM User u WHERE u.userName = :userName")
    User findByUserName(@Param("userName") String userName);

    @Query("SELECT u.contactPhone FROM User u WHERE u.customer = :customer AND u.id = :userId")
    String getCustomerContact(@Param("customer") Integer customer, @Param("userId") Integer userId);

    @Transactional
    @Modifying
    @Query("INSERT INTO User(firstName, lastName, userName, password, email) " +
            "VALUES (:firstName, :lastName, :userName, :hashedPassword, :email)")
    void addToUser(
            @Param("firstName") String firstName,
            @Param("lastName") String lastName,
            @Param("userName") String userName,
            @Param("hashedPassword") String password,
            @Param("email") String email
    );

    @Transactional
    @Modifying
    @Query("INSERT INTO User(firstName, lastName, userName, email, customer, contactPhone, dateCreated, type) " +
            "VALUES (:firstName, :lastName, :userName, :email, :customer, :contactPhone, :currentDate, :type)")
    void addSubUserToUser(@Param("firstName") String firstName, @Param("lastName") String lastName, @Param("userName") String userName, @Param("customer") Integer customer, @Param("contactPhone") String contactPhone, @Param("email") String email, @Param("currentDate") Date currentDate, @Param("type") int type);

    @Transactional
    @Modifying
    @Query("INSERT INTO User(firstName, lastName, userName, email, password, webPassword, contactPhone, type, dateCreated, customer) " +
            "VALUES (:firstName, :lastName, :userName, :email, :password, :password1, :contactPhone, :type, :dateCreated, :customer)")
    Integer addTrialUserToUser(@Param("firstName") String firstName, @Param("lastName") String lastName, @Param("userName") String userName, @Param("email") String email, @Param("contactPhone") String contactPhone, @Param("type") int type, @Param("customer") int customer, @Param("password") String password, @Param("password1") String password1, @Param("dateCreated") Date dateCreated);


    @Query("SELECT u FROM User u WHERE u.userName = :userName")
    User getUserByUserName(@Param("userName") String userName);

    @Query("SELECT u FROM User u WHERE u.type = 103")
    List<User> getAllSubUsers();

    @Query("SELECT u, ur, r, tu, CASE WHEN tu.end > :sqlDate THEN 'ACTIVE' ELSE 'EXPIRED' END AS account_status FROM User u INNER JOIN UserRole ur ON u.id = ur.userId INNER JOIN Role r ON r.id = ur.roleId INNER JOIN TestUser tu ON tu.user = u.id WHERE u.type = 104 ORDER BY u.id DESC")
    List<Object> getAllTrialUsers(@Param("sqlDate") java.sql.Date sqlDate);

    @Query("SELECT u.id AS userId, cp.accountNo AS firstName, cp.name AS lastName, u.userName AS userName, u.password AS password, u.email AS email, r.name AS roleName, r.id AS roleId FROM User u LEFT JOIN UserRole ur ON u.id = ur.userId LEFT JOIN Role r ON ur.roleId = r.id LEFT JOIN CustomerProfile cp ON u.customer = cp.accountNo WHERE u.type != 101 ORDER BY cp.dateCreated")
    List<Map<String,Object>> getAllUsers();

    @Query("SELECT u.enableLock AS enableLock, u.dateLoginLock AS lockTime ,u.enableLoginOTP AS enableLoginOTP ,u.enableWebUI AS enableWebUI , u.id AS id, u.customer AS customer, u.type AS type, u.status AS user_active_status, cp.status AS active_status , cp.haltServices AS halt_status FROM User u INNER JOIN CustomerProfile cp ON cp.accountNo = u.customer WHERE u.userName = :userName")
    Map<String, Object> verifyUserName(@Param("userName") String userName);

    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.enableLock = 1, u.dateLoginLock = CURRENT_TIMESTAMP WHERE u.userName = :userName")
    Integer lockLogin(String userName);

    @Transactional
    @Modifying
    @Query("UPDATE User SET enableLock = 0 WHERE userName = :userName")
    void unlockLogin(@Param("userName") String userName);

    @Query("SELECT datePwResetWeb as pwResetWeb, forcePWReset as forcePWReset FROM User WHERE userName = :userName")
    Map<String, Object> getLatestPwReset(@Param("userName") String userName);

    @Query("SELECT new map(CASE WHEN t.end < CURRENT_TIMESTAMP THEN true ELSE false END) FROM TestUser t WHERE t.id = :userId")
    List<Object> isExpired(@Param("userId") Object userId);

    @Query("SELECT u.id as userId, u.firstName as firstName, u.lastName as lastname, u.customer as customer, u.type as userType, cp.enableIdd as EnableIDD, cp.enableM2o as EnableM2O, cp.enableM2all as EnableM2ALL, cp.enableAdStop as EnableAdStop FROM User u INNER JOIN CustomerProfile cp ON cp.accountNo = u.customer WHERE u.userName = :userName AND u.webPassword = :password AND cp.status = 1 AND (cp.haltServices != 1 OR cp.haltServices IS NULL)")
    Map<String, Object> verifyUser(@Param("userName") String userName,@Param("password") String password);

    @Transactional
    @Modifying
    @Query("UPDATE User SET firstName = :firstName, lastName = :lastName, email = :email, userName = :userName, password = :hashedPassword WHERE id = :userId")
    void updateUser(@Param("firstName") String firstName, @Param("lastName") String lastName, @Param("userName") String userName, @Param("email") String email, @Param("userId") Integer userId, @Param("hashedPassword") String hashedPassword);

    @Query("SELECT u.userName as UNAME FROM User u WHERE u.customer = :accountNo")
    List<Map<String,String>> get_user_logins(@Param("accountNo") Integer accountNo);

    @Query("SELECT u.id AS userId, u.firstName AS firstName, u.lastName AS lastName, u.customer AS customer, u.type AS type, cp.enableIdd AS EnableIDD, cp.enableM2o AS EnableM2O, cp.enableM2all AS EnableM2ALL, cp.enableAdStop as EnableAdStop FROM User u INNER JOIN CustomerProfile cp ON cp.accountNo = u.customer WHERE u.userName = :userName AND u.customer = :customer")
    Map<String, Object> verified_user_data(@Param("userName") String userName, @Param("customer") Integer customer);

    @Transactional
    @Modifying
    @Query("UPDATE User SET password = :newPassword, datePwResetApi = :localDateTime WHERE id = :userId")
    Integer updatePasswordResetApi(@Param("newPassword") String newPassword, @Param("localDateTime") LocalDateTime localDateTime, @Param("userId") Integer userId);

    @Transactional
    @Modifying
    @Query("UPDATE User SET webPassword = :newPassword, datePwResetWeb = :localDateTime WHERE id = :userId")
    Integer updatePasswordResetWeb(String newPassword, LocalDateTime localDateTime, Integer userId);

    @Transactional
    @Modifying
    @Query("UPDATE User SET password = :newPassword, webPassword = :newPassword, datePwResetApi = :localDateTime, datePwResetWeb = :localDateTime WHERE id = :userId")
    Integer updatePasswordResetApiAndWeb(String newPassword, LocalDateTime localDateTime, Integer userId);

    @Query("SELECT u.id FROM User u WHERE u.userName = :userName")
    Integer getUserIdByUserName(@Param("userName") String userName);

    @Transactional
    @Modifying
    @Query("UPDATE User SET firstName = :firstName, lastName = :lastName, email = :email, userName = :userName WHERE id = :userId")
    void updateSubUser(@Param("firstName") String firstName, @Param("lastName") String lastName, @Param("userName") String userName, @Param("email") String email, @Param("userId") Integer userId);
}
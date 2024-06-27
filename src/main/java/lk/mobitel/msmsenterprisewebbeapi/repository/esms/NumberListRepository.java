package lk.mobitel.msmsenterprisewebbeapi.repository.esms;

import jakarta.transaction.Transactional;
import lk.mobitel.msmsenterprisewebbeapi.model.esms.NumberList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Repository
public interface NumberListRepository extends JpaRepository<NumberList,Integer> {
    @Query("SELECT nl FROM NumberList nl WHERE nl.customer = :customer ORDER BY nl.id ASC")
    List<NumberList> getAllNumberListsForCustomer(@Param("customer") Integer customer);

    @Query("SELECT nl FROM NumberList nl WHERE nl.customer = :customer AND nl.user = :user ORDER BY nl.id ASC")
    List<NumberList> getAllNumberListsForUser(@Param("customer") Integer customer, @Param("user") Integer user);

    @Query("SELECT nl FROM NumberList nl WHERE nl.customer = :customer AND nl.name = :name")
    NumberList numberListExist(@Param("customer") Integer customer, @Param("name") String name);

    @Transactional
    @Modifying
    @Query("INSERT INTO NumberList(name, customer, user, createdDate) " +
            "VALUES (:name, :customer, :userId, :date)")
    void insertNumberList(@Param("name") String name, @Param("customer") Integer customer, @Param("userId") Integer userId, @Param("date") Date date);

    @Transactional
    @Modifying
    @Query("UPDATE NumberList nl SET nl.size = :validCount WHERE nl.id = :id")
    void updateNumberList(@Param("id") int id, @Param("validCount") int validCount);

    @Query("SELECT count(nl.id) AS count FROM NumberList nl WHERE nl.customer = :customer AND nl.id = :numberListId")
    Map<String, Object> validateNumberList(@Param("customer") Integer customer, @Param("numberListId") Integer numberListId);

    @Query("SELECT count(nl.id) AS count FROM NumberList nl WHERE nl.customer = :customer AND nl.user = :userId AND nl.id = :numberListId")
    Map<String, Object> validateNumberListByUserId(@Param("customer") Integer customer, @Param("userId") Integer userId, @Param("numberListId") Integer numberListId);

    @Transactional
    @Modifying
    @Query("UPDATE NumberList nl SET nl.name = :numberListName WHERE nl.id = :numberListId")
    void update_numberList(@Param("numberListId") Integer numberListId, @Param("numberListName") String numberListName);

    @Query("SELECT nl.name AS name, nl.size AS size, nl.createdDate AS createdDate, nl.customer AS customer, nl.user AS user, u.userName AS userName FROM NumberList nl INNER JOIN User u ON nl.user = u.id WHERE nl.customer = :customer")
    List<Map<String, Object>> get_numberlist(@Param("customer") int customer);

    @Query("SELECT nl.id AS id, nl.name AS name, nl.size AS size, nl.createdDate AS createdDate, nl.customer AS customer, nl.user AS user, u.userName AS userName FROM NumberList nl INNER JOIN User u ON nl.user = u.id WHERE nl.customer = :customer AND nl.user = :userId")
    List<Map<String, Object>> get_numberListWithUserId(@Param("customer") int customer, @Param("userId") int userId);
}

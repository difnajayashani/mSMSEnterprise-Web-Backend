package lk.mobitel.msmsenterprisewebbeapi.repository.esms;

import lk.mobitel.msmsenterprisewebbeapi.model.esms.CgMonthlySmsCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Repository
public interface CgMonthlySmsCountRepository extends JpaRepository<CgMonthlySmsCount,Integer> {
    @Query("SELECT csc FROM CgMonthlySmsCount csc WHERE csc.customer = :accountNo AND csc.endDate = :billingMonth")
    List<CgMonthlySmsCount> get_monthly_usage(@Param("accountNo") String accountNo, @Param("billingMonth") LocalDate billingMonth);

    @Query("SELECT csc FROM CgMonthlySmsCount csc WHERE csc.customer = :accountNo")
    List<CgMonthlySmsCount> get_monthly_usage_perCustomer(@Param("accountNo") String accountNo);

    @Query("SELECT csc FROM CgMonthlySmsCount csc WHERE csc.endDate = :billingMonth")
    List<CgMonthlySmsCount> get_monthly_usage_perMonth(@Param("billingMonth") LocalDate billingMonth);
}

package lk.mobitel.msmsenterprisewebbeapi.repository.esms;

import lk.mobitel.msmsenterprisewebbeapi.model.esms.NumberListArchive;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NumberListArchiveRepository extends JpaRepository<NumberListArchive,Integer> {
}

package lk.mobitel.msmsenterprisewebbeapi.repository.esms;

import lk.mobitel.msmsenterprisewebbeapi.model.esms.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role,Integer> {

}

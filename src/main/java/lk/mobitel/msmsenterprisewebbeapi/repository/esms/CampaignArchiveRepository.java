package lk.mobitel.msmsenterprisewebbeapi.repository.esms;

import jakarta.transaction.Transactional;
import lk.mobitel.msmsenterprisewebbeapi.model.esms.CampaignArchive;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CampaignArchiveRepository extends JpaRepository<CampaignArchive, Integer> {
    @Transactional
    @Modifying
    @Query(value = "INSERT INTO CampaignArchive SELECT c FROM Campaign WHERE c.id = :campaignId", nativeQuery = true)
    void insertIntoCampaignArchive(@Param("campaignId") int campaignId);
}

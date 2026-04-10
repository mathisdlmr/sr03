package fr.utc.sr03.repository;

import fr.utc.sr03.model.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, Integer> {

    @Query("select i from Invitation i where i.user.id = ?1")
    List<Invitation> findByUserId(int creator_id);

}

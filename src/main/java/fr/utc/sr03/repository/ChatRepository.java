package fr.utc.sr03.repository;

import fr.utc.sr03.model.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Integer> {

    @Query("select c from Chat c where c.creator.id = ?1")
    List<Chat> findByCreatorId(int creator_id);

    @Query("select c from Chat c join c.invitations i where i.user.id = ?1")
    List<Chat> findByInvitationUserId(int user_id);

    // Etant donné l'unicité sur l'id chat_id, soit on récupère un résultat (1=true) soit aucun (0=false)
    @Query("select count(c) from Chat c where c.id = ?1 and c.creator.id = ?2")
    Boolean isOwner(int chat_id, int user_id);
}

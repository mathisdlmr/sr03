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

}

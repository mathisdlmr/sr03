package fr.utc.sr03.repository;

import fr.utc.sr03.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<Users, Integer>{

    @Query("select u from Users u where u.mail = ?1")
    Users findByEmailAddress(String emailAddress);

}

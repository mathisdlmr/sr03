package fr.utc.sr03.repository;

import fr.utc.sr03.model.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<Users, Integer> {

    @Query("select u from Users u where u.mail = ?1")
    Users findByEmailAddress(String emailAddress);

    @Query(value = "select u from Users u where u.active = ?1 and (u.mail like ?2 or u.firstname like ?2 or u.lastname like ?2)",
           countQuery = "select count(u) from Users u where u.active = ?1 and (u.mail like ?2 or u.firstname like ?2 or u.lastname like ?2)")
    Page<Users> findByActiveAndSearch(boolean active, String search, Pageable pageable);

    @Query("select u from Users u where u.id = ?1 and u.admin=?2")
    List<Users> findAdminUsersById(Integer userId, Boolean isAdmin);

    @Query("select u from Users u where u.mail = ?1 and u.admin=?2")
    List<Users> findAdminUsersByEmail(String emailAddress, Boolean isAdmin);

    @Query("select u from Users u where u.id = ?1 and u.active=?2")
    List<Users> findActiveUsersById(Integer userId, Boolean isActive);

    @Query("select u from Users u where u.mail = ?1 and u.active=?2")
    List<Users> findActiveUsersByEmail(String emailAddress, Boolean isActive);

    @Modifying
    @Query("delete from Users u where u.mail = ?1")
    void deleteUserByEmail(String emailAddress);
}

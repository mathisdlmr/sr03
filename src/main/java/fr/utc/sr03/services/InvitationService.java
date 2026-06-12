package fr.utc.sr03.services;

import fr.utc.sr03.model.Invitation;
import fr.utc.sr03.model.Users;
import fr.utc.sr03.repository.InvitationRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InvitationService {

    @Resource
    private InvitationRepository invitationRepository;

    // CREATE or UPDATE
    public void saveInvitation(Invitation invitation) {
        invitationRepository.save(invitation);
    }

    // READ
    public Invitation getInvitationById(int id) {
        return invitationRepository.findById(id).orElse(null);
    }

    public Invitation getInvitationByChatAndUserId(int chat_id, int user_id) {
        return invitationRepository.findInvitationByChatAndUserId(chat_id, user_id);
    }

    public List<Invitation> getInvitationByUserId(int user_id) {
        return invitationRepository.findByUserId(user_id);
    }

    public List<Invitation> getInvitationByChatId(int chat_id) {
        return invitationRepository.findByChatId(chat_id);
    }

    public List<Users> getInvitedUsersByChatId(int chat_id) {
        return invitationRepository.findInvitedUsersByChatId(chat_id);
    }

    public List<Invitation> getAllInvitations() {
        return invitationRepository.findAll();
    }

    // DELETE
    public void deleteInvitation(int id) {
        invitationRepository.deleteById(id);
    }

    // OTHER METHODS
    public boolean isInvited(int chat_id, int user_id) {
        return invitationRepository.countInvited(chat_id, user_id) > 0;
    }
}

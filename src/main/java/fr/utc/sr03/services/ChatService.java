package fr.utc.sr03.services;

import fr.utc.sr03.model.Chat;
import fr.utc.sr03.repository.ChatRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatService {

    @Resource
    private ChatRepository chatRepository;

    // CREATE
    public void saveChat(Chat chat) {
        chatRepository.save(chat);
    }

    // READ
    public Chat getChatById(int id) {
        return chatRepository.findById(id).orElse(null);
    }

    public List<Chat> getChatByCreatorId(int creator_id) {
        return chatRepository.findByCreatorId(creator_id);
    }

    public List<Chat> getChatsByInvitations(int user_id) { return chatRepository.findByInvitationUserId(user_id); }

    public List<Chat> getAllChats() {
        return chatRepository.findAll();
    }

    // DELETE
    public void deleteChat(int id) {
        chatRepository.deleteById(id);
    }

    // OTHER METHODS
    public boolean isOwner(int chat_id, int user_id)  { return chatRepository.isOwner(chat_id, user_id); }
}

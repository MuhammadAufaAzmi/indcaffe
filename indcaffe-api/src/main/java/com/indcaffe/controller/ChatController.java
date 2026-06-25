package com.indcaffe.controller;

import com.indcaffe.entity.Message;
import com.indcaffe.entity.User;
import com.indcaffe.repository.MessageRepository;
import com.indcaffe.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ChatController {

    @Autowired
    MessageRepository messageRepository;

    @Autowired
    UserRepository userRepository;

    @GetMapping("/{userId1}/{userId2}")
    public ResponseEntity<?> getConversation(@PathVariable Long userId1, @PathVariable Long userId2) {
        List<Message> messages = messageRepository.findConversation(userId1, userId2);
        
        List<Map<String, Object>> response = messages.stream().map(m -> Map.of(
            "id", m.getId(),
            "senderId", m.getSender().getId(),
            "receiverId", m.getReceiver().getId(),
            "text", m.getText(),
            "timestamp", m.getTimestamp().toString(),
            "senderRole", m.getSender().getRole().toString()
        )).collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<?> sendMessage(@RequestBody Map<String, Object> payload) {
        Long senderId = Long.valueOf(payload.get("senderId").toString());
        Long receiverId = Long.valueOf(payload.get("receiverId").toString());
        String text = payload.get("text").toString();

        User sender = userRepository.findById(senderId).orElseThrow();
        User receiver = userRepository.findById(receiverId).orElseThrow();

        Message message = Message.builder()
                .sender(sender)
                .receiver(receiver)
                .text(text)
                .timestamp(LocalDateTime.now())
                .isRead(false)
                .build();

        Message savedMessage = messageRepository.save(message);

        return ResponseEntity.ok(Map.of(
            "id", savedMessage.getId(),
            "senderId", savedMessage.getSender().getId(),
            "receiverId", savedMessage.getReceiver().getId(),
            "text", savedMessage.getText(),
            "timestamp", savedMessage.getTimestamp().toString(),
            "senderRole", savedMessage.getSender().getRole().toString()
        ));
    }
    
    @GetMapping("/partners/{userId}")
    public ResponseEntity<?> getChatPartners(@PathVariable Long userId) {
        // Return a list of users that this user has chatted with, or just all other users for simplicity since this is an MVP
        // For IndCaffe, Cafes chat with Mitras. So if user is CAFE, return all MITRAs. If user is MITRA, return all CAFEs.
        User user = userRepository.findById(userId).orElseThrow();
        List<User> partners;
        if ("CAFE".equals(user.getRole().toString())) {
            partners = userRepository.findAll().stream().filter(u -> "MITRA".equals(u.getRole().toString())).collect(Collectors.toList());
        } else {
            partners = userRepository.findAll().stream().filter(u -> "CAFE".equals(u.getRole().toString())).collect(Collectors.toList());
        }
        
        List<Map<String, Object>> response = partners.stream().map(p -> Map.of(
            "id", p.getId(),
            "username", p.getUsername(),
            "role", p.getRole().toString()
        )).collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }
}

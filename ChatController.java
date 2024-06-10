package com.virus.chatbot;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Controller
public class ChatController {

    private final OpenNLPChatBot openNLPChatBot;

    public ChatController(OpenNLPChatBot openNLPChatBot) {
        this.openNLPChatBot = openNLPChatBot;
    }

    @PostMapping("/send-message")
    public ResponseEntity<ChatMessage> sendMessage(@RequestBody ChatMessage message) {
        try {
            String userInput = message.getMessage();
            String botResponse = openNLPChatBot.processInput(userInput);
            ChatMessage responseMessage = new ChatMessage(botResponse);
            return ResponseEntity.ok(responseMessage);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ChatMessage("Internal Server Error"));
        }
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
    }
}

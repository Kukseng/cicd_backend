//package kh.edu.cstad.stackquizapi.controller;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.messaging.handler.annotation.MessageMapping;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import org.springframework.stereotype.Controller;
//
//@Controller
//@RequiredArgsConstructor
//public class GameController {
//
//    private final SimpMessagingTemplate messagingTemplate;
//
//    @MessageMapping("/start-quiz")
//    public void startQuiz() {
//        String question = "What is 2 + 2?";
//        messagingTemplate.convertAndSend("/topic/questions", question);
//    }
//}

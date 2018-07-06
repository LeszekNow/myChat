package com.leszeknowinski.myChat.models;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

@Component
public class ChatSocket extends TextWebSocketHandler implements WebSocketConfigurer {

    private List<UserModel> userList = new ArrayList<>();

    private java.util.Queue<String> lastTenMessages = new ArrayDeque<>();

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry webSocketHandlerRegistry) {
        webSocketHandlerRegistry.addHandler(this, "/room").setAllowedOrigins("*"); // all ip adresses may connect to our socket
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        userList.add(new UserModel(session));
        lastTenMessages.forEach(s -> {
            try {
                session.sendMessage(new TextMessage(s));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        userList.remove(userList.stream().filter(s -> s.getUserSession().getId().equals(session.getId())).findAny().orElseThrow(IllegalStateException::new));

    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
      UserModel sender = findBySession(session);

       if(message.getPayload().startsWith("nickname:")){
           if(sender.getNickname() == null){
               sender.setNickname(message.getPayload().replace("nickname", ""));
           }else {
               sender.sendMessage(new TextMessage("You cannot change nickname any more!"));
           }
           return;
       }

       if(sender.getNickname() == null){
           sender.sendMessage(new TextMessage("Set nickname first!"));
           return;
       }


        addMessagesToQue(message.getPayload());
        userList.forEach(s -> {
            try {
                s.sendMessage(new TextMessage(sender.getNickname() + ": " + message.getPayload()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private UserModel findBySession(WebSocketSession webSocketSession){
        return userList.stream()
                .filter(s -> s.getUserSession().getId().equals(webSocketSession.getId()))
                .findAny()
                .orElseThrow(IllegalStateException::new);
    }

    public void addMessagesToQue(String message){
        if(lastTenMessages.size() >= 10){
            lastTenMessages.poll();
        }
        lastTenMessages.offer(message);
    }
}

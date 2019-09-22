package edu.udacity.java.nano.chat;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket Server
 *
 * @see ServerEndpoint WebSocket Client
 * @see Session   WebSocket Session
 */

@Component
@ServerEndpoint("/chat/{username}")
public class WebSocketChatServer {

    /**
     * All chat sessions.
     */
    private static Map<String, Session> onlineSessions = new ConcurrentHashMap<>();
    ObjectMapper mapper = new ObjectMapper();

    private static void sendMessageToAll(String msg) {
        //add send message method.
        onlineSessions.forEach((id, session) -> {
            try{
                session.getBasicRemote().sendText(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Open connection, 1) add session, 2) add user.
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("username") String username) {
        //add on open connection.
        onlineSessions.put(session.getId(), session);
        Message message = new Message(MessageType.ENTER, username, username + " ENTERED THE CHAT", onlineSessions.size());
        try {
            String jsonInString = mapper.writeValueAsString(message);
            sendMessageToAll(jsonInString);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Send message, 1) get username and session, 2) send message to all.
     */
    @OnMessage
    public void onMessage(Session session, String jsonStr) {
        // add send message.
        try {
            Message message = mapper.readValue(jsonStr, Message.class);
            message.setType(MessageType.SPEAK);
            message.setOnlineCount(onlineSessions.size());
            String jsonInString = mapper.writeValueAsString(message);
            sendMessageToAll(jsonInString);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Close connection, 1) remove session, 2) update user.
     */
    @OnClose
    public void onClose(Session session, @PathParam("username") String username) {
        // add close connection.
        onlineSessions.remove(session.getId());
        Message message = new Message(MessageType.LEAVE, username, username + " LEFT THE CHAT", onlineSessions.size());
        try {
            String jsonInString = mapper.writeValueAsString(message);
            sendMessageToAll(jsonInString);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Print exception.
     */
    @OnError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
    }

}

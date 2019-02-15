package com.haoke.im.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.haoke.im.dao.MessageDAO;
import com.haoke.im.pojo.Message;
import com.haoke.im.pojo.UserData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * <p></p>
 *
 * @author xiaodongsun
 * @date 2019/02/15
 */
@Component
public class MessageHandler extends TextWebSocketHandler {

    @Autowired
    private MessageDAO messageDAO;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final Map<Long, WebSocketSession> SESSIONS = new HashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long uid = (Long)session.getAttributes().get("uid");
        //将当前用户的session放置到map中，后面会使用相应的session通信
        SESSIONS.put(uid, session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) throws Exception {
        Long uid = (Long) session.getAttributes().get("id");
        JsonNode jsonNode = MAPPER.readTree(textMessage.getPayload());
        long toId = jsonNode.get("toId").asLong();
        String msg = jsonNode.get("msg").asText();

        Message message = Message.builder()
                .from(UserData.USER_MAP.get(uid))
                .to(UserData.USER_MAP.get(toId))
                .msg(msg)
                .build();
        //消息保存到mongoDB
        message = this.messageDAO.saveMessage(message);
        //判断用户是否在线
        WebSocketSession toSession = SESSIONS.get(toId);
        if (toSession != null && toSession.isOpen()){
            //TODO 具体格式需要和前端对接
            toSession.sendMessage(new TextMessage(MAPPER.writeValueAsString(message)));

            //更新消息状态已读
            this.messageDAO.updateMessageState(message.getId(), 2);
        }

    }
}

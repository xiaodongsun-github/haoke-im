package com.haoke.im.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.haoke.im.dao.MessageDAO;
import com.haoke.im.pojo.Message;
import com.haoke.im.pojo.UserData;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * <p></p>
 *
 * @author xiaodongsun
 * @date 2019/02/15
 */
@Component
@RocketMQMessageListener(
        topic = "haoke-im-send-message-topic",
        selectorExpression = "SEND_MSG",
        messageModel = MessageModel.BROADCASTING,
        consumerGroup = "haoke-im-group"
)
public class MessageHandler extends TextWebSocketHandler implements RocketMQListener<String> {

    @Autowired
    private MessageDAO messageDAO;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final Map<Long, WebSocketSession> SESSIONS = new HashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long uid = (Long)session.getAttributes().get("uid");
        //将当前用户的session放置到map中，后面会使用相应的session通信
        SESSIONS.put(uid, session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage
            textMessage) throws Exception {
        Long uid = (Long) session.getAttributes().get("uid");

        JsonNode jsonNode = MAPPER.readTree(textMessage.getPayload());
        Long toId = jsonNode.get("toId").asLong();
        String msg = jsonNode.get("msg").asText();

        Message message = Message.builder()
                .from(UserData.USER_MAP.get(uid))
                .to(UserData.USER_MAP.get(toId))
                .msg(msg)
                .build();

        // 将消息保存到MongoDB
        message = this.messageDAO.saveMessage(message);

        String msgJson = MAPPER.writeValueAsString(message);

        // 判断to用户是否在线
        WebSocketSession toSession = SESSIONS.get(toId);
        if (toSession != null && toSession.isOpen()) {
            //TODO 具体格式需要和前端对接
            toSession.sendMessage(new
                    TextMessage(msgJson));
            // 更新消息状态为已读
            this.messageDAO.updateMessageState(message.getId(), 2);
        }else {
            //该用户可能下线了，也可能在其他的节点中，发送消息到MQ系统
            //添加tag，便于消息筛选
            this.rocketMQTemplate.convertAndSend("haoke-im-send-message-topic:SEND_MSG", msgJson);
        }

    }

    @Override
    public void onMessage(String msg) {
        try {
            JsonNode jsonNode = MAPPER.readTree(msg);
            long toId = jsonNode.get("to").get("id").longValue();
            WebSocketSession toSession = SESSIONS.get(toId);
            if (toSession != null && toSession.isOpen()){
                //TODO 具体格式需要和前端对接
                toSession.sendMessage(new TextMessage(msg));
                // 更新消息状态为已读
                this.messageDAO.updateMessageState(new ObjectId(jsonNode.get("id").asText()), 2);
            }else {

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

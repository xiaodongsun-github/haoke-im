package com.haoke.im.dao.impl;

import com.haoke.im.dao.MessageDAO;
import com.haoke.im.pojo.Message;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * <p></p>
 *
 * @author xiaodongsun
 * @date 2019/02/15
 */
@Component
public class MessageDAOImpl implements MessageDAO {

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 查询点对点消息记录
     *
     * @param fromId
     * @param toId
     * @param page
     * @param rows
     * @return
     */
    @Override
    public List<Message> findListByFromAndTo(Long fromId, Long toId, Integer page, Integer rows) {
        //设置查询条件
        //用户A发送给B用户的条件
        Criteria criteriaFrom = new Criteria().andOperator(
                Criteria.where("from.id").is(fromId),
                Criteria.where("to.id").is(toId)
        );

        Criteria criteriaTo = new Criteria().andOperator(
                Criteria.where("to.id").is(fromId),
                Criteria.where("from.id").is(toId)
        );
        Criteria criteria = new Criteria().orOperator(criteriaFrom, criteriaTo);

        //分页
        PageRequest pageRequest = PageRequest.of(page - 1, rows, Sort.by(Sort.Direction.ASC, "sendDate"));
        Query query = Query.query(criteria).with(pageRequest);

        return this.mongoTemplate.find(query, Message.class);
    }

    @Override
    public Message findMessageById(String id) {
        return this.mongoTemplate.findById(new ObjectId(id), Message.class);
    }

    @Override
    public UpdateResult updateMessageState(ObjectId id, Integer status) {
        Query query = Query.query(Criteria.where("id").is(id));
        Update update = Update.update("status", status);
        if (status.intValue() == 1){
            update.set("send_date", new Date());
        } else if (status.intValue() == 2){
            update.set("read_date", new Date());
        }
        return this.mongoTemplate.updateFirst(query, update, Message.class);
    }

    @Override
    public Message saveMessage(Message message) {
        //发送时间
        message.setSendDate(new Date());
        message.setStatus(1);
        return this.mongoTemplate.save(message);
    }

    @Override
    public DeleteResult deleteMessage(String id) {
        Query query = Query.query(Criteria.where("id").is(id));
        return this.mongoTemplate.remove(query, Message.class);
    }
}

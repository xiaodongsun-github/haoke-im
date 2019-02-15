package com.haoke.im.test;

import com.haoke.im.dao.MessageDAO;
import com.haoke.im.pojo.Message;
import com.haoke.im.pojo.User;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.function.Consumer;

/**
 * <p></p>
 *
 * @author xiaodongsun
 * @date 2019/02/15
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class TestMessageDAO {

    @Autowired
    MessageDAO messageDAO;

    @Test
    public void testSave(){
        Message message = Message.builder()
                .id(ObjectId.get())
                .msg("你好")
                .from(new User(1001L, "zhangsan"))
                .to(new User(1002L, "lisi"))
                .build();
        this.messageDAO.saveMessage(message);

        message = Message.builder()
                .msg("你也好")
                .from(new User(1002L, "lisi"))
                .to(new User(1001L, "zhangsan"))
                .build();
        this.messageDAO.saveMessage(message);

        message = Message.builder()
                .id(ObjectId.get())
                .msg("我在学习mongo db")
                .from(new User(1001L, "zhangsan"))
                .to(new User(1002L, "lisi"))
                .build();
        this.messageDAO.saveMessage(message);

        message = Message.builder()
                .msg("那很好啊")
                .from(new User(1002L, "lisi"))
                .to(new User(1001L, "zhangsan"))
                .build();
        this.messageDAO.saveMessage(message);

        System.out.println("ok!");
    }

    @Test
    public void testQueryList(){
        this.messageDAO.findListByFromAndTo(1001L, 1002L, 2, 1)
                .forEach((Consumer<? super Message>) message -> {
            System.out.println(message);
        });
    }
}

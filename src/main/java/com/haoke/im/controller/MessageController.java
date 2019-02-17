package com.haoke.im.controller;

import com.haoke.im.pojo.Message;
import com.haoke.im.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p></p>
 *
 * @author xiaodongsun
 * @date 2019/2/17
 */
@RestController
@RequestMapping("/message")
@CrossOrigin
public class MessageController {

    @Autowired
    private MessageService messageService;

    @GetMapping
    public List<Message> queryMessageList(@RequestParam("fromId")Long fromId,
                                          @RequestParam("toId")Long toId,
                                          @RequestParam(value = "page", defaultValue = "1")Integer page,
                                          @RequestParam(value = "rows", defaultValue = "10")Integer rows){
        return messageService.queryMessageList(fromId, toId, page, rows);
    }
}

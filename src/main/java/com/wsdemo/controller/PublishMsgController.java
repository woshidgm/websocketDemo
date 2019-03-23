package com.wsdemo.controller;

import com.alibaba.fastjson.JSONObject;
import com.wsdemo.config.websocket.WebSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PublishMsgController {
    @Autowired
    private StringRedisTemplate template;

    @RequestMapping(value = "/publish", method = RequestMethod.POST)
    public void publishMsg(@RequestBody JSONObject jsonParam){
        template.convertAndSend("MSG", jsonParam.toJSONString());
    }

}

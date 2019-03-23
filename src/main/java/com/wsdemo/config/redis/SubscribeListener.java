package com.wsdemo.config.redis;


import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;

import javax.websocket.Session;
import java.io.IOException;


// 这个消息订阅监听者类持有websocket的客户端会话对象（session），
// 当接收到订阅的消息时，通过这个会话对象（session）将消息发送到前端，
// 从而实现消息的主动推送。
public class SubscribeListener implements MessageListener {
    private Session session;

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    // 接受发布者的消息
    @Override
    public void onMessage(Message message, byte[] bytes) {
        String msg = new String(message.getBody());
        if (session!=null&&session.isOpen()){
            try{
                session.getBasicRemote().sendText(msg);
            }catch (IOException e){
                e.printStackTrace();
            }
        }

    }
}

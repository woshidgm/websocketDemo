package com.wsdemo.config.websocket;

import com.wsdemo.config.redis.SubscribeListener;
import com.wsdemo.util.SpringUtils;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@ServerEndpoint("/websocket/server")
public class WebSocketServer  {
    /**
     * 因为@ServerEndpoint不支持注入，所以使用SpringUtils获取IOC实例
     */

    private RedisMessageListenerContainer redisMessageListenerContainer = SpringUtils.getBean(RedisMessageListenerContainer.class);

    //静态变量，用来记录当前在线连接数。应该把它设计成线程安全的(多线程操作)。
    private static AtomicInteger onlineCount=new AtomicInteger(0);

    // concurrent包的线程安全Set，用来存放每个客户端对应的webSocket对象。
    // 若要实现服务端与单一客户端通信的话，可以使用Map来存放，其中Key可以为用户标识
    private static CopyOnWriteArraySet<WebSocketServer> webSocketSet = new CopyOnWriteArraySet<WebSocketServer>();

    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;

    private SubscribeListener subscribeListener; //消息订阅监听者类

    @OnOpen
    public void onOpen(Session session){
        this.session = session;
        System.out.println(session.toString());
        webSocketSet.add(this);
        addOnlineCount(); //在线加1
        System.out.println("有新连接加入！当前在线人数为" + getOnlineCount());
        subscribeListener = new SubscribeListener();
        subscribeListener.setSession(session);

        // 设置订阅topic
        redisMessageListenerContainer.addMessageListener(subscribeListener, new ChannelTopic("MSG"));
    }

    /**
     * 连接关闭的方法
     * @throws IOException
     */
    @OnClose
    public void onClose() throws IOException{
        webSocketSet.remove(this);//从set中删除
        subOnlineCount();           //在线数减1
        redisMessageListenerContainer.removeMessageListener(subscribeListener);
        System.out.println("有一连接关闭！当前在线人数为" + getOnlineCount());
    }

    /**
     * 收到客户端发来的消息 调用的方法
     * 实际中没有使用到这个逻辑
     * @param message
     * @param session
     */
    @OnMessage
    public void onMessage(String message, Session session){
        System.out.println("来自客户端的消息:" + message);
        for (WebSocketServer wss: webSocketSet){
            try{
                wss.sendMessage(message);
            }catch (IOException e){
                e.printStackTrace();
                continue;
            }
        }
    }

    @OnError
    public void onError(Session session, Throwable error){
        System.out.println("发生错误");
        error.printStackTrace();
    }


    /**
     * 这个方法与上面几个方法不一样。没有用注解，是根据自己需要添加的方法。
     * @param message
     * @throws IOException
     */

    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }


    public int getOnlineCount() {
        return onlineCount.get();
    }

    public void addOnlineCount() {
        WebSocketServer.onlineCount.getAndIncrement();
    }

    public void subOnlineCount() {
        WebSocketServer.onlineCount.getAndDecrement();
    }
}

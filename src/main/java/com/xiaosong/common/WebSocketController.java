package com.xiaosong.common;

import io.undertow.servlet.websockets.WebSocketServlet;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;

@ServerEndpoint("/websocket")
public class WebSocketController {
    MyThread thread1=new MyThread();
    Thread thread=new Thread(thread1);
    //用来存放每个客户端对应的MyWebSocket对象。
    private static CopyOnWriteArraySet<WebSocketController> webSocketSet = new CopyOnWriteArraySet<WebSocketController>();
    private  javax.websocket.Session session=null;
    //开启连接
    @OnOpen
    public void onOpen(Session session) throws IOException{
        this.session=session;
        webSocketSet.add(this);
        System.out.println(webSocketSet);
        //开启一个线程对数据库中的数据进行轮询
        thread.start();
    }
    //关闭连接
    @OnClose
    public void onClose(){
        thread1.stopMe();
        webSocketSet.remove(this);
    }
    //给服务器发送消息告知数据库发生变化
    @OnMessage
    public void onMessage(int count) {
        System.out.println("发生变化"+count);
        try {
            sendMessage();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    //出错的操作
    @OnError
    public void onError(Throwable error){
        System.out.println(error);
        error.printStackTrace();
    }

    /**
     * 这个方法与上面几个方法不一样。没有用注解，是根据自己需要添加的方法。
     * @throws IOException
     * 发送自定义信号，“1”表示告诉前台，数据发生改变了，需要刷新
     */
    public void sendMessage() throws IOException{
        //群发消息
        for(WebSocketController item: webSocketSet){
            item.session.getBasicRemote().sendText("1");
        }
    }
}

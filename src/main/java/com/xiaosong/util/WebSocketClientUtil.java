//package com.xiaosong.util;
//
//import org.apache.log4j.Logger;
//import org.java_websocket.client.WebSocketClient;
//import org.java_websocket.handshake.ServerHandshake;
//
//import java.io.IOException;
//import java.net.URI;
//import javax.websocket.OnClose;
//import javax.websocket.OnError;
//import javax.websocket.OnMessage;
//import javax.websocket.OnOpen;
//import javax.websocket.Session;
//import javax.websocket.server.ServerEndpoint;
//
//@ServerEndpoint("/websocket.ws")
//public class WebSocketClientUtil {
//
//    private static Logger logger = Logger.getLogger(WebSocketClientUtil.class);
//
//    @OnOpen
//    public void onOpen(Session session) {
//        System.out.println("连接打开了OPEN");
//    }
//
//    /**
//     * 收到客户端消息时触发
//     */
//    @OnMessage
//    public void onMessage(Session session, String key) throws IOException {
//        //向客户端返回发送过来的消息
//        logger.info("发送一条消息：--"+key);
//        session.getBasicRemote().sendText(key);//推送发送的消息
//    }
//
//    /**
//     * 异常时触发
//     */
//    @OnError
//    public void onError(Throwable throwable,Session session) {
//        logger.error("连接失败le ~~~~(>_<)~~~~");
//    }
//
//    /**
//     * 关闭连接时触发
//     */
//    @OnClose
//    public void onClose(Session session) {
//        logger.info("连接关闭了~~~~(>_<)~~~~");
//    }
//
//}
//

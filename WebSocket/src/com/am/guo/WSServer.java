package com.am.guo;
/** 
* @author AMGuo E-mail:www.guoao@foxmail.com 
* @version 创建时间：2017年8月29日 上午9:49:28 
* 类说明 
*/

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/ws/{user}", configurator = GetHttpRequestConfigurator.class)
public class WSServer {
    private String currentUser;

    private static Map<String, Session> userInfo = new LinkedHashMap<>();;

    private static int onlineCount = 0;

    public WSServer() {

    }

    // 连接打开时执行
    @OnOpen
    public void onOpen(@PathParam("user") String user, Session session, EndpointConfig config) throws UnsupportedEncodingException {
        user = URLDecoder.decode(user, "UTF-8");
        if (!userInfo.containsKey(user)) {
            synchronized (this) {
                if (!userInfo.containsKey(user)) {
                    currentUser = user;
                    // 获取客户端信息
                    HandshakeRequest request = (HandshakeRequest) config.getUserProperties().get(HandshakeRequest.class.getName());
                    Map<String, List<String>> headers = request.getHeaders();

                    userInfo.put(user, session);
                    addOnlineCount(); // 在线数加1
                    System.out.println("Connected ... " + session.getId());
                }
            }
        }
        System.out.println(onlineCount);
        System.out.println(userInfo.size());
    }

    // 收到消息时执行
    @OnMessage
    public void onMessage(String message, Session session) {
        System.out.println("来自客户端的消息：" + currentUser + "：" + message);
        for (Entry<String, Session> entry : userInfo.entrySet()) {
            sendMessage(entry.getValue(), currentUser + ":" + message);
        }
    }

    private void sendMessage(Session session, String message) {
        try {
            session.getAsyncRemote().sendText(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 连接关闭时执行
    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        System.out.println(String.format("Session %s closed because of %s", session.getId(), closeReason));
    }

    // 连接错误时执行
    @OnError
    public void onError(Throwable t) {
        t.printStackTrace();
    }

    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static void addOnlineCount() {
        WSServer.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        WSServer.onlineCount--;
    }
}

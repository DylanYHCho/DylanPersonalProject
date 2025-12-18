package com.jstargram.server.main;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import com.jstargram.server.network.ClientHandler;

public class ServerMain {
    private static final int PORT = 9999;

    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket();
            serverSocket.setReuseAddress(true);
            
            // [중요] "0.0.0.0"은 "모든 IP에서의 접속을 허용한다"는 뜻입니다.
            serverSocket.bind(new InetSocketAddress("0.0.0.0", PORT)); 
            
            System.out.println("[서버 시작] 모든 IP에서 9999번 포트 접속 대기 중...");

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("[접속 알림] " + socket.getInetAddress() + " 님이 입장하셨습니다.");
                ClientHandler handler = new ClientHandler(socket);
                handler.start(); 
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
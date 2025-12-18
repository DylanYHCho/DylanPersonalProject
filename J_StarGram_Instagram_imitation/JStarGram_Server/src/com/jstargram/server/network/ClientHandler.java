package com.jstargram.server.network;

import java.io.*;
import java.net.Socket;
import java.util.Vector;
import com.jstargram.common.dto.UserDTO;
import com.jstargram.common.dto.ChatMessage; // [추가] 채팅 DTO
import com.jstargram.common.dto.PresenceInfo; // [추가] 상태 DTO
import com.jstargram.server.dao.UserDAO;

public class ClientHandler extends Thread {
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private UserDAO userDAO;
    
    private static int postIdCounter = 1000;
    public static Vector<ClientHandler> allClients = new Vector<>();

    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.userDAO = new UserDAO();
        allClients.add(this);
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            while (true) {
                Object obj = in.readObject();
                
                // 1. 로그인/회원가입 (UserDTO)
                if (obj instanceof UserDTO) {
                    UserDTO dto = (UserDTO) obj;
                    if (dto.getCommand() == UserDTO.JOIN) handleJoin(dto);
                    else if (dto.getCommand() == UserDTO.LOGIN) handleLogin(dto);
                } 
                // 2. 피드/좋아요/삭제 (String)
                else if (obj instanceof String) {
                	String msg = (String) obj;
                	System.out.println("[방송 요청] " + msg);
                	handleRealtimeMessage(msg);
                }
                // 3. [추가] 채팅 메시지 (ChatMessage) -> 모두에게 방송
                else if (obj instanceof ChatMessage) {
                    ChatMessage chatMsg = (ChatMessage) obj;
                    System.out.println("[채팅] " + chatMsg.getSenderName() + ": " + chatMsg.getContent());
                    broadcast(chatMsg); // 객체 통째로 방송
                }
                // 4. [추가] 접속자 상태/위치 (PresenceInfo) -> 모두에게 방송
                else if (obj instanceof PresenceInfo) {
                    PresenceInfo info = (PresenceInfo) obj;
                    broadcast(info); // 객체 통째로 방송
                }
            }
        } catch (Exception e) {
            System.out.println("[접속 종료] " + socket.getInetAddress());
            allClients.remove(this);
        }
    }
    
    // [수정] String뿐만 아니라 모든 Object를 방송할 수 있도록 변경
    public void broadcast(Object msg) {
    	for(ClientHandler client : allClients) {
    		try {
				client.out.writeObject(msg);
				client.out.flush();
			} catch (IOException e) {
				System.err.println("전송 실패");
			}
    	}
    }
    
    private void handleRealtimeMessage(String msg) {
        if (msg.startsWith("POST|")) {
            String[] parts = msg.split("\\|", 5); 
            if (parts.length >= 5) { 
                int newPostId = ++postIdCounter;
                String broadcastMsg = "POST|" + newPostId + "|" + parts[1] + "|" + parts[2] + "|" + parts[3] + "|" + parts[4];
                System.out.println("[새 글 방송] ID " + newPostId);
                broadcast(broadcastMsg);
            }
        } 
        else if (msg.startsWith("DELETE_POST|") || msg.startsWith("LIKE|") || msg.startsWith("COMMENT|")) {
            broadcast(msg);
        }
    }

    private void handleJoin(UserDTO dto) {
        boolean success = userDAO.join(dto);
        try {
            dto.setName(success ? "SUCCESS" : "FAIL");
            out.writeObject(dto);
            out.flush();
        } catch (IOException e) {}
    }

    private void handleLogin(UserDTO dto) {
        UserDTO loggedInUser = userDAO.login(dto.getId(), dto.getPw());
        try {
            if (loggedInUser != null) {
                out.writeObject(loggedInUser); 
            } else {
                dto.setName("FAIL");
                out.writeObject(dto);
            }
            out.flush();
        } catch (IOException e) {}
    }
}
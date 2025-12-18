package com.jstargram.client.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import com.jstargram.common.dto.UserDTO;
import com.jstargram.common.dto.ChatMessage;
import com.jstargram.common.dto.PresenceInfo;
import com.jstargram.client.view.PhotoBoardUI;
import com.jstargram.client.main.ClientMain; 

public class ClientService {

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private PhotoBoardUI mainUI; 
    private ChatListener chatListenerDelegate;
    
    private static final String SERVER_IP = "127.0.0.1"; // 실제 IP로 수정 필요
    private static final int PORT = 9999;

    public boolean connect() {
        try {
            socket = new Socket(SERVER_IP, PORT);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            System.out.println("[클라이언트] 서버 연결 성공");
            return true;
        } catch (Exception e) {
            System.out.println("[클라이언트] 서버 연결 실패");
            return false;
        }
    }
    
    public void startListening(PhotoBoardUI ui) {
    	this.mainUI = ui;
        if (ui instanceof ChatListener) {
            this.chatListenerDelegate = (ChatListener) ui;
        }
    	
    	new Thread(() -> {
    		try {
				while(true) {
					Object obj = in.readObject();
					
					if (obj instanceof String) {
						String msg = (String) obj;
						System.out.println("[수신] 방송: " + msg);
						if(mainUI != null) mainUI.handleBroadcast(msg);
					} 
                    else if (obj instanceof ChatMessage) {
                        ChatMessage msg = (ChatMessage) obj;
                        if (chatListenerDelegate != null) chatListenerDelegate.onMessageReceived(msg);
                    }
                    else if (obj instanceof PresenceInfo) {
                        PresenceInfo info = (PresenceInfo) obj;
                        if (chatListenerDelegate != null) chatListenerDelegate.onPresenceUpdated(info);
                    }
				}
			} catch (Exception e) {
				System.out.println("[클라이언트] 서버 연결 끊김");
			}
    	}).start();
    }

    public boolean join(String id, String pw, String name, String phone) {
        try {
            UserDTO dto = new UserDTO(UserDTO.JOIN, id, pw, name, phone);
            out.writeObject(dto);
            out.flush();
            UserDTO response = (UserDTO) in.readObject();
            return "SUCCESS".equals(response.getName());
        } catch (Exception e) { return false; }
    }

    public String login(String id, String pw) {
        try {
            UserDTO dto = new UserDTO();
            dto.setCommand(UserDTO.LOGIN);
            dto.setId(id);
            dto.setPw(pw);
            out.writeObject(dto);
            out.flush();
            
            UserDTO response = (UserDTO) in.readObject();
            
            if (response.getName() != null && !response.getName().equals("FAIL")) {
            	ClientMain.currentUserId = response.getId();
            	ClientMain.currentUserNickname = response.getName();
            	
            	// [핵심] 로그인 성공 시 나의 상태(Presence)를 서버로 전송!
            	sendMyPresence();
            	
            	return response.getName(); 
            }
            return null; 
        } catch (Exception e) { return null; }
    }

    // [수정] 내 접속 상태 및 위치 전송
    private void sendMyPresence() {
        PresenceInfo myInfo = new PresenceInfo(ClientMain.currentUserId, ClientMain.currentUserNickname);
        myInfo.setOnline(true);
        myInfo.setLastSeenText("방금 전");
        
        // 위치 정보 테스트 (임시 고정값)
        myInfo.setLocationText("서울 강남구");
        myInfo.setLatitude(37.498095);
        myInfo.setLongitude(127.027610);
        
        try {
            if (out != null) {
                out.writeObject(myInfo);
                out.flush();
            }
        } catch (IOException e) { e.printStackTrace(); }
    }
    
    // [추가] 방 생성 요청
    public void sendCreateRoom(String roomName) {
        try {
            out.writeObject("CREATE_ROOM|" + roomName);
            out.flush();
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void sendLike(int postId) {
    	try { out.writeObject("LIKE|" + postId + "|" + ClientMain.currentUserId); out.flush(); } catch(Exception e){}
    }
    
    public void sendComment(int postId, String content) {
    	try { out.writeObject("COMMENT|" + postId + "|" + ClientMain.currentUserNickname + "|" + content); out.flush(); } catch(Exception e){}
    }
    
    public void sendNewPost(String writerId, String writerNickname, String content, String imagePath) {
        try {
            String msg = "POST|" + writerId + "|" + writerNickname + "|" + content + "|" + imagePath;
            out.writeObject(msg);
            out.flush();
        } catch (Exception e) {}
    }
    
    public void sendDeleteRequest(int postId) {
        try { out.writeObject("DELETE_POST|" + postId); out.flush(); } catch(Exception e){}
    }
    
    public void joinChatRoom(String roomId) {
        try { out.writeObject("CHAT_JOIN|" + roomId + "|" + ClientMain.currentUserId); out.flush(); } catch(Exception e){}
    }
    
    public void leaveChatRoom(String roomId) {
        try { out.writeObject("CHAT_LEAVE|" + roomId + "|" + ClientMain.currentUserId); out.flush(); } catch(Exception e){}
    }
    
    public void sendChatMessage(String roomId, String content) {
        try {
            ChatMessage message = new ChatMessage(
                    roomId, 
                    ClientMain.currentUserId, 
                    ClientMain.currentUserNickname,
                    content, 
                    System.currentTimeMillis()
            );
            out.writeObject(message);
            out.flush();
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void close() {
        try { if(in != null) in.close(); } catch(Exception e) {}
        try { if(out != null) out.close(); } catch(Exception e) {}
        try { if(socket != null) socket.close(); } catch(Exception e) {}
    }
}
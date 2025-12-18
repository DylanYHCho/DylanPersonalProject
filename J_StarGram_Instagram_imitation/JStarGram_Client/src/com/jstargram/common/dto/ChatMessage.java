package com.jstargram.common.dto;

import java.io.Serializable;

// 채팅 정보
public class ChatMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private String roomId;      // 어느 방(그룹채팅 방 ID)
    private String senderId;    // 누가 보냈는지 (사용자 ID)
    private String senderName;  // 보여줄 이름 (닉네임)
    private String content;     // 메시지 내용
    private long timestamp;     // 보낸 시간 (epoch milli 등)

    public ChatMessage() {}

    public ChatMessage(String roomId, String senderId, String senderName,
                       String content, long timestamp) {
        this.roomId = roomId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.content = content;
        this.timestamp = timestamp;
    }

    public String getRoomId() { return roomId; }
    public String getSenderId() { return senderId; }
    public String getSenderName() { return senderName; }
    public String getContent() { return content; }
    public long getTimestamp() { return timestamp; }
    
    // Setter (필요 시)
    public void setRoomId(String roomId) { this.roomId = roomId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public void setContent(String content) { this.content = content; }
}
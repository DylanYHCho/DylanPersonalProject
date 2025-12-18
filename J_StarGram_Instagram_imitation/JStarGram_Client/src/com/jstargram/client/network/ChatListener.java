package com.jstargram.client.network;

import com.jstargram.common.dto.ChatMessage;
import com.jstargram.common.dto.PresenceInfo;

// 서버에서 메시지 오면 UI에 알려주는 인터페이스
public interface ChatListener {

    // 일반 채팅 메시지
    void onMessageReceived(ChatMessage message);   

    // 시스템 알림 (누가 들어왔다/나갔다, 오류 메시지 등)
    default void onSystemMessage(String roomId, String text) { }

    // 새로운 presence 콜백 (접속 상태 및 위치 정보 갱신)
    void onPresenceUpdated(PresenceInfo info);

    // 연결 끊겼을 때
    default void onDisconnected(Exception e) { }
}
package com.jstargram.common.dto;

import java.io.Serializable;

// 유저 상태&위치 정보 데이터 표현용 클래스
public class PresenceInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String userId;
    private final String nickname;

    private boolean online;
    private String lastSeenText;   // "5분 전", "2시간 전" 등
    private String locationText;   // "서울시 강남구 ..." 등

    // ★ 지도용 좌표
    private Double latitude;       // 위도
    private Double longitude;      // 경도

    public PresenceInfo(String userId, String nickname) {
        this.userId = userId;
        this.nickname = nickname;
    }

    public String getUserId() { return userId; }
    public String getNickname() { return nickname; }
    public boolean isOnline() { return online; }
    public String getLastSeenText() { return lastSeenText; }
    public String getLocationText() { return locationText; }

    public void setOnline(boolean online) { this.online = online; }
    public void setLastSeenText(String lastSeenText) { this.lastSeenText = lastSeenText; }
    public void setLocationText(String locationText) { this.locationText = locationText; }

    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
}
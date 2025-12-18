package com.jstargram.common.dto;

import java.io.Serializable;

// [중요] 이 파일은 Server와 Client 양쪽 프로젝트에 모두 있어야 합니다!
// com.jstargram.common.dto 패키지에 넣어주세요.
public class UserDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	// 프로토콜(명령어) 정의
	public static final int JOIN = 1;   // 회원가입
	public static final int LOGIN = 2;  // 로그인
	public static final int UPDATE = 3; // 정보수정
	public static final int DELETE = 4; // 회원탈퇴
	
	private int command; // 명령어
	private String id;
	private String pw;
	private String name;
	private String phone;
	
	// 기본 생성자
	public UserDTO() {}

	// 전체 생성자
	public UserDTO(int command, String id, String pw, String name, String phone) {
		this.command = command;
		this.id = id;
		this.pw = pw;
		this.name = name;
		this.phone = phone;
	}

	// [이 Getter/Setter 메서드들이 있어야 UserDAO에서 에러가 안 납니다!]
	public int getCommand() { return command; }
	public void setCommand(int command) { this.command = command; }
	
	public String getId() { return id; }
	public void setId(String id) { this.id = id; }
	
	public String getPw() { return pw; }
	public void setPw(String pw) { this.pw = pw; }
	
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	
	public String getPhone() { return phone; }
	public void setPhone(String phone) { this.phone = phone; }
}
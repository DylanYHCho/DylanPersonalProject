package com.jstargram.server.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import com.jstargram.common.dto.UserDTO;

public class UserDAO {

	// 1. 회원가입 기능
	public boolean join(UserDTO user) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		boolean isSuccess = false;
		
		try {
			conn = DBConnection.getConnection(); 
			
			// [중요] 자동 저장을 끄고 수동으로 제어함 (확실하게 하기 위해)
			conn.setAutoCommit(false);
			
			String sql = "INSERT INTO user_tb (id, password, name, phone, status_msg) VALUES (?, ?, ?, ?, ?)";
			
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, user.getId());
			pstmt.setString(2, user.getPw());
			pstmt.setString(3, user.getName());
			pstmt.setString(4, user.getPhone());
			pstmt.setString(5, "안녕하세요!"); 
			
			int result = pstmt.executeUpdate(); 
			
			if(result > 0) {
				conn.commit(); // "저장해!!" 하고 도장 쾅 찍기
				isSuccess = true;
				System.out.println("[DB] 회원가입 데이터 저장 완료(Commit): " + user.getId());
			} else {
				conn.rollback(); // 실패하면 되돌리기
				System.out.println("[DB] 저장 실패로 롤백됨.");
			}
			
		} catch (Exception e) {
			System.out.println("[DB 에러] 회원가입 실패 (" + e.getMessage() + ")");
			try { if(conn != null) conn.rollback(); } catch(Exception ex) {} // 에러나면 취소
		} finally {
			try { if(pstmt != null) pstmt.close(); } catch(Exception e) {}
			try { if(conn != null) conn.close(); } catch(Exception e) {}
		}
		
		return isSuccess;
	}
	
	// 2. 로그인 기능
    // [수정] boolean 대신 UserDTO 객체를 반환하여, 닉네임 정보를 클라이언트에 전달합니다.
	public UserDTO login(String id, String pw) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		UserDTO loggedInUser = null; // 성공 시 여기에 DTO를 담아 반환

		try {
			System.out.println("[DB] 로그인 조회 시작 -> ID: " + id);
			conn = DBConnection.getConnection();
			
			String sql = "SELECT * FROM user_tb WHERE id = ? AND password = ?";
			
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, id);
			pstmt.setString(2, pw);
			
			rs = pstmt.executeQuery(); 
			
			if (rs.next()) {
                // 로그인 성공! 조회된 정보를 DTO에 담습니다.
                loggedInUser = new UserDTO();
                loggedInUser.setId(rs.getString("id"));
                loggedInUser.setPw(rs.getString("password")); // 보안상 PW는 안 보내는 것이 좋으나 통일성을 위해 포함
                loggedInUser.setName(rs.getString("name"));     // [핵심] 닉네임(name)을 담음
                loggedInUser.setPhone(rs.getString("phone"));
                
				System.out.println("[DB] 로그인 성공! 사용자명: " + loggedInUser.getName());
			} else {
				System.out.println("[DB] 로그인 실패: 일치하는 계정 없음 (ID: " + id + ")");
			}
			
		} catch (Exception e) {
			System.out.println("[DB 에러] 로그인 처리 중 오류: " + e.getMessage());
			e.printStackTrace();
		} finally {
			try { if(rs != null) rs.close(); } catch(Exception e) {}
			try { if(pstmt != null) pstmt.close(); } catch(Exception e) {}
			try { if(conn != null) conn.close(); } catch(Exception e) {}
		}
		
		return loggedInUser; // 성공 시 DTO, 실패 시 null 반환
	}
}
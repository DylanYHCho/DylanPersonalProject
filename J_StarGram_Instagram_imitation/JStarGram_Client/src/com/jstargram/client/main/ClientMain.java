package com.jstargram.client.main;

import javax.swing.JOptionPane;
import com.jstargram.client.network.ClientService;
import com.jstargram.client.view.LoginUI;

public class ClientMain {
	
	public static ClientService clientService;
    
    // [중요] 현재 로그인한 사용자 정보를 저장하는 필드
    // 이 정보가 있어야 게시자(Me -> 닉네임) 및 좋아요 1개 제한(ID) 기능이 동작합니다.
    public static String currentUserId;
    public static String currentUserNickname;

	public static void main(String[] args) {
		System.out.println("====== 클라이언트 프로그램 시작 ======");
		
		clientService = new ClientService();
		
		if (clientService.connect()) {
			System.out.println("[성공] 서버에 연결되었습니다. 로그인 창을 엽니다.");
			
			javax.swing.SwingUtilities.invokeLater(() -> {
				new LoginUI(); 
			});
			
		} else {
			System.out.println("[실패] 서버 연결 실패.");
			JOptionPane.showMessageDialog(null, 
					"서버에 연결할 수 없습니다.\n서버가 켜져 있는지 확인해주세요.", 
					"연결 오류", 
					JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}
}
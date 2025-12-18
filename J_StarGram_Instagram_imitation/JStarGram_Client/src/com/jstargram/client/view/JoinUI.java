package com.jstargram.client.view;

import javax.swing.*;
import java.awt.*;
import com.jstargram.client.main.ClientMain;

// 회원가입 팝업창 (JDialog: 부모창 위에 뜨는 창)
public class JoinUI extends JDialog {

	private JTextField txtId, txtName, txtPhone;
	private JPasswordField txtPw;
	private JButton btnSubmit, btnCancel;

	public JoinUI(JFrame parent) {
		super(parent, "회원가입", true); // true = 이 창이 켜진 동안 뒤에 로그인창 클릭 불가
		setSize(300, 400);
		setLayout(new GridLayout(6, 2, 10, 10)); // 6줄 2칸 격자 배치
		
		// 1. 화면 구성 (라벨 + 입력창)
		add(new JLabel("  아이디:"));
		txtId = new JTextField();
		add(txtId);

		add(new JLabel("  비밀번호:"));
		txtPw = new JPasswordField();
		add(txtPw);

		add(new JLabel("  닉네임:"));
		txtName = new JTextField();
		add(txtName);

		add(new JLabel("  전화번호:"));
		txtPhone = new JTextField();
		add(txtPhone);

		// 빈 공간 채우기용 (레이아웃 맞춤)
		add(new JLabel("")); 
		add(new JLabel(""));

		// 2. 버튼 생성
		btnSubmit = new JButton("가입완료");
		btnSubmit.setBackground(new Color(100, 200, 100)); // 연두색
		btnCancel = new JButton("취소");
		
		add(btnSubmit);
		add(btnCancel);
		
		// 3. 기능 연결
		// [가입완료 버튼]
		btnSubmit.addActionListener(e -> {
			// 입력값 가져오기 (공백 제거)
			String id = txtId.getText().trim();
			String pw = new String(txtPw.getPassword()).trim();
			String name = txtName.getText().trim();
			String phone = txtPhone.getText().trim();
			
			// 빈칸 검사
			if(id.isEmpty() || pw.isEmpty() || name.isEmpty()) {
				JOptionPane.showMessageDialog(this, "아이디, 비밀번호, 닉네임은 필수입니다.");
				return;
			}
			
			// [디버깅] 전송 로그 출력
			System.out.println("[클라이언트] 회원가입 요청 -> ID: " + id + ", Name: " + name);
			
			// ★ 서버로 회원가입 요청 보내기 (ClientService 사용)
			boolean isSuccess = ClientMain.clientService.join(id, pw, name, phone);
			
			if (isSuccess) {
				JOptionPane.showMessageDialog(this, "회원가입 성공! 로그인해주세요.");
				dispose(); // 창 닫기
			} else {
				// 서버가 꺼져있거나 에러가 나도 false가 반환되므로 메시지를 범용적으로 수정
				JOptionPane.showMessageDialog(this, "회원가입 실패\n(ID 중복 또는 서버 오류)");
			}
		});
		
		// [취소 버튼]
		btnCancel.addActionListener(e -> dispose());

		// 화면 중앙 배치
		setLocationRelativeTo(parent);
		setVisible(true);
	}
}
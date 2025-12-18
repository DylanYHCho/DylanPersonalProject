package com.jstargram.client.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import com.jstargram.client.main.ClientMain;

public class LoginUI extends JFrame {

	private JTextField txtId;
	private JPasswordField txtPw;
	private JButton btnLogin, btnJoin;

	public LoginUI() {
		setTitle("J-StarGram 로그인");
		setSize(350, 450);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(null); 
		getContentPane().setBackground(Color.WHITE);

		// 1. 로고
		JLabel lblTitle = new JLabel("J-StarGram");
		lblTitle.setFont(new Font("Verdana", Font.BOLD, 28));
		lblTitle.setBounds(70, 50, 200, 40);
		lblTitle.setHorizontalAlignment(JLabel.CENTER);
		add(lblTitle);

		// 2. 입력 필드
		JLabel lblId = new JLabel("ID");
		lblId.setBounds(40, 120, 50, 30);
		add(lblId);

		txtId = new JTextField();
		txtId.setBounds(90, 120, 200, 30);
		add(txtId);

		JLabel lblPw = new JLabel("PW");
		lblPw.setBounds(40, 160, 50, 30);
		add(lblPw);

		txtPw = new JPasswordField();
		txtPw.setBounds(90, 160, 200, 30);
		add(txtPw);

		// 3. 버튼들
		btnLogin = new JButton("로그인");
		
		// 맥북 호환성 패치: 배경색이 보이도록 불투명 설정
		btnLogin.setOpaque(true); 
		btnLogin.setBorderPainted(false);
		
		btnLogin.setBackground(new Color(50, 100, 255)); 
		btnLogin.setForeground(Color.WHITE);
		btnLogin.setFont(new Font("맑은 고딕", Font.BOLD, 14)); 
		btnLogin.setBounds(40, 210, 250, 40);
		add(btnLogin);

		btnJoin = new JButton("회원가입");
		
		// 가입 버튼 패치
		btnJoin.setOpaque(true); 
		btnJoin.setBorderPainted(false);
		
		btnJoin.setBackground(Color.LIGHT_GRAY);
		btnJoin.setBounds(40, 260, 250, 40);
		add(btnJoin);

		// 4. 이벤트 연결
		btnLogin.addActionListener(e -> {
			String id = txtId.getText().trim(); 
			String pw = new String(txtPw.getPassword()).trim(); 

			if (id.isEmpty() || pw.isEmpty()) {
				JOptionPane.showMessageDialog(this, "아이디와 비밀번호를 입력하세요.");
				return;
			}
			
			System.out.println("[클라이언트] 로그인 시도 -> ID: " + id);
			
			// [수정] String 타입으로 닉네임을 받습니다. (오류 해결)
			String nickname = ClientMain.clientService.login(id, pw); 

			// [수정] 닉네임이 null이 아니면(즉, String 값이 있으면) 성공 처리
			if (nickname != null) {
				JOptionPane.showMessageDialog(this, nickname + "님 환영합니다!");
				dispose(); 
				new PhotoBoardUI(); 
			} else {
				JOptionPane.showMessageDialog(this, "로그인 실패!\n아이디나 비밀번호를 확인해주세요.");
			}
		});

		btnJoin.addActionListener(e -> {
			new JoinUI(this);
		});

		setLocationRelativeTo(null); 
		setVisible(true);
	}
}
package com.jstargram.client.view;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import com.jstargram.client.main.ClientMain;

// 글쓰기 버튼을 누르면 뜨는 팝업창
public class WritePostUI extends JDialog {

    private PhotoBoardUI mainUI; 
    private String selectedFilePath = ""; // 사용자가 고른 사진 파일 경로
    private JLabel previewLabel; // 사진 미리보기

    public WritePostUI(PhotoBoardUI parent) {
        super(parent, "새 게시글 작성", true); // 모달 창
        this.mainUI = parent;

        setSize(300, 400);
        setLayout(new FlowLayout());

        add(new JLabel("사진을 선택하고 글을 쓰세요."));

        // 사진 미리보기 영역
        previewLabel = new JLabel("사진 없음");
        previewLabel.setPreferredSize(new Dimension(250, 200));
        previewLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        previewLabel.setHorizontalAlignment(JLabel.CENTER);
        add(previewLabel);

        // [사진 찾기] 버튼
        JButton findImgBtn = new JButton("사진 파일 찾기...");
        findImgBtn.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(this);
            
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                selectedFilePath = selectedFile.getAbsolutePath(); // 파일 경로 저장
                
                // 미리보기
                ImageIcon icon = new ImageIcon(selectedFilePath);
                Image img = icon.getImage().getScaledInstance(250, 200, Image.SCALE_SMOOTH);
                previewLabel.setIcon(new ImageIcon(img));
                previewLabel.setText(""); 
            }
        });
        add(findImgBtn);

        // 내용 입력창
        JTextField contentField = new JTextField(20); 
        add(new JLabel("내용: "));
        add(contentField);

        // [업로드] 버튼
        JButton uploadBtn = new JButton("게시글 올리기");
        uploadBtn.setBackground(Color.ORANGE);
        uploadBtn.addActionListener(e -> {
            String content = contentField.getText().trim();
            
            if (content.isEmpty()) {
                JOptionPane.showMessageDialog(this, "내용을 입력해주세요!");
                return;
            }
            
            String writerNickname = ClientMain.currentUserNickname;
            String writerId = ClientMain.currentUserId; // ID 추가 (ClientHandler에서 필요)
            
            if(writerNickname == null || writerId == null) {
                JOptionPane.showMessageDialog(this, "로그인 정보가 없습니다.");
                return;
            }
            
            // [핵심 수정] 이미지 경로가 비어있으면 "EMPTY"를 넣어 포맷을 강제합니다.
            String imagePathToSend = selectedFilePath.isEmpty() ? "EMPTY" : selectedFilePath;

            // 서버로 전송 요청! (POST|작성자ID|닉네임|내용|이미지경로)
            System.out.println("[클라이언트] 게시글 업로드 요청: " + writerNickname);
            
            ClientMain.clientService.sendNewPost(writerId, writerNickname, content, imagePathToSend);
            
            dispose(); // 창 닫기
        });
        add(uploadBtn);

        setLocationRelativeTo(parent); 
        setVisible(true);
    }
}
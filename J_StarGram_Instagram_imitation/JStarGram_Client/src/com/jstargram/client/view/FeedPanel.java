package com.jstargram.client.view;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList; 
import com.jstargram.client.main.ClientMain;

public class FeedPanel extends JPanel {

	private PhotoBoardUI parentUI; 
    private int postId;
    private String postWriterId; 
    private JLabel likeCountLabel;
    private JButton likeButton;
    private int currentLikes;
    
    private ArrayList<String> likeUsers = new ArrayList<>(); 
    private ArrayList<String> commentList = new ArrayList<>();
    
    private JTextArea activeCommentArea = null;

    // 생성자
    public FeedPanel(PhotoBoardUI parent, int id, String writerId, String writerNickname, String content, String imagePath) {
    	this.parentUI = parent; 
        this.postId = id;
        this.postWriterId = writerId; 
        this.currentLikes = 0;

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        setPreferredSize(new Dimension(350, 400)); 
        setBackground(Color.WHITE);

        // --- 1. 상단 (작성자 및 삭제 버튼) ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        
        JLabel writerLabel = new JLabel(" 👤 " + writerNickname);
        writerLabel.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        writerLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JButton deleteBtn = new JButton("X");
        deleteBtn.setForeground(Color.RED);
        deleteBtn.setBorderPainted(false);
        deleteBtn.setContentAreaFilled(false); 
        deleteBtn.setFocusPainted(false);
        deleteBtn.setFont(new Font("Arial", Font.BOLD, 12));
        deleteBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // 삭제 버튼의 표시 여부 결정
        if (!ClientMain.currentUserId.equals(this.postWriterId)) {
            deleteBtn.setVisible(false);
        }
        
        deleteBtn.addActionListener(e -> {
        	int confirm = JOptionPane.showConfirmDialog(this, "정말 이 글을 삭제하시겠습니까?", "삭제 확인", JOptionPane.YES_NO_OPTION);
        	if(confirm == JOptionPane.YES_OPTION) {
        		// [핵심 수정] 로컬 삭제 대신 서버로 요청을 보냄
        		ClientMain.clientService.sendDeleteRequest(postId);
        	}
        });
        
        topPanel.add(writerLabel, BorderLayout.WEST);
        topPanel.add(deleteBtn, BorderLayout.EAST);
        
        add(topPanel, BorderLayout.NORTH);

        // --- 2. 사진 ---
        JLabel imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        
        if (imagePath != null && !imagePath.isEmpty()) {
            ImageIcon originalIcon = new ImageIcon(imagePath);
            Image img = originalIcon.getImage();
            Image scaledImg = img.getScaledInstance(300, 250, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(scaledImg));
        } else {
            imageLabel.setText("이미지 없음");
            imageLabel.setOpaque(true);
            imageLabel.setBackground(Color.LIGHT_GRAY);
        }
        add(imageLabel, BorderLayout.CENTER);

        // --- 3. 하단 (좋아요/댓글 버튼) ---
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(Color.WHITE);
        
        JLabel contentLabel = new JLabel(" 📝 " + content);
        contentLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(Color.WHITE);
        
        // (1) 좋아요 버튼
        likeButton = new JButton("♥");
        likeButton.setBackground(Color.PINK); 
        likeButton.setOpaque(true); 
        likeButton.setBorderPainted(false);
        
        likeCountLabel = new JLabel("0");
        
        likeButton.addActionListener(e -> {
            ClientMain.clientService.sendLike(postId);
        });

        // (2) 댓글 버튼
        JButton commentBtn = new JButton("💬 댓글");
        commentBtn.setBackground(new Color(230, 230, 250)); 
        commentBtn.setOpaque(true);
        commentBtn.setBorderPainted(false);
        commentBtn.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        
        commentBtn.addActionListener(e -> {
            showCommentDialog();
        });

        btnPanel.add(likeButton);
        btnPanel.add(likeCountLabel);
        btnPanel.add(Box.createHorizontalStrut(10)); 
        btnPanel.add(commentBtn); 

        bottomPanel.add(contentLabel, BorderLayout.CENTER);
        bottomPanel.add(btnPanel, BorderLayout.SOUTH);

        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    // 서버에서 댓글 신호가 왔을 때 실행
    public void receiveComment(String newComment) {
        commentList.add(newComment);
        
        if (activeCommentArea != null) {
            if(activeCommentArea.getText().contains("아직 작성된")) { 
                 activeCommentArea.setText(newComment);
            } else {
            	 activeCommentArea.append("\n" + newComment);
            }
            activeCommentArea.setCaretPosition(activeCommentArea.getDocument().getLength());
        }
    }
    
    // 서버에서 좋아요 신호가 왔을 때 실행
    public void receiveLike(String userId) {
        String currentUserId = ClientMain.currentUserId;
        
        if (likeUsers.contains(userId)) {
            likeUsers.remove(userId);
            currentLikes--;
            
            if(userId.equals(currentUserId)) {
                likeButton.setBackground(Color.PINK);
            }
        } else {
            likeUsers.add(userId);
            currentLikes++;
            
            if(userId.equals(currentUserId)) {
                likeButton.setBackground(Color.RED);
            }
        }
        
        likeCountLabel.setText(String.valueOf(currentLikes)); 
    }

    private void showCommentDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "댓글 목록", true);
        dialog.setSize(300, 400);
        dialog.setLayout(new BorderLayout());
        
        JTextArea activeCommentArea = new JTextArea(); // 지역 변수로 선언
        this.activeCommentArea = activeCommentArea; // 멤버 변수에 할당
        activeCommentArea.setEditable(false); 
        activeCommentArea.setBackground(new Color(245, 245, 245));
        
        StringBuilder sb = new StringBuilder();
        if(commentList.isEmpty()) {
            sb.append("아직 작성된 댓글이 없습니다.\n첫 번째 댓글을 남겨보세요!");
        } else {
            for(String c : commentList) {
                sb.append(c).append("\n");
            }
        }
        activeCommentArea.setText(sb.toString());
        
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                FeedPanel.this.activeCommentArea = null; // 멤버 변수 초기화
            }
        });
        
        dialog.add(new JScrollPane(activeCommentArea), BorderLayout.CENTER);
        
        JPanel inputPanel = new JPanel(new BorderLayout());
        JTextField inputField = new JTextField();
        JButton sendBtn = new JButton("등록");
        
        sendBtn.addActionListener(ev -> {
            String text = inputField.getText().trim();
            if(!text.isEmpty()) {
                ClientMain.clientService.sendComment(postId, text);
                inputField.setText("");
            }
        });
        
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendBtn, BorderLayout.EAST);
        
        dialog.add(inputPanel, BorderLayout.SOUTH);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    
    public int getPostId() {
    	return this.postId;
    }
}
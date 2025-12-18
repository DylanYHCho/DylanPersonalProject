package com.jstargram.client.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import com.jstargram.client.main.ClientMain;
import com.jstargram.client.network.ChatListener;
import com.jstargram.common.dto.ChatMessage;
import com.jstargram.common.dto.PresenceInfo;

public class PhotoBoardUI extends JFrame implements ChatListener {

    private static final long serialVersionUID = 1L;

    // --- 카드 레이아웃 관리 ---
    private CardLayout cardLayout;
    private JPanel rootPanel;
    private final String CARD_FEED = "FEED";
    private final String CARD_LIST = "LIST";
    private final String CARD_CHAT = "CHAT";
    private final String CARD_STATUS = "STATUS";

    // --- 피드 관련 컴포넌트 ---
    private ArrayList<FeedPanel> feedList = new ArrayList<>();
    private JPanel feedContainer;
    private JButton chatMenuButton; 

    // --- 채팅 관련 컴포넌트 ---
    private DefaultListModel<String> roomListModel;
    private JList<String> roomList;
    private ChatPanel currentChatPanel;
    private JPanel chatScreen;        
    private JButton backButton;
    private JLabel roomTitleLabel;    
    private JLabel userStatusLabel;   
    private JButton statusButton; 

    // --- 접속자 상태 관련 ---
    private UserStatusPanel userStatusPanel;
    private Map<String, PresenceInfo> presenceMap = new HashMap<>();

    public PhotoBoardUI() {
        setTitle("J-StarGram - " + ClientMain.currentUserNickname);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 600);
        setLocationRelativeTo(null);

        // 창이 켜지면 서버 소식 듣기 시작
        ClientMain.clientService.startListening(this);

        initUI();
        setVisible(true);
    }

    private void initUI() {
        cardLayout = new CardLayout();
        rootPanel = new JPanel(cardLayout);

        // 1. 피드 화면
        JPanel feedScreen = createFeedScreen();

        // 2. 채팅 목록 화면
        JPanel listScreen = createRoomListScreen();

        // 3. 채팅방 화면
        chatScreen = createChatScreenPanel();

        // 4. 접속자/위치 화면
        userStatusPanel = new UserStatusPanel(this);

        rootPanel.add(feedScreen, CARD_FEED);
        rootPanel.add(listScreen, CARD_LIST);
        rootPanel.add(chatScreen, CARD_CHAT);
        rootPanel.add(userStatusPanel, CARD_STATUS);

        add(rootPanel);

        // 시작은 피드 화면
        cardLayout.show(rootPanel, CARD_FEED);
    }

    // =========================================================
    // A. 피드 관련 UI
    // =========================================================

    private JPanel createFeedScreen() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.LIGHT_GRAY);

        JButton writeBtn = new JButton("글쓰기");
        writeBtn.setBackground(Color.YELLOW);
        writeBtn.setOpaque(true);
        writeBtn.setBorderPainted(false);
        writeBtn.addActionListener(e -> new WritePostUI(this));

        // 채팅 목록으로 가는 버튼
        chatMenuButton = new JButton("💬 채팅");
        chatMenuButton.setOpaque(true);
        chatMenuButton.setBackground(new Color(150, 200, 255));
        chatMenuButton.setBorderPainted(false);
        chatMenuButton.addActionListener(e -> showRoomList());

        JPanel titleArea = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titleArea.setOpaque(false);
        titleArea.add(new JLabel("J-StarGram"));

        topPanel.add(titleArea, BorderLayout.CENTER);
        topPanel.add(writeBtn, BorderLayout.WEST);
        topPanel.add(chatMenuButton, BorderLayout.EAST);

        panel.add(topPanel, BorderLayout.NORTH);

        feedContainer = new JPanel();
        feedContainer.setLayout(new BoxLayout(feedContainer, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane(feedContainer);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    // 게시글 추가 함수
    public void addPost(int postId, String writerId, String writerNickname, String content, String imgPath) {
        FeedPanel newPost = new FeedPanel(this, postId, writerId, writerNickname, content, imgPath);
        feedList.add(0, newPost);
        feedContainer.add(newPost, 0);
        feedContainer.add(Box.createVerticalStrut(10), 0);
        feedContainer.revalidate();
        feedContainer.repaint();
    }

    // 게시글 삭제 함수
    public void deletePost(FeedPanel panel) {
        feedList.remove(panel);
        feedContainer.remove(panel);
        feedContainer.revalidate();
        feedContainer.repaint();
    }

    // =========================================================
    // B. 채팅 관련 UI
    // =========================================================

    /** 채팅 목록 화면 생성 */
    private JPanel createRoomListScreen() {
        JPanel panel = new JPanel(new BorderLayout());

        roomListModel = new DefaultListModel<>();
        roomList = new JList<>(roomListModel);
        roomList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // 기본 방 목록
        roomListModel.addElement("전체 채팅방");
        roomListModel.addElement("공지사항");

        roomList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    enterSelectedRoom();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(roomList);

        // [상단] 제목 + 접속자 버튼
        JLabel titleLabel = new JLabel("채팅방 목록", SwingConstants.CENTER);
        statusButton = new JButton("접속자/위치");
        statusButton.addActionListener(e -> showUserStatusPanel());

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(titleLabel, BorderLayout.CENTER);
        topPanel.add(statusButton, BorderLayout.EAST);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // [하단] 버튼 패널 (입장 / 방 만들기 / 뒤로가기)
        JPanel botPanel = new JPanel(new GridLayout(1, 3)); // 3개 버튼을 나란히 배치
        
        JButton enterBtn = new JButton("입장");
        enterBtn.addActionListener(e -> enterSelectedRoom());
        
        // [추가] 방 만들기 버튼
        JButton createRoomBtn = new JButton("+ 방 만들기");
        createRoomBtn.setBackground(new Color(200, 255, 200));
        createRoomBtn.setOpaque(true);
        createRoomBtn.setBorderPainted(false);
        createRoomBtn.addActionListener(e -> {
            String newRoomName = JOptionPane.showInputDialog(this, "새로운 방 이름을 입력하세요:");
            if (newRoomName != null && !newRoomName.trim().isEmpty()) {
                newRoomName = newRoomName.trim();
                // 리스트에 없으면 추가
                if (!roomListModel.contains(newRoomName)) {
                    roomListModel.addElement(newRoomName);
                }
                // 바로 입장
                enterChatRoom(newRoomName);
            }
        });

        JButton backToFeedButton = new JButton("← 피드");
        backToFeedButton.addActionListener(e -> showFeedScreen());
        
        botPanel.add(backToFeedButton);
        botPanel.add(createRoomBtn);
        botPanel.add(enterBtn);
        
        panel.add(botPanel, BorderLayout.SOUTH);

        return panel;
    }
    
    /** 채팅방 화면 패널 생성 */
    private JPanel createChatScreenPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel topWrapper = new JPanel(new BorderLayout());

        JPanel topBar = new JPanel(new BorderLayout());
        backButton = new JButton("← 목록");
        roomTitleLabel = new JLabel("상대 이름", SwingConstants.CENTER);

        topBar.add(backButton, BorderLayout.WEST);
        topBar.add(roomTitleLabel, BorderLayout.CENTER);

        userStatusLabel = new JLabel("상태 확인 중...", SwingConstants.CENTER);

        topWrapper.add(topBar, BorderLayout.NORTH);
        topWrapper.add(userStatusLabel, BorderLayout.SOUTH);

        // 뒤로가기 누르면 방 리스트 화면으로
        backButton.addActionListener(e -> {
            if (currentChatPanel != null) {
                // 방 나가기 신호 전송
                ClientMain.clientService.leaveChatRoom(currentChatPanel.getRoomId()); 
                chatScreen.remove(currentChatPanel);
                currentChatPanel = null;
            }
            showRoomList();
            chatScreen.revalidate();
            chatScreen.repaint();
        });

        panel.add(topWrapper, BorderLayout.NORTH);
        return panel;
    }

    /** 리스트에서 선택된 방 입장 */
    private void enterSelectedRoom() {
        String roomId = roomList.getSelectedValue();
        if (roomId == null) {
            JOptionPane.showMessageDialog(this, "방을 선택해주세요.");
            return;
        }
        enterChatRoom(roomId);
    }

    /** 특정 roomId로 채팅방 입장 */
    private void enterChatRoom(String roomId) {
        // 서버에 방 참가 요청
        ClientMain.clientService.joinChatRoom(roomId);

        if (currentChatPanel != null) {
            chatScreen.remove(currentChatPanel);
        }

        // 새 채팅 패널 생성 후 붙이기
        currentChatPanel = new ChatPanel(roomId, ClientMain.clientService); 
        chatScreen.add(currentChatPanel, BorderLayout.CENTER);

        // 상단 제목
        String targetUserOrRoom = extractTargetUser(roomId);
        roomTitleLabel.setText(targetUserOrRoom);

        userStatusLabel.setText("상태 확인 중...");
        requestUserStatus(targetUserOrRoom);

        // 화면 전환
        showChatRoom();
        chatScreen.revalidate();
        chatScreen.repaint();
    }

    private String extractTargetUser(String roomId) {
        if (!roomId.startsWith("dm-")) {
            return roomId;
        }

        String[] parts = roomId.split("-");
        if (parts.length < 3) return roomId;

        String u1 = parts[1];
        String u2 = parts[2];

        return ClientMain.currentUserId.equals(u1) ? u2 : u1;
    }

    private void requestUserStatus(String targetUserOrRoom) {
        // TODO: 서버에 상태 요청 로직 구현
    }

    // =========================================================
    // C. 카드 전환 메서드
    // =========================================================

    public void showFeedScreen() { cardLayout.show(rootPanel, CARD_FEED); }
    public void showRoomList() { cardLayout.show(rootPanel, CARD_LIST); }
    public void showChatRoom() { cardLayout.show(rootPanel, CARD_CHAT); }
    public void showUserStatusPanel() {
        userStatusPanel.setUserList(presenceMap.values());
        cardLayout.show(rootPanel, CARD_STATUS);
    }

    // =========================================================
    // D. ChatListener 구현 (서버 응답 수신)
    // =========================================================
    
    @Override
    public void onMessageReceived(ChatMessage message) {
        SwingUtilities.invokeLater(() -> {
            if (currentChatPanel != null &&
                    message.getRoomId().equals(currentChatPanel.getRoomId())) {

                String sender = (message.getSenderName() != null)
                        ? message.getSenderName()
                        : message.getSenderId();

                currentChatPanel.appendMessage(sender, message.getContent());
            }
        });
    }

    @Override
    public void onSystemMessage(String roomId, String text) {
        SwingUtilities.invokeLater(() -> {
            if (currentChatPanel != null &&
                    roomId.equals(currentChatPanel.getRoomId())) {
                currentChatPanel.appendSystemMessage(text);
            }
        });
    }
    
    @Override
    public void onPresenceUpdated(PresenceInfo info) {
        presenceMap.put(info.getUserId(), info);

        SwingUtilities.invokeLater(() -> {
            userStatusPanel.updateUser(info);
            
            if (currentChatPanel != null) {
                String target = extractTargetUser(currentChatPanel.getRoomId());
                if (target.equals(info.getUserId())) {
                    updateTopUserStatusLabel(info);
                }
            }
        });
    }

    private void updateTopUserStatusLabel(PresenceInfo info) {
        if (info.isOnline()) {
            userStatusLabel.setText("온라인");
        } else if (info.getLastSeenText() != null) {
            userStatusLabel.setText("last seen " + info.getLastSeenText());
        } else {
            userStatusLabel.setText("오프라인");
        }
    }

    @Override
    public void onDisconnected(Exception e) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this,
                    "서버와 연결이 끊어졌습니다.",
                    "연결 종료",
                    JOptionPane.WARNING_MESSAGE);
        });
    }

    // =========================================================
    // E. 피드/채팅 공통 (서버 브로드캐스트 핸들러)
    // =========================================================

    public void handleBroadcast(String msg) {
        String[] parts = msg.split("\\|");
        if (parts.length < 2) return;

        String command = parts[0];
        int targetId;

        try {
            targetId = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            System.err.println("잘못된 ID 형식: " + parts[1]);
            return;
        }

        if (command.equals("POST") && parts.length >= 6) {
            String writerId = parts[2];
            String writerNickname = parts[3];
            String content = parts[4];
            String imagePath = parts[5];

            if (targetId > 0 && !writerId.isEmpty()) {
                addPost(targetId, writerId, writerNickname, content, imagePath);
            }
        }
        else if (command.equals("DELETE_POST") && parts.length == 2) {
            for (FeedPanel panel : feedList) {
                if (panel.getPostId() == targetId) {
                    deletePost(panel);
                    break;
                }
            }
        }
        else if (command.equals("LIKE") || command.equals("COMMENT")) {
            for (FeedPanel panel : feedList) {
                if (panel.getPostId() == targetId) {
                    if (command.equals("LIKE") && parts.length > 2) {
                        panel.receiveLike(parts[2]);
                    } else if (command.equals("COMMENT") && parts.length > 3) {
                        panel.receiveComment(parts[2] + ": " + parts[3]);
                    }
                    break;
                }
            }
        }
    }
}
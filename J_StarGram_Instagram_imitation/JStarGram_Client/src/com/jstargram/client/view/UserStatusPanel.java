package com.jstargram.client.view;

// 왼쪽: 접속자 목록, 오른쪽: 선택한 유저의 위치 정보 UI

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import com.jstargram.common.dto.PresenceInfo;

// [수정] PhotoBoardUI가 이 역할을 겸하므로, PhotoBoardUI를 받도록 수정합니다.
public class UserStatusPanel extends JPanel {

    private final PhotoBoardUI mainWindow; // [수정] MainWindow -> PhotoBoardUI로 변경

    private JTable userTable;
    private DefaultTableModel tableModel;
    private JLabel locationLabel;
    private JButton openMapButton;      // ★ 카카오 지도 열기 버튼
    private String currentMapUrl = null; // ★ 현재 선택된 유저의 지도 URL


    // userId -> PresenceInfo
    private Map<String, PresenceInfo> presenceMap = new LinkedHashMap<>();

    public UserStatusPanel(PhotoBoardUI mainWindow) { // [수정] 생성자 타입 변경
        this.mainWindow = mainWindow;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));

        // 상단 타이틀
        JLabel titleLabel = new JLabel("실시간 접속자 / 위치 정보", SwingConstants.CENTER);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 18f));
        add(titleLabel, BorderLayout.NORTH);

        // 중앙: 좌측 테이블 + 우측 위치 표시
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        add(centerPanel, BorderLayout.CENTER);

        // ---- 왼쪽: 유저 리스트 테이블 ----
        String[] columnNames = { "유저ID", "닉네임", "상태", "마지막 접속" };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        userTable = new JTable(tableModel);
        userTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = userTable.getSelectedRow();
                if (row >= 0) {
                    String userId = (String) tableModel.getValueAt(row, 0);
                    showUserLocation(userId);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(userTable);
        centerPanel.add(scrollPane);

        // ---- 오른쪽: 위치/지도 영역 ----
        JPanel locPanel = new JPanel(new BorderLayout());
        locationLabel = new JLabel("유저를 선택하면 위치 정보가 표시됩니다.", SwingConstants.CENTER);

        // 아래쪽에 버튼 하나 추가
        openMapButton = new JButton("카카오 지도에서 보기");
        openMapButton.setEnabled(false); // 처음에는 비활성화
        openMapButton.addActionListener(e -> openKakaoMapInBrowser());

        JPanel locInner = new JPanel(new BorderLayout());
        locInner.add(locationLabel, BorderLayout.CENTER);
        locInner.add(openMapButton, BorderLayout.SOUTH);

        locPanel.add(locInner, BorderLayout.CENTER);
        centerPanel.add(locPanel);

        // ---- 하단: 뒤로가기 버튼 ----
        JButton backButton = new JButton("← 채팅방 목록으로");
        backButton.addActionListener(e -> mainWindow.showRoomList()); // [수정] PhotoBoardUI의 showRoomList 호출
        add(backButton, BorderLayout.SOUTH);
    }

    /** presence 하나 갱신 */
    public void updateUser(PresenceInfo info) {
        presenceMap.put(info.getUserId(), info);
        refreshTable();
    }

    /** 전체를 한 번에 세팅하고 싶을 때 */
    public void setUserList(Collection<PresenceInfo> list) {
        presenceMap.clear();
        for (PresenceInfo p : list) {
            presenceMap.put(p.getUserId(), p);
        }
        refreshTable();
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (PresenceInfo p : presenceMap.values()) {
            String status = p.isOnline() ? "온라인" : "오프라인";
            String lastSeen = p.getLastSeenText() != null ? p.getLastSeenText() : "-";
            tableModel.addRow(new Object[] {
                    p.getUserId(),
                    p.getNickname(),
                    status,
                    lastSeen
            });
        }
    }
    
    private String buildKakaoLinkUrl(String name, double lat, double lon) {
        try {
            // 이름은 URL 인코딩 (한글 깨짐 방지)
            String encodedName = java.net.URLEncoder.encode(name, "UTF-8");
            return "https://map.kakao.com/link/map/"
                    + encodedName + "," + lat + "," + lon;
        } catch (Exception e) {
            return "https://map.kakao.com/link/map/" + lat + "," + lon;
        }
    }
    
    private void openKakaoMapInBrowser() {
        if (currentMapUrl == null) return;

        try {
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().browse(new java.net.URI(currentMapUrl));
            } else {
                JOptionPane.showMessageDialog(this,
                        "이 시스템에서는 기본 브라우저를 열 수 없습니다.",
                        "오류",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "카카오 지도를 여는 중 오류가 발생했습니다.\n" + e.getMessage(),
                    "오류",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showUserLocation(String userId) {
        PresenceInfo p = presenceMap.get(userId);
        if (p == null) {
            locationLabel.setIcon(null);
            locationLabel.setText("선택된 유저 정보가 없습니다.");
            openMapButton.setEnabled(false);
            currentMapUrl = null;
            return;
        }

        // 기본 텍스트
        String textBase = "<html><center>" + p.getNickname() + "<br>" +
                (p.getLocationText() != null ? p.getLocationText() : "") +
                "</center></html>";

        locationLabel.setIcon(null);
        locationLabel.setText(textBase);

        // 위도/경도가 없으면 버튼 비활성화
        if (p.getLatitude() == null || p.getLongitude() == null) {
            openMapButton.setEnabled(false);
            currentMapUrl = null;
            return;
        }

        // Kakao 지도 링크 URL 생성
        currentMapUrl = buildKakaoLinkUrl(
                p.getNickname(),
                p.getLatitude(),
                p.getLongitude()
        );
        openMapButton.setEnabled(true);
    }
}
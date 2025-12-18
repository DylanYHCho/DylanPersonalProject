package com.jstargram.client.util; // 패키지 수정

import java.util.ArrayList;

public class TextFilter {

    // 욕설 목록 리스트 (여기에 단어를 추가하면)
    private ArrayList<String> badWordList;

    // 여기에 추가해요
    public TextFilter() {
        badWordList = new ArrayList<>();
        
        badWordList.add("씨발");
        badWordList.add("존나");
        badWordList.add("새끼");
        badWordList.add("병신");
    }

    public String filter(String inputMessage) {
        String outputMessage = inputMessage; // 원본을 복사

        // 리스트에 있는 단어를 하나씩 꺼내서 검사
        for (String badWord : badWordList) {
            if (outputMessage.contains(badWord)) {
                // "**"로 교체
                outputMessage = outputMessage.replace(badWord, "**");
            }
        }
        
        return outputMessage; // 메시지 반환
    }
}
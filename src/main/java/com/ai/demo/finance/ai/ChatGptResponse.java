package com.ai.demo.finance.ai;

import java.util.List;
import lombok.Data;

@Data
public class ChatGptResponse {
    private List<Choice> choices;

    @Data
    public static class Choice {
        private ChatGptRequest.Message message;
    }
}
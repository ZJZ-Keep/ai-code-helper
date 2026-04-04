package org.example.aicodehelper.guardrail;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.InputGuardrailResult;

import java.util.Set;

/**
 * 安全检测输入护轨
 */
public class SafeInputGuardrail implements InputGuardrail {

    private static final Set<String> sensitiveWords = Set.of("kill", "evil","吸毒");

    /**
     * 检测用户输入是否安全
     */
    @Override
    public InputGuardrailResult validate(UserMessage userMessage) {
        String inputText = userMessage.singleText().toLowerCase();

        if (inputText.isEmpty()) {
            return success();
        }

        for (String word : sensitiveWords) {
            if (inputText.contains(word.toLowerCase())) {
                return fatal("Sensitive word detected: " + word);
            }
        }
        return success();
    }
}

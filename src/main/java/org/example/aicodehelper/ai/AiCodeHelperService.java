package org.example.aicodehelper.ai;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.Result;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.guardrail.InputGuardrails;
import org.example.aicodehelper.guardrail.SafeInputGuardrail;
import reactor.core.publisher.Flux;
import java.util.List;
/**
 * AI 助手服务
 * @author zjz
 */
@InputGuardrails({SafeInputGuardrail.class})
public interface AiCodeHelperService {
    @SystemMessage(fromResource = "SYSTEM_MESSAGE.txt")
    String chat(@MemoryId String memoryId, @UserMessage String message);

    @SystemMessage(fromResource = "SYSTEM_MESSAGE.txt")
    Report chatForReport(@MemoryId String memoryId, @UserMessage String message);

    record Report(String name, List<String> suggestionList){}

    @SystemMessage(fromResource = "SYSTEM_MESSAGE.txt")
    Result<String> chatForRag(@MemoryId String memoryId, @UserMessage String message);

    // 流式对话
    @SystemMessage(fromResource = "SYSTEM_MESSAGE.txt")
    Flux<String> chatStream(@MemoryId int memoryId, @UserMessage String userMessage);

}
package org.example.aicodehelper.ai;

import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.example.aicodehelper.tools.InterviewQuestionTool;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * 创建AiCodeHelperService
 */
@Configuration
public class AiCodeHelperServiceFactory {
    @Resource
    private ChatModel myQwenChatModel;
    @Resource
    private ContentRetriever contentRetriever;
    @Resource
    private McpToolProvider mcpToolProvider;
    @Resource
    private StreamingChatModel qwenStreamingChatModel;
    @Bean
    public AiCodeHelperService aiCodeHelperService(){
        //return AiServices.create(AiCodeHelperService.class,qwenChatModel);
        return AiServices.builder(AiCodeHelperService.class)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(10))
                .chatModel(myQwenChatModel)
                .streamingChatModel(qwenStreamingChatModel)//流式对话
                .contentRetriever(contentRetriever)//rag检索
                .tools(new InterviewQuestionTool())//工具调用
                .toolProvider(mcpToolProvider)//mcp工具调用
                .build();
    }
}

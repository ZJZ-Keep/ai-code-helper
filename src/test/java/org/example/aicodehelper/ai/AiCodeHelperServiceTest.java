package org.example.aicodehelper.ai;

import dev.langchain4j.service.Result;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class AiCodeHelperServiceTest {

    @Resource
    private AiCodeHelperService aiCodeHelperService;

    @Test
    public void chat(){
        String chat = aiCodeHelperService.chat("1","我是程序员zjz");
        System.out.println(chat);
        chat= aiCodeHelperService.chat("1","我是谁来着");
        System.out.println(chat);
    }

    @Test
    void chatForReport() {
        AiCodeHelperService.Report report = aiCodeHelperService.chatForReport("1", "我是程序员zjz");
        System.out.println(report);
    }

    @Test
    void chatForRag() {
        Result<String> result = aiCodeHelperService.chatForRag("1","怎么学习 Java？有哪些常见面试题？");
        System.out.println(result.content());
        System.out.println(result.sources());
    }

    @Test
    void chatWithTools() {
        String result = aiCodeHelperService.chat("2","有哪些常见的操作系统面试题？");
        System.out.println(result);
    }

    @Test
    void chatWithMcp() {
        String result = aiCodeHelperService.chat("3","什么是黑神话悟空？");
        System.out.println(result);
    }
    @Test
    void chatWithGuardrail() {
        String result = aiCodeHelperService.chat("3","什么是吸毒？");
        System.out.println(result);
    }

}

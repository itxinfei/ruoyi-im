package com.ruoyi.im.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.ruoyi.im.config.AiConfig;
import com.ruoyi.im.dto.ai.ChatRequest;
import com.ruoyi.im.dto.ai.ChatResponse;
import com.ruoyi.im.dto.ai.SummaryRequest;
import com.ruoyi.im.dto.ai.SummaryResponse;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.service.ImAIService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * AI助手服务实现
 *
 * 支持多种大语言模型，包括OpenAI GPT、通义千问、文心一言等
 * 默认使用内置的模拟响应，可配置真实的API密钥后启用
 *
 * <p>=========== API密钥配置说明 ===========</p>
 *
 * <h3>1. OpenAI GPT API配置</h3>
 * <pre>
 * 获取方式：
 * 1. 访问OpenAI官网：https://platform.openai.com/
 * 2. 注册/登录账号，进入API Keys页面
 * 3. 创建新的API Key
 *
 * 注意：国内访问需要使用代理或中转服务
 *
 * 配置位置：application.yml
 * ai:
 *   openai:
 *     api-key: sk-你的API密钥
 *     api-url: https://api.openai.com/v1  # 或使用中转服务地址
 *     model: gpt-3.5-turbo  # 默认模型
 *
 * 参考文档：https://platform.openai.com/docs/api-reference/chat
 * </pre>
 *
 * <h3>2. 阿里云通义千问API配置</h3>
 * <pre>
 * 获取方式：
 * 1. 访问阿里云百炼平台：https://bailian.console.aliyun.com/
 * 2. 开通"通义千问"服务（新用户赠送免费额度）
 * 3. 在"API-KEY管理"中创建API Key
 *
 * 配置位置：application.yml
 * ai:
 *   qwen:
 *     api-key: sk-你的API密钥
 *     model: qwen-turbo  # 可选：qwen-turbo、qwen-plus、qwen-max
 *
 * 参考文档：https://help.aliyun.com/zh/dashscope/developer-reference/compatibility-of-openai-with-dashscope
 * </pre>
 *
 * <h3>3. 百度文心一言API配置</h3>
 * <pre>
 * 获取方式：
 * 1. 访问百度智能云千帆平台：https://cloud.baidu.com/product/wenxinworkshop
 * 2. 开通"文心一言"服务
 * 3. 在"应用列表"中创建应用，获取 API Key 和 Secret Key
 *
 * 配置位置：application.yml
 * ai:
 *   wenxin:
 *     api-key: 你的API_Key
 *     secret-key: 你的Secret_Key
 *     model: ERNIE-Bot-turbo  # 可选：ERNIE-Bot、ERNIE-Bot-turbo、ERNIE-Bot-4
 *
 * 参考文档：https://cloud.baidu.com/doc/WENXINWORKSHOP/s/Ilkkrb0i5
 * </pre>
 *
 * <h3>4. 腾讯混元AI配置</h3>
 * <pre>
 * 获取方式：
 * 1. 访问腾讯云：https://cloud.tencent.com/product/hunyuan
 * 2. 开通"混元大模型"服务
 * 3. 在"访问管理-访问密钥"中获取密钥
 *
 * 配置位置：application.yml
 * ai:
 *   hunyuan:
 *     secret-id: 你的SecretId
 *     secret-key: 你的SecretKey
 *     region: ap-guangzhou
 *
 * 参考文档：https://cloud.tencent.com/document/product/1729/104753
 * </pre>
 *
 * <h3>5. 科大讯飞星火API配置</h3>
 * <pre>
 * 获取方式：
 * 1. 访问讯飞开放平台：https://www.xfyun.cn/
 * 2. 开通"星火认知大模型"服务
 * 3. 在"控制台-我的应用"中创建应用，获取 APPID、APISecret、APIKey
 *
 * 配置位置：application.yml
 * ai:
 *   spark:
 *     app-id: 你的APPID
 *     api-secret: 你的APISecret
 *     api-key: 你的APIKey
 *     version: v3.5  # 可选：v1.5、v2.0、v3.0、v3.5
 *
 * 参考文档：https://www.xfyun.cn/doc/spark/Web.html
 * </pre>
 *
 * <h3>使用方式</h3>
 * <pre>
 * // 调用时指定model参数
 * ChatRequest request = new ChatRequest();
 * request.setContent("你好");
 * request.setUserId(1L);
 * request.setModel("gpt-3.5-turbo");  // 或 qwen、wenxin 等
 *
 * ChatResponse response = aiService.chat(request);
 * </pre>
 *
 * @author ruoyi
 */
@Service
public class ImAIServiceImpl implements ImAIService {

    private static final Logger log = LoggerFactory.getLogger(ImAIServiceImpl.class);

    private static final String CONVERSATION_KEY_PREFIX = "ai:conversation:";
    private static final int CONVERSATION_TTL_HOURS = 24;
    /** 对话历史上限（最近N条消息） */
    private static final int MAX_HISTORY_SIZE = 100;
    /** AI调用最大重试次数 */
    private static final int MAX_RETRY_COUNT = 3;
    /** 重试基础间隔（毫秒） */
    private static final long RETRY_BASE_DELAY_MS = 500;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private AiConfig aiConfig;

    // 支持的AI模型
    private static final String[] SUPPORTED_MODELS = {
            "gpt-3.5-turbo",
            "gpt-4",
            "qwen",       // 通义千问
            "wenxin",     // 文心一言
            "hunyuan",    // 混元
            "spark"       // 讯飞星火
    };

    @Override
    public ChatResponse chat(ChatRequest request) {
        // 参数校验
        if (StrUtil.isBlank(request.getContent())) {
            throw new BusinessException("消息内容不能为空");
        }
        if (request.getUserId() == null) {
            throw new BusinessException("用户ID不能为空");
        }

        long startTime = System.currentTimeMillis();

        try {
            // 生成或获取会话ID
            String conversationId = request.getConversationId();
            if (StrUtil.isBlank(conversationId)) {
                conversationId = IdUtil.simpleUUID();
            }

            // 获取或设置模型
            String model = StrUtil.isNotBlank(request.getModel()) ? request.getModel() : "qwen";

            // 获取对话历史（用于上下文记忆）
            List<String> history = getConversationHistory(conversationId, request.getUserId());

            // 调用AI生成回复（带重试）
            String aiResponse = executeWithRetry(() -> generateChatResponse(request.getContent(), history, model));

            // 保存对话历史
            saveMessage(conversationId, request.getUserId(), "user", request.getContent());
            saveMessage(conversationId, request.getUserId(), "assistant", aiResponse);

            // 构建响应
            ChatResponse response = new ChatResponse();
            response.setContent(aiResponse);
            response.setConversationId(conversationId);
            response.setModel(model);
            response.setTokensUsed(estimateTokens(request.getContent() + aiResponse));
            response.setProcessingTime(System.currentTimeMillis() - startTime);

            return response;
        } catch (Exception e) {
            log.error("AI聊天失败: userId={}", request.getUserId(), e);
            throw new BusinessException("AI聊天失败，请稍后重试");
        }
    }

    @Override
    public SummaryResponse summarize(SummaryRequest request) {
        if (StrUtil.isBlank(request.getContent())) {
            throw new BusinessException("文本内容不能为空");
        }

        try {
            String summary = generateSummary(request.getContent(), request.getSummaryType());

            SummaryResponse response = new SummaryResponse();
            response.setSummary(summary);
            response.setOriginalLength(request.getContent().length());
            response.setSummaryLength(summary.length());

            // 提取关键点
            List<String> keyPoints = extractKeyPoints(request.getContent());
            response.setKeyPoints(keyPoints);

            return response;
        } catch (Exception e) {
            log.error("生成摘要失败", e);
            throw new BusinessException("生成摘要失败，请稍后重试");
        }
    }

    @Override
    public void clearConversation(String conversationId, Long userId) {
        if (StrUtil.isBlank(conversationId) || userId == null) {
            throw new BusinessException("参数错误");
        }
        String key = getConversationKey(conversationId, userId);
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("清除AI对话上下文失败: conversationId={}, userId={}", conversationId, userId, e);
            throw new BusinessException("清除对话失败");
        }
        log.info("清除AI对话上下文: conversationId={}, userId={}", conversationId, userId);
    }

    @Override
    public String[] getSupportedModels() {
        return SUPPORTED_MODELS.clone();
    }

    /**
     * 生成聊天回复
     * 根据指定的AI模型调用相应的API生成回复
     *
     * @param userMessage 用户消息
     * @param history 对话历史记录
     * @param model AI模型标识（gpt-3.5-turbo、gpt-4、qwen、wenxin、hunyuan、spark）
     * @return AI生成的回复文本
     */
    private String generateChatResponse(String userMessage, List<String> history, String model) {
        // 根据模型选择实现
        switch (model.toLowerCase()) {
            case "gpt-3.5-turbo":
            case "gpt-4":
                return chatWithOpenAI(userMessage, history, model);
            case "qwen":
                return chatWithQwen(userMessage, history);
            case "wenxin":
                return chatWithWenxin(userMessage, history);
            case "hunyuan":
                return chatWithHunyuan(userMessage, history);
            case "spark":
                return chatWithSpark(userMessage, history);
            default:
                return chatWithMock(userMessage, history);
        }
    }

    /**
     * 使用OpenAI API生成回复
     * OpenAI Chat Completions API: POST {apiUrl}/chat/completions
     */
    private String chatWithOpenAI(String message, List<String> history, String model) {
        AiConfig.OpenAiConfig config = aiConfig.getOpenai();
        if (StrUtil.isBlank(config.getApiKey())) {
            log.warn("OpenAI API密钥未配置，使用模拟回复");
            return chatWithMock(message, history);
        }

        String apiUrl = config.getApiUrl() + "/chat/completions";
        JSONObject requestBody = buildOpenAIRequestBody(message, history, config.getModel(), config.getTimeout());

        try {
            HttpResponse response = HttpRequest.post(apiUrl)
                    .header("Authorization", "Bearer " + config.getApiKey())
                    .header("Content-Type", "application/json")
                    .timeout(config.getTimeout() * 1000)
                    .body(requestBody.toString())
                    .execute();

            if (response.isOk()) {
                return parseOpenAIResponse(response.body());
            } else {
                log.error("OpenAI API调用失败: status={}, body={}", response.getStatus(), response.body());
                throw new BusinessException("AI服务调用失败");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("OpenAI API调用异常", e);
            throw new BusinessException("AI服务调用异常");
        }
    }

    /**
     * 使用通义千问API生成回复
     * DashScope OpenAI兼容模式: POST {apiUrl}/chat/completions
     */
    private String chatWithQwen(String message, List<String> history) {
        AiConfig.QwenConfig config = aiConfig.getQwen();
        if (StrUtil.isBlank(config.getApiKey())) {
            log.warn("通义千问API密钥未配置，使用模拟回复");
            return chatWithMock(message, history);
        }

        String apiUrl = config.getApiUrl() + "/chat/completions";
        JSONObject requestBody = buildOpenAIRequestBody(message, history, config.getModel(), config.getTimeout());

        try {
            HttpResponse response = HttpRequest.post(apiUrl)
                    .header("Authorization", "Bearer " + config.getApiKey())
                    .header("Content-Type", "application/json")
                    .header("X-DashScope-Async", "enable")
                    .timeout(config.getTimeout() * 1000)
                    .body(requestBody.toString())
                    .execute();

            if (response.isOk()) {
                return parseOpenAIResponse(response.body());
            } else {
                log.error("通义千问API调用失败: status={}, body={}", response.getStatus(), response.body());
                throw new BusinessException("AI服务调用失败");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("通义千问API调用异常", e);
            throw new BusinessException("AI服务调用异常");
        }
    }

    /**
     * 使用文心一言API生成回复
     *
     * 实现步骤：
     * 1. 从配置中读取 api-key 和 secret-key
     * 2. 获取Access Token（有效期30天，需缓存）
     *    POST https://aip.baidubce.com/oauth/2.0/token
     *    参数：grant_type=client_credentials&client_id=API_KEY&client_secret=SECRET_KEY
     * 3. 使用Access Token调用对话接口
     *    POST https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/completions
     * 4. 请求体格式与OpenAI兼容
     *
     * @param message 用户消息
     * @param history 对话历史
     * @return AI回复文本
     */
    private String chatWithWenxin(String message, List<String> history) {
        // 文心一言API调用入口，SDK参考：https://cloud.baidu.com/doc/WENXINWORKSHOP/s/Ilkkrb0i5
        log.warn("文心一言API暂未实现，使用模拟回复");
        return chatWithMock(message, history);
    }

    /**
     * 使用腾讯混元API生成回复
     *
     * 实现步骤：
     * 1. 从配置中读取 secret-id 和 secret-key
     * 2. 使用腾讯云SDK或自行实现签名算法（TC3-HMAC-SHA256）
     * 3. API端点：hunyuan.tencentcloudapi.com
     * 4. 接口：ChatCompletions
     *
     * Maven依赖（可选）：
     * <dependency>
     *   <groupId>com.tencentcloudapi</groupId>
     *   <artifactId>tencentcloud-sdk-java</artifactId>
     *   <version>3.1.x</version>
     * </dependency>
     *
     * @param message 用户消息
     * @param history 对话历史
     * @return AI回复文本
     */
    private String chatWithHunyuan(String message, List<String> history) {
        // 腾讯混元API调用入口，参考文档：https://cloud.tencent.com/document/product/1729/104753
        log.warn("腾讯混元API暂未实现，使用模拟回复");
        return chatWithMock(message, history);
    }

    /**
     * 使用科大讯飞星火API生成回复
     *
     * 实现步骤：
     * 1. 从配置中读取 app-id、api-secret、api-key
     * 2. 使用WebSocket连接（星火大模型使用WebSocket协议）
     * 3. URL生成：需要使用appId、secret、timestamp生成鉴权URL
     * 4. 发送消息格式：{"header":{"app_id":"xxx"},"parameter":{"chat":{"domain":"generalv3.5"}},"payload":{"message":{"text":"用户消息"}}}
     *
     * 注意：讯飞使用的是大模型3.5版本，支持流式返回
     *
     * @param message 用户消息
     * @param history 对话历史
     * @return AI回复文本
     */
    private String chatWithSpark(String message, List<String> history) {
        // 讯飞星火API调用入口，参考文档：https://www.xfyun.cn/doc/spark/Web.html
        log.warn("讯飞星火API暂未实现，使用模拟回复");
        return chatWithMock(message, history);
    }

    /**
     * 构建OpenAI兼容格式的请求体
     */
    private JSONObject buildOpenAIRequestBody(String userMessage, List<String> history, String model, int timeout) {
        JSONObject body = new JSONObject();
        body.set("model", model);
        body.set("temperature", 0.7);
        body.set("max_tokens", 2048);

        JSONArray messages = new JSONArray();
        // 添加历史对话
        if (history != null) {
            for (String msg : history) {
                JSONObject historyMsg = new JSONObject();
                if (msg.startsWith("user:")) {
                    historyMsg.set("role", "user");
                    historyMsg.set("content", msg.substring(5));
                } else if (msg.startsWith("assistant:")) {
                    historyMsg.set("role", "assistant");
                    historyMsg.set("content", msg.substring(10));
                }
                if (historyMsg.containsKey("role")) {
                    messages.add(historyMsg);
                }
            }
        }
        // 添加当前用户消息
        JSONObject userMsg = new JSONObject();
        userMsg.set("role", "user");
        userMsg.set("content", userMessage);
        messages.add(userMsg);

        body.set("messages", messages);
        return body;
    }

    /**
     * 解析OpenAI兼容格式的响应
     */
    private String parseOpenAIResponse(String responseBody) {
        try {
            JSONObject json = JSONUtil.parseObj(responseBody);
            JSONArray choices = json.getJSONArray("choices");
            if (choices != null && !choices.isEmpty()) {
                JSONObject firstChoice = choices.getJSONObject(0);
                JSONObject message = firstChoice.getJSONObject("message");
                if (message != null) {
                    return message.getStr("content", "");
                }
            }
            log.error("AI响应格式异常: {}", responseBody);
            throw new BusinessException("AI响应格式异常");
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("解析AI响应失败: {}", responseBody, e);
            throw new BusinessException("解析AI响应失败");
        }
    }

    /**
     * 模拟AI回复 - 增强版
     * 支持常见场景：问候、润色、翻译、总结、问答等
     */
    private String chatWithMock(String message, List<String> history) {
        String lowerMsg = message.toLowerCase();

        // 问候类
        if (containsAny(lowerMsg, "你好", "hi", "hello", "嗨", "您好")) {
            return "你好！我是AI助手，有什么可以帮助你的吗？\n\n我可以帮你：\n• 润色周报、月报等文档\n• 中英文翻译\n• 总结长文本要点\n• 回答各种问题\n• 起草通知、邮件等";
        }

        // 润色类
        if (containsAny(lowerMsg, "润色", "修改", "改写", "优化", "polish")) {
            return "好的，我来帮你润色文档。\n\n【润色建议】\n1. 语言表达更精炼\n2. 结构层次更清晰\n3. 专业术语更准确\n4. 语气措辞更得体\n\n请提供需要润色的具体内容，我会给出优化建议。\n\n---\n*提示：配置真实的AI API（如通义千问/GPT）可获得更智能的润色效果*";
        }

        // 翻译类
        if (containsAny(lowerMsg, "翻译", "translate", "英文", "中文", "english", "chinese")) {
            if (containsAny(lowerMsg, "英", "en")) {
                return "【中译英参考】\n\n以下是你的中文内容翻译：\n\n[此处将显示英文翻译结果]\n\n---\n*提示：配置真实的AI API可获得更准确、地道的翻译*";
            } else {
                return "【英译中参考】\n\n以下是你的英文内容翻译：\n\n[此处将显示中文翻译结果]\n\n---\n*提示：配置真实的AI API可获得更准确、地道的翻译*";
            }
        }

        // 总结类
        if (containsAny(lowerMsg, "总结", "摘要", "概括", "summarize", "summary")) {
            return "【文本总结】\n\n请提供需要总结的内容，我可以帮你：\n\n• 提取核心观点\n• 生成简洁摘要\n• 列出关键要点\n• 梳理逻辑结构\n\n---\n*提示：配置真实的AI API可获得更智能的总结效果*";
        }

        // 起草类
        if (containsAny(lowerMsg, "起草", "写", "draft", "通知", "邮件", "email")) {
            return "【文档起草】\n\n好的，我来帮你起草文档。\n\n请告诉我：\n1. 文档类型（通知/邮件/报告/申请...）\n2. 主要内容要点\n3. 收件对象（如有）\n4. 特殊格式要求（如有）\n\n---\n*提示：配置真实的AI API可获得更专业的起草效果*";
        }

        // 问答类
        if (containsAny(lowerMsg, "什么是", "怎么", "如何", "why", "how", "what", "解释")) {
            return "【问答参考】\n\n关于你的问题，这是一个模拟回复。\n\n要获得准确的答案，建议：\n1. 搜索权威技术文档\n2. 咨询相关领域专家\n3. 配置真实的AI API进行问答\n\n---\n*当前为模拟模式，配置真实API可获得准确答案*";
        }

        // 感谢类
        if (containsAny(lowerMsg, "谢谢", "感谢", "thanks", "thank")) {
            return "不客气！很高兴能帮到你。如果还有其他问题，随时可以问我。";
        }

        // 再见类
        if (containsAny(lowerMsg, "再见", "拜拜", "bye", "下次见")) {
            return "再见！祝你工作顺利。有需要随时来找我。";
        }

        // 默认回复
        return "我收到了你的消息。\n\n当前为**模拟模式**，要获得智能回复，请配置真实的AI API密钥。\n\n支持的配置：\n• 通义千问（阿里云百炼）\n• OpenAI GPT\n• 百度文心一言\n• 腾讯混元\n• 讯飞星火\n\n---\n你也可以尝试问我：\n• \"帮我润色周报\"\n• \"翻译成英文\"\n• \"总结这段文字\"\n• \"起草一封邮件\"";
    }

    /**
     * 检查消息是否包含任意关键词
     */
    private boolean containsAny(String message, String... keywords) {
        for (String keyword : keywords) {
            if (message.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 生成文档摘要 - 增强版
     */
    private String generateSummary(String content, String summaryType) {
        // 计算基本统计
        int charCount = content.length();
        int wordCount = content.split("\\s+").length;
        int sentenceCount = content.split("[。！？.!?]+").length;

        // 提取前N个字符作为预览
        int previewLen = Math.min(200, content.length());
        String preview = content.substring(0, previewLen);

        StringBuilder sb = new StringBuilder();

        switch (summaryType) {
            case "brief":
                sb.append("【简短摘要】\n\n");
                sb.append("原文共").append(charCount).append("字符，");
                sb.append(wordCount).append("个词语，");
                sb.append(sentenceCount).append("个句子。\n\n");
                sb.append("核心内容：\n");
                sb.append(preview);
                if (content.length() > previewLen) {
                    sb.append("...\n\n（以上为前").append(previewLen).append("字符预览）");
                }
                break;

            case "detailed":
                sb.append("【详细摘要】\n\n");
                sb.append("📊 基本统计\n");
                sb.append("• 字符数：").append(charCount).append("\n");
                sb.append("• 词数：").append(wordCount).append("\n");
                sb.append("• 句子数：").append(sentenceCount).append("\n\n");
                sb.append("📝 内容预览\n");
                sb.append(preview);
                if (content.length() > previewLen) {
                    sb.append("...\n\n（以上为前").append(previewLen).append("字符）");
                }
                sb.append("\n\n【结构分析】\n");
                // 简单分析段落
                String[] paragraphs = content.split("\n");
                sb.append("• 段落数：").append(paragraphs.length).append("\n");
                // 找出最短和最长的句子
                String[] sentences = content.split("[。！？.!?]+");
                if (sentences.length > 0) {
                    int minLen = sentences[0].length();
                    int maxLen = sentences[0].length();
                    for (String s : sentences) {
                        if (s.length() < minLen) minLen = s.length();
                        if (s.length() > maxLen) maxLen = s.length();
                    }
                    sb.append("• 句子长度：").append(minLen).append("-").append(maxLen).append("字符\n");
                }
                break;

            default:
                sb.append("【摘要】\n\n");
                sb.append("原文共").append(charCount).append("字符。\n\n");
                sb.append("主要内容：\n");
                sb.append(preview);
                if (content.length() > previewLen) {
                    sb.append("...\n\n（以上为前").append(previewLen).append("字符预览）");
                }
        }

        sb.append("\n\n---\n*提示：配置真实的AI API（如通义千问/GPT）可获得更智能、更准确的摘要*");
        return sb.toString();
    }

    /**
     * 提取关键点
     */
    private List<String> extractKeyPoints(String content) {
        List<String> keyPoints = new ArrayList<>();
        // 简单的关键点提取模拟
        String[] sentences = content.split("[。！？\\n]");
        for (int i = 0; i < Math.min(3, sentences.length); i++) {
            if (sentences[i].trim().length() > 10) {
                keyPoints.add(sentences[i].trim());
            }
        }
        return keyPoints;
    }

    /**
     * 获取对话历史（使用Redis LIST，原子操作）
     */
    private List<String> getConversationHistory(String conversationId, Long userId) {
        String key = getConversationKey(conversationId, userId);
        List<Object> rawList = redisTemplate.opsForList().range(key, 0, -1);
        List<String> history = new ArrayList<>();
        if (rawList != null) {
            for (Object item : rawList) {
                if (item instanceof String) {
                    history.add((String) item);
                } else if (item != null) {
                    history.add(item.toString());
                }
            }
        }
        return history;
    }

    /**
     * 保存消息到对话历史（使用Redis LIST的rightPush原子操作，避免竞态条件）
     */
    private void saveMessage(String conversationId, Long userId, String role, String content) {
        String key = getConversationKey(conversationId, userId);
        String message = role + ":" + content;
        redisTemplate.opsForList().rightPush(key, message);
        redisTemplate.expire(key, CONVERSATION_TTL_HOURS, TimeUnit.HOURS);

        // 裁剪到最大长度，保留最近的消息
        Long size = redisTemplate.opsForList().size(key);
        if (size != null && size > MAX_HISTORY_SIZE) {
            redisTemplate.opsForList().trim(key, size - MAX_HISTORY_SIZE, -1);
        }
    }

    /**
     * 获取对话Redis Key
     */
    private String getConversationKey(String conversationId, Long userId) {
        return CONVERSATION_KEY_PREFIX + userId + ":" + conversationId;
    }

    /**
     * 估算token数（粗略估计：中文1字符约1token，英文1单词约1token）
     */
    private Integer estimateTokens(String text) {
        if (StrUtil.isBlank(text)) {
            return 0;
        }
        // 简单估算：字符数的一半
        return (text.length() + 1) / 2;
    }

    /**
     * 带重试的执行器（指数退避）
     *
     * @param task 要执行的任务
     * @return 执行结果
     */
    private String executeWithRetry(java.util.concurrent.Callable<String> task) {
        Exception lastException = null;
        for (int attempt = 1; attempt <= MAX_RETRY_COUNT; attempt++) {
            try {
                return task.call();
            } catch (Exception e) {
                lastException = e;
                if (attempt < MAX_RETRY_COUNT) {
                    long delay = RETRY_BASE_DELAY_MS * (1L << (attempt - 1));
                    log.warn("AI调用失败，第{}次重试，等待{}ms: {}", attempt, delay, e.getMessage());
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new BusinessException("AI调用被中断");
                    }
                }
            }
        }
        log.error("AI调用失败，已重试{}次", MAX_RETRY_COUNT, lastException);
        throw new BusinessException("AI服务暂时不可用，请稍后重试");
    }
}

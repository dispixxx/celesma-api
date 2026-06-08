package com.disp.celesma.service;

import com.disp.celesma.service.interfaces.IAiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Сервис для взаимодействия с DeepSeek API.
 * Поддерживает несколько действий над описанием задачи:
 * <ul>
 *   <li>TITLE — генерация короткого названия</li>
 *   <li>IMPROVE — улучшение описания (орфография, стиль, конкретика)</li>
 *   <li>FORMALIZE — перевод в официально-деловой стиль</li>
 *   <li>SUBTASKS — разбиение на подзадачи</li>
 * </ul>
 *
 * DeepSeek API полностью совместим с форматом OpenAI Chat Completions.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiService implements IAiService {

    private final RestTemplate restTemplate;

    @Value("${ai.api.url}")
    private String apiUrl;

    @Value("${ai.api.key}")
    private String apiKey;

    // ──────────────────────────────────────────────
    // System prompts для разных действий
    // ──────────────────────────────────────────────

    private static final String PROMPT_TITLE =
            "Ты — ассистент проектного менеджера. " +
            "Сгенерируй короткое, ёмкое название задачи (не более 10 слов) " +
            "на русском языке на основе предоставленного описания. " +
            "Верни ТОЛЬКО название, без кавычек, пояснений и лишнего текста.";

    private static final String PROMPT_IMPROVE =
            "Ты — опытный технический писатель и проектный менеджер. " +
            "Улучши предоставленное описание задачи: исправь орфографические и грамматические ошибки, " +
            "сделай текст более конкретным и структурированным, добавь недостающие детали, " +
            "сохраняя исходный смысл. Верни ТОЛЬКО улучшенный текст описания, без пояснений.";

    private static final String PROMPT_FORMALIZE =
            "Ты — специалист по документированию в IT-компании. " +
            "Перепиши описание задачи в официально-деловом стиле, " +
            "используя профессиональную терминологию и чёткие формулировки. " +
            "Верни ТОЛЬКО переписанный текст, без комментариев.";

    private static final String PROMPT_SUBTASKS =
            "Ты — опытный Scrum-мастер. Разбей описание задачи на логические подзадачи. " +
            "Каждая подзадача должна быть конкретной, измеримой и независимой. " +
            "Верни ответ строго в формате нумерованного списка, по одной подзадаче на строку, " +
            "например:\n" +
            "1. Первая подзадача\n" +
            "2. Вторая подзадача\n" +
            "Никаких пояснений до или после списка.";

    private static final String FALLBACK_TITLE = "Новая задача";
    private static final String FALLBACK_DESCRIPTION = "Не удалось обработать описание. Попробуйте позже.";

    // ──────────────────────────────────────────────
    // Публичные методы
    // ──────────────────────────────────────────────

    @Override
    public String generateAiTitle(String description) {
        return processDescription(description, "TITLE");
    }

    @Override
    public String processDescription(String description, String action) {
        if (description == null || description.isBlank()) {
            log.warn("processDescription вызван с пустым описанием, action={}", action);
            return "TITLE".equalsIgnoreCase(action) ? FALLBACK_TITLE : FALLBACK_DESCRIPTION;
        }

        String systemPrompt = selectPrompt(action);
        int maxTokens = "TITLE".equalsIgnoreCase(action) ? 30 : 800;

        return callDeepSeek(systemPrompt, description, maxTokens,
                "TITLE".equalsIgnoreCase(action) ? FALLBACK_TITLE : FALLBACK_DESCRIPTION);
    }

    // ──────────────────────────────────────────────
    // Приватные методы
    // ──────────────────────────────────────────────

    private String selectPrompt(String action) {
        return switch (action.toUpperCase()) {
            case "TITLE"   -> PROMPT_TITLE;
            case "IMPROVE"  -> PROMPT_IMPROVE;
            case "FORMALIZE" -> PROMPT_FORMALIZE;
            case "SUBTASKS"  -> PROMPT_SUBTASKS;
            default -> {
                log.warn("Неизвестное действие '{}', использую IMPROVE по умолчанию", action);
                yield PROMPT_IMPROVE;
            }
        };
    }

    /**
     * Единый метод вызова DeepSeek Chat Completions API.
     */
    private String callDeepSeek(String systemPrompt, String userMessage,
                                int maxTokens, String fallback) {
        Map<String, Object> requestBody = Map.of(
                "model", "deepseek-chat",
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userMessage)
                ),
                "max_tokens", maxTokens,
                "temperature", 0.7
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl, HttpMethod.POST, entity, Map.class);

            return extractContent(response.getBody(), fallback);
        } catch (RestClientException e) {
            log.error("Ошибка при обращении к DeepSeek API: {}", e.getMessage(), e);
            return fallback;
        }
    }

    @SuppressWarnings("unchecked")
    private String extractContent(Map responseBody, String fallback) {
        if (responseBody == null) {
            log.error("DeepSeek API вернул пустое тело ответа");
            return fallback;
        }

        List<Map<String, Object>> choices =
                (List<Map<String, Object>>) responseBody.get("choices");

        if (choices != null && !choices.isEmpty()) {
            Map<String, Object> choice = choices.getFirst();
            Map<String, Object> message =
                    (Map<String, Object>) choice.get("message");
            if (message != null) {
                String content = (String) message.get("content");
                if (content != null) {
                    return content.trim()
                            .replaceAll("^[\"'«]|[\"'»]$", "")
                            .replaceAll("^Название:\\s*", "");
                }
            }
        }

        log.warn("DeepSeek API ответил, но choices пуст: {}", responseBody);
        return fallback;
    }
}
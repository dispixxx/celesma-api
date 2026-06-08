package com.disp.celesma.service.interfaces;

public interface IAiService {
    String generateAiTitle(String description);

    /**
     * Обрабатывает описание задачи через AI в зависимости от выбранного действия.
     *
     * @param description исходное описание
     * @param action      действие: TITLE, IMPROVE, FORMALIZE, SUBTASKS
     * @return результат обработки (название, улучшенное описание и т.д.)
     */
    String processDescription(String description, String action);
}

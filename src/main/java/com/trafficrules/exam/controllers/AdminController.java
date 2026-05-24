package com.trafficrules.exam.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Контроллер для отдачи HTML-страницы администрирования.
 * Сама страница загружает данные через REST API (AdminApiController).
 * Этот контроллер только показывает шаблон admin.html.
 */
@Controller
public class AdminController {

    /**
     * GET /admin
     * Отображает админ-панель.
     *
     * @param model модель для передачи атрибутов в шаблон
     * @return имя шаблона (admin.html)
     *
     * Логика:
     * - Добавляет атрибут "currentPage" для подсветки активного пункта меню (если пункт "Админ" появится в меню).
     * - Возвращает "admin" — Spring найдёт /templates/admin.html.
     * - Далее на стороне клиента JavaScript выполнит fetch('/api/admin/questions') для загрузки данных.
     */
    @GetMapping("/admin")
    public String adminPanel(Model model) {
        model.addAttribute("currentPage", "admin");
        return "admin";
    }
}
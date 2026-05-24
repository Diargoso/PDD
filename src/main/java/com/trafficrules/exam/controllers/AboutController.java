package com.trafficrules.exam.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Контроллер для отображения страницы "О проекте".
 * Отвечает только за выдачу HTML-страницы about.html.
 */
@Controller  // Объявляет класс как Spring MVC контроллер, который обрабатывает HTTP-запросы и возвращает представления (HTML).
public class AboutController {

    /**
     * Обрабатывает GET-запрос по пути "/about".
     *
     * @param model интерфейс Spring для передачи атрибутов в представление (Thymeleaf).
     * @return имя шаблона Thymeleaf (без расширения .html), который будет отображён.
     *
     * Логика:
     * 1. Добавляем в модель атрибут "currentPage" со значением "about".
     *    Это нужно для подсветки активного пункта меню в шапке (фрагмент header.html).
     * 2. Возвращаем "about" — Spring найдёт шаблон /templates/about.html и отдаст клиенту.
     */
    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("currentPage", "about");
        return "about";
    }
}
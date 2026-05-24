package com.trafficrules.exam.controllers;

import com.trafficrules.exam.services.ExamService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Контроллер для входа в систему: отображение формы входа и обработка введённого ключа.
 */
@Controller
public class LoginController {

    @Autowired private ExamService examService;

    /**
     * GET /login (и корневой путь /)
     * Отображает страницу входа с формой.
     *
     * @param model модель для передачи атрибутов (например, currentPage)
     * @return имя шаблона "login"
     */
    @GetMapping({"/", "/login"})
    public String login(Model model) {
        model.addAttribute("currentPage", "login");
        return "login";
    }

    /**
     * POST /login
     * Принимает ключ доступа из формы, проверяет его валидность и активность.
     * Если ключ корректен — сохраняет его и категорию в сессию и перенаправляет на /exam.
     * Если нет — возвращает страницу логина с сообщением об ошибке.
     *
     * @param key     значение ключа, переданное из формы (параметр "key")
     * @param model   модель для передачи сообщения об ошибке
     * @param session HTTP-сессия, куда сохраняем данные авторизованного пользователя
     * @return либо редирект на "/exam", либо снова шаблон "login" с ошибкой
     *
     * Логика:
     * 1. Вызываем examService.validateAndGetCategory(key) — ищет в БД активный ключ с таким значением и возвращает категорию.
     * 2. Если категория найдена:
     *    - сохраняем в сессии "accessKey" и "category".
     *    - перенаправляем на GET /exam.
     * 3. Если не найдена:
     *    - добавляем в модель атрибут error.
     *    - возвращаем представление "login" (та же страница, но с ошибкой).
     */
    @PostMapping("/login")
    public String processLogin(@RequestParam String key, Model model, HttpSession session) {
        String category = examService.validateAndGetCategory(key);
        if (category != null) {
            // Успешный вход: запоминаем ключ и категорию для последующих запросов
            session.setAttribute("accessKey", key);
            session.setAttribute("category", category);
            return "redirect:/exam";
        } else {
            // Ошибка: показываем форму заново с сообщением
            model.addAttribute("error", "Неверный или неактивный ключ доступа");
            model.addAttribute("currentPage", "login");
            return "login";
        }
    }
}
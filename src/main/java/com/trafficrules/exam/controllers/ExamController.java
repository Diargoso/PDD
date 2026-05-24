package com.trafficrules.exam.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trafficrules.exam.models.Question;
import com.trafficrules.exam.services.ExamService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Контроллер для процесса экзамена: отображение вопросов, приём ответов, подсчёт результата.
 * Работает с HTTP-сессией для хранения состояния экзамена (вопросы, ключ, категория).
 */
@Controller
public class ExamController {

    @Autowired private ExamService examService;      // Сервис с бизнес-логикой экзамена (выборка вопросов, подсчёт, сохранение)
    @Autowired private ObjectMapper objectMapper;    // Jackson ObjectMapper (здесь не используется явно, но может пригодиться для отладки)

    /**
     * GET /exam
     * Загружает страницу экзамена. Требует наличия в сессии атрибута "category" (устанавливается после входа).
     * Если категории нет — редирект на /login.
     *
     * @param model   модель для передачи атрибутов в шаблон exam.html
     * @param session текущая HTTP-сессия пользователя
     * @return имя шаблона "exam" или редирект "redirect:/login"
     *
     * Логика:
     * 1. Получаем из сессии категорию (A/B/C/D), сохранённую при логине.
     * 2. Запрашиваем у ExamService 20 случайных вопросов для данной категории.
     * 3. Если вопросов нет — показываем страницу ошибки.
     * 4. Сохраняем список вопросов в сессии (позже понадобится для подсчёта результатов).
     * 5. Передаём список вопросов в модель (Thymeleaf сможет его использовать на клиенте через JavaScript).
     * 6. Возвращаем шаблон exam.html.
     */
    @GetMapping("/exam")
    public String exam(Model model, HttpSession session) {
        String category = (String) session.getAttribute("category");
        if (category == null) return "redirect:/login";

        // Получаем до 20 вопросов (метод сервиса перемешивает и ограничивает)
        List<Question> questions = examService.getQuestionsByCategory(category, 20);
        if (questions.isEmpty()) {
            model.addAttribute("error", "Вопросов для данной категории пока нет");
            return "error";
        }

        // Сохраняем вопросы в сессии — они понадобятся при отправке ответов, чтобы сверить правильные варианты
        session.setAttribute("examQuestions", questions);

        // Передаём вопросы в модель. В шаблоне они будут доступны как переменная ${questions}
        model.addAttribute("questions", questions);
        model.addAttribute("currentPage", "exam");
        model.addAttribute("category", category);
        return "exam";
    }

    /**
     * POST /exam/submit
     * Принимает массив ответов пользователя в формате JSON, вычисляет результат, сохраняет в БД,
     * деактивирует использованный ключ доступа и возвращает JSON с результатом.
     *
     * @param dto     объект, содержащий массив answers (индексы выбранных вариантов, -1 если не отвечен)
     * @param session сессия, откуда достаём сохранённые вопросы и ключ доступа
     * @return JSON-строка вида {"score":15, "total":20, "passed":true} или {"error":"..."}
     *
     * Логика:
     * 1. Проверяем, есть ли в сессии ключ доступа (если нет — сессия истекла).
     * 2. Получаем из сессии список вопросов, которые были показаны пользователю.
     * 3. Вызываем examService.calculateScore() для подсчёта правильных ответов.
     * 4. Сохраняем результат в БД через examService.saveResult().
     * 5. Деактивируем ключ доступа (чтобы его нельзя было использовать повторно).
     * 6. Удаляем вопросы из сессии (освобождаем память).
     * 7. Формируем JSON с результатом и возвращаем.
     */
    @PostMapping("/exam/submit")
    @ResponseBody   // Указывает, что возвращаемое значение — это HTTP-ответ (тело), а не имя представления
    public String submitExam(@RequestBody UserAnswersDto dto, HttpSession session) {
        String accessKey = (String) session.getAttribute("accessKey");
        if (accessKey == null) {
            return "{\"error\":\"Сессия истекла. Войдите заново.\"}";
        }

        @SuppressWarnings("unchecked")
        List<Question> sessionQuestions = (List<Question>) session.getAttribute("examQuestions");
        if (sessionQuestions == null) {
            return "{\"error\":\"Вопросы не найдены. Начните экзамен заново.\"}";
        }

        int[] userAnswers = dto.getAnswers();
        int total = sessionQuestions.size();
        int score = examService.calculateScore(sessionQuestions, userAnswers);

        examService.saveResult(accessKey, score, total);       // Сохраняем результат
        examService.deactivateAccessKey(accessKey);           // Блокируем ключ
        session.removeAttribute("examQuestions");             // Очищаем сессию

        boolean passed = score >= total * 0.8;                // Проходной балл 80%
        return String.format("{\"score\":%d, \"total\":%d, \"passed\":%b}", score, total, passed);
    }
}
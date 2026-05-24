package com.trafficrules.exam.controllers;

import com.trafficrules.exam.models.AccessKey;
import com.trafficrules.exam.models.ExamResult;
import com.trafficrules.exam.models.Question;
import com.trafficrules.exam.repositories.AccessKeyRepository;
import com.trafficrules.exam.repositories.ExamResultRepository;
import com.trafficrules.exam.services.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST-контроллер для административных операций (CRUD вопросов, ключей, просмотр результатов).
 * Все методы возвращают данные в формате JSON (благодаря аннотации @RestController).
 * Базовый путь: /api/admin
 */
@RestController  // = @Controller + @ResponseBody на каждый метод. Возвращает JSON, а не HTML.
@RequestMapping("/api/admin")  // Все эндпоинты этого контроллера имеют префикс /api/admin
public class AdminApiController {

    // Внедрение зависимостей через поле (Field Injection). Spring автоматически предоставит готовые бины.
    @Autowired private QuestionService questionService;   // Сервис для работы с вопросами (CRUD + связь с Answer)
    @Autowired private AccessKeyRepository accessKeyRepository;   // JPA-репозиторий для доступа к таблице access_keys
    @Autowired private ExamResultRepository examResultRepository; // Репозиторий для результатов экзаменов

    // ======================== УПРАВЛЕНИЕ ВОПРОСАМИ ========================

    /**
     * GET /api/admin/questions
     * Возвращает список всех вопросов вместе с их вариантами ответов.
     * Используется на странице администрирования для отображения таблицы вопросов.
     *
     * @return List<Question> — автоматически сериализуется в JSON.
     */
    @GetMapping("/questions")
    public List<Question> getAllQuestions() {
        return questionService.findAll(); // Вызывает QuestionRepository.findAll(), который загружает вопросы со всеми ответами (FetchType.EAGER)
    }

    /**
     * POST /api/admin/questions
     * Создаёт новый вопрос. Данные приходят в теле запроса в формате JSON.
     * Ожидается, что переданный объект Question уже содержит список Answer (без id).
     *
     * @param question вопрос из тела запроса (Spring автоматически десериализует JSON в объект)
     * @return сохранённый вопрос с присвоенными id (включая id ответов)
     */
    @PostMapping("/questions")
    public Question createQuestion(@RequestBody Question question) {
        // В QuestionService.save() происходит привязка каждого ответа к вопросу (answer.setQuestion(question))
        return questionService.save(question);
    }

    /**
     * PUT /api/admin/questions/{id}
     * Обновляет существующий вопрос. id берётся из пути URL.
     *
     * @param id       идентификатор обновляемого вопроса
     * @param question новые данные вопроса (в формате JSON)
     * @return обновлённый вопрос
     */
    @PutMapping("/questions/{id}")
    public Question updateQuestion(@PathVariable Integer id, @RequestBody Question question) {
        question.setId(id);  // Устанавливаем id из пути в объект вопроса
        return questionService.save(question); // Сохраняем (при наличии id — обновление, иначе вставка)
    }

    /**
     * DELETE /api/admin/questions/{id}
     * Удаляет вопрос по id. Каскадно удаляются также связанные ответы (благодаря CascadeType.ALL).
     *
     * @param id идентификатор вопроса для удаления
     */
    @DeleteMapping("/questions/{id}")
    public void deleteQuestion(@PathVariable Integer id) {
        questionService.deleteById(id);
    }

    // ======================== УПРАВЛЕНИЕ КЛЮЧАМИ ДОСТУПА ========================

    /**
     * GET /api/admin/keys
     * Возвращает список всех ключей доступа (для отображения в админ-панели).
     */
    @GetMapping("/keys")
    public List<AccessKey> getAllKeys() {
        return accessKeyRepository.findAll();
    }

    /**
     * POST /api/admin/keys
     * Создаёт новый ключ доступа. Устанавливает текущую дату/время создания.
     *
     * @param key данные ключа (keyValue, description, category, active) из JSON
     * @return сохранённый ключ с присвоенным id
     */
    @PostMapping("/keys")
    public AccessKey createKey(@RequestBody AccessKey key) {
        key.setCreatedAt(LocalDateTime.now()); // Устанавливаем время создания вручную (можно было бы через @PrePersist, но так нагляднее)
        return accessKeyRepository.save(key);
    }

    /**
     * PUT /api/admin/keys/{id}
     * Обновляет существующий ключ. Сначала ищет ключ по id, если не найден — возвращает 404.
     *
     * @param id      идентификатор ключа
     * @param keyData новые данные ключа
     * @return ResponseEntity с обновлённым ключом (200 OK) или 404 Not Found
     */
    @PutMapping("/keys/{id}")
    public ResponseEntity<AccessKey> updateKey(@PathVariable Integer id, @RequestBody AccessKey keyData) {
        // Используем Optional для безопасного поиска
        return accessKeyRepository.findById(id).map(key -> {
            // Обновляем поля
            key.setKeyValue(keyData.getKeyValue());
            key.setDescription(keyData.getDescription());
            key.setCategory(keyData.getCategory());
            key.setActive(keyData.isActive());
            // Сохраняем и возвращаем в ResponseEntity с кодом 200
            return ResponseEntity.ok(accessKeyRepository.save(key));
        }).orElse(ResponseEntity.notFound().build()); // Если ключ не найден — 404
    }

    /**
     * DELETE /api/admin/keys/{id}
     * Удаляет ключ доступа.
     *
     * @param id идентификатор ключа
     * @return ResponseEntity с кодом 204 No Content (успешно удалено)
     */
    @DeleteMapping("/keys/{id}")
    public ResponseEntity<Void> deleteKey(@PathVariable Integer id) {
        accessKeyRepository.deleteById(id);
        return ResponseEntity.noContent().build(); // 204 No Content — стандарт для удаления
    }

    // ======================== ПРОСМОТР РЕЗУЛЬТАТОВ ЭКЗАМЕНОВ ========================

    /**
     * GET /api/admin/results
     * Возвращает список всех результатов экзаменов с подгруженными ключами доступа (JOIN FETCH).
     * Используется в модальном окне "Результаты" на странице admin.html.
     *
     * @return List<ExamResult> — каждый результат содержит поле accessKey (категория, значение ключа и т.д.)
     */
    @GetMapping("/results")
    public List<ExamResult> getAllResults() {
        // Используем кастомный метод репозитория с JOIN FETCH для избежания проблемы N+1 запроса
        return examResultRepository.findAllWithKeys();
    }

    /**
     * DELETE /api/admin/results/{id}
     * Удаляет запись о результате экзамена.
     *
     * @param id идентификатор результата
     * @return 204 No Content
     */
    @DeleteMapping("/results/{id}")
    public ResponseEntity<Void> deleteResult(@PathVariable Integer id) {
        examResultRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
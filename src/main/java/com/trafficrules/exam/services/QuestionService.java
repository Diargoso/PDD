package com.trafficrules.exam.services;

import com.trafficrules.exam.models.Answer;
import com.trafficrules.exam.models.Question;
import com.trafficrules.exam.repositories.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * Сервис, предоставляющий CRUD-операции для сущности Question (вопрос).
 * Используется в AdminApiController для управления вопросами через REST API.
 * Дополнительно выполняет привязку каждого ответа (Answer) к родительскому вопросу перед сохранением.
 */
@Service
public class QuestionService {

    @Autowired private QuestionRepository questionRepository;

    /**
     * Возвращает список всех вопросов, содержащихся в базе данных.
     * Вопросы загружаются со всеми связанными ответами (благодаря FetchType.EAGER в модели Question).
     *
     * @return список всех вопросов (может быть пустым)
     */
    public List<Question> findAll() {
        return questionRepository.findAll();
    }

    /**
     * Находит вопрос по его идентификатору.
     * Если вопрос с таким id не существует, возвращает null.
     *
     * @param id идентификатор вопроса
     * @return объект Question или null
     */
    public Question findById(Integer id) {
        return questionRepository.findById(id).orElse(null);
    }

    /**
     * Сохраняет вопрос (создаёт новый или обновляет существующий).
     * Перед сохранением устанавливает связь «ответ → вопрос» для каждого элемента списка answers:
     * каждый ответ получает ссылку на родительский вопрос (ans.setQuestion(question)).
     * Это необходимо, потому что при десериализации JSON в объект Question поле question у объектов Answer остаётся null,
     * а база данных требует внешний ключ question_id.
     *
     * @param question сохраняемый вопрос (может быть новым или уже существующим)
     * @return сохранённый вопрос с присвоенными идентификаторами (включая id ответов)
     */
    public Question save(Question question) {
        // Если список ответов присутствует и не null, пройти по каждому ответу и привязать вопрос
        if (question.getAnswers() != null) {
            for (Answer ans : question.getAnswers()) {
                ans.setQuestion(question);
            }
        }
        // Репозиторий выполняет INSERT или UPDATE в зависимости от наличия id
        return questionRepository.save(question);
    }

    /**
     * Удаляет вопрос по его идентификатору.
     * Благодаря настройкам каскада (CascadeType.ALL, orphanRemoval = true) все связанные ответы также удаляются.
     *
     * @param id идентификатор удаляемого вопроса
     */
    public void deleteById(Integer id) {
        questionRepository.deleteById(id);
    }
}
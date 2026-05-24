package com.trafficrules.exam.services;

import com.trafficrules.exam.models.*;
import com.trafficrules.exam.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

/**
 * Сервис, содержащий бизнес-логику процесса экзамена.
 * Класс отвечает за:
 * - проверку ключа доступа и получение категории;
 * - выборку случайных вопросов по категории;
 * - подсчёт правильных ответов;
 * - сохранение результата экзамена;
 * - деактивацию использованного ключа.
 */
@Service   // Аннотация объявляет класс компонентом Spring, содержащим бизнес-логику.
// Spring создаст единственный экземпляр (singleton) и внедрит его в контроллеры.
public class ExamService {

    // Внедрение репозиториев через поля. Spring автоматически предоставит готовые реализации.
    @Autowired private AccessKeyRepository accessKeyRepository;
    @Autowired private QuestionRepository questionRepository;
    @Autowired private ExamResultRepository examResultRepository;

    /**
     * Проверяет, существует ли переданный ключ доступа и активен ли он.
     * Если ключ найден и активен, возвращается категория этого ключа.
     * Если ключ не найден или неактивен, возвращается null.
     *
     * @param keyValue строковое значение ключа (вводится пользователем на странице логина)
     * @return категория (A, B, C или D) либо null
     */
    public String validateAndGetCategory(String keyValue) {
        // Репозиторий выполняет запрос: SELECT ... WHERE key_value = ? AND is_active = true
        Optional<AccessKey> opt = accessKeyRepository.findByKeyValueAndActiveTrue(keyValue);
        // Если Optional содержит значение, вернуть его категорию, иначе null
        return opt.map(AccessKey::getCategory).orElse(null);
    }

    /**
     * Возвращает список вопросов для указанной категории, перемешанный в случайном порядке,
     * и ограниченный указанным количеством.
     *
     * @param category категория (A, B, C, D)
     * @param limit максимальное количество вопросов, которое требуется вернуть.
     *              Если limit <= 0, возвращаются все вопросы категории.
     * @return список вопросов (может быть пустым, если в категории нет вопросов)
     */
    public List<Question> getQuestionsByCategory(String category, int limit) {
        // Получить все вопросы данной категории из репозитория (с ответами, благодаря FetchType.EAGER)
        List<Question> all = questionRepository.findByCategory(category);
        // Перемешать список, чтобы каждый экзамен был уникальным
        Collections.shuffle(all);
        // Если задан лимит и вопросов больше лимита, взять первые limit элементов
        if (limit > 0 && all.size() > limit) {
            return all.subList(0, limit);
        }
        return all;
    }

    /**
     * Сохраняет результат экзамена в базу данных.
     * Сначала находит активный ключ по его значению. Если ключ найден, создаётся новая запись ExamResult,
     * связывается с этим ключом, заполняется количество правильных ответов и общее число вопросов,
     * после чего запись сохраняется через репозиторий.
     * Если ключ не найден (например, стал неактивен между началом экзамена и его завершением), метод ничего не делает.
     *
     * @param keyValue значение ключа доступа, использованного для экзамена
     * @param score    количество правильных ответов, данных пользователем
     * @param total    общее количество вопросов в экзамене
     */
    public void saveResult(String keyValue, int score, int total) {
        Optional<AccessKey> opt = accessKeyRepository.findByKeyValueAndActiveTrue(keyValue);
        opt.ifPresent(accessKey -> {
            ExamResult result = new ExamResult();
            result.setAccessKey(accessKey);
            result.setScore(score);
            result.setTotalQuestions(total);
            // examDate заполняется автоматически в конструкторе (LocalDateTime.now())
            examResultRepository.save(result);
        });
    }

    /**
     * Вычисляет количество правильных ответов на основе сохранённых в сессии вопросов
     * и массива выбранных пользователем индексов ответов.
     *
     * Алгоритм:
     * - Для каждого вопроса (по индексу i) получить выбранный пользователем индекс ответа userAnswers[i].
     * - Если выбранный индекс находится в допустимых пределах (не -1, не выходит за размер списка),
     *   и если ответ с этим индексом помечен как правильный (isCorrect = true),
     *   то увеличить счётчик правильных ответов.
     *
     * @param sessionQuestions список вопросов, которые были показаны пользователю во время экзамена
     * @param userAnswers      массив целых чисел, где каждый элемент — индекс ответа, выбранного пользователем.
     *                         Если пользователь не ответил на вопрос, передаётся -1.
     * @return количество правильных ответов
     */
    public int calculateScore(List<Question> sessionQuestions, int[] userAnswers) {
        int correct = 0;
        for (int i = 0; i < sessionQuestions.size(); i++) {
            Question q = sessionQuestions.get(i);
            int selectedIdx = userAnswers[i];
            // Проверка, что индекс корректен (не -1 и не выходит за пределы списка ответов)
            if (selectedIdx >= 0 && selectedIdx < q.getAnswers().size()) {
                if (q.getAnswers().get(selectedIdx).isCorrect()) {
                    correct++;
                }
            }
        }
        return correct;
    }

    /**
     * Деактивирует ключ доступа, чтобы его нельзя было использовать повторно.
     * Поиск ключа выполняется по значению и флагу активности.
     * Если ключ найден, его поле active устанавливается в false, и обновлённый ключ сохраняется в репозитории.
     *
     * @param keyValue значение ключа, который требуется деактивировать
     */
    public void deactivateAccessKey(String keyValue) {
        accessKeyRepository.findByKeyValueAndActiveTrue(keyValue).ifPresent(ak -> {
            ak.setActive(false);
            accessKeyRepository.save(ak);
        });
    }
}
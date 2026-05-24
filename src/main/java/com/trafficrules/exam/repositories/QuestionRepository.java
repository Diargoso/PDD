package com.trafficrules.exam.repositories;

import com.trafficrules.exam.models.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Репозиторий для работы с сущностью Question (вопросы).
 *
 * Помимо стандартных методов, определён один поиск по категории.
 */
public interface QuestionRepository extends JpaRepository<Question, Integer> {

    /**
     * Находит все вопросы, относящиеся к указанной категории (A, B, C, D).
     *
     * Имя метода: findByCategory → Spring автоматически строит запрос:
     * SELECT q FROM Question q WHERE q.category = :category
     *
     * @param category категория (один символ: A/B/C/D)
     * @return список вопросов (может быть пустым)
     */
    List<Question> findByCategory(String category);
}

package com.trafficrules.exam.repositories;

import com.trafficrules.exam.models.ExamResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

/**
 * Репозиторий для работы с сущностью ExamResult (результаты экзаменов).
 *
 * Основные операции наследуются от JpaRepository.
 * Добавлен один кастомный метод для получения всех результатов вместе с ключами доступа.
 */
public interface ExamResultRepository extends JpaRepository<ExamResult, Integer> {

    /**
     * Возвращает список всех результатов экзаменов, одновременно загружая связанные объекты AccessKey.
     * Используется в админ-панели для отображения таблицы результатов с категориями и ключами.
     *
     * Зачем нужен JOIN FETCH?
     * - По умолчанию при получении ExamResult поле accessKey загружается лениво (LAZY).
     * - При попытке обратиться к accessKey.getCategory() вне транзакции или внутри цикла может быть N+1 запрос.
     * - JOIN FETCH заставляет Hibernate подгрузить AccessKey одним запросом через SQL JOIN.
     *
     * @return список ExamResult с инициализированным полем accessKey
     */
    @Query("SELECT er FROM ExamResult er JOIN FETCH er.accessKey")
    List<ExamResult> findAllWithKeys();
}
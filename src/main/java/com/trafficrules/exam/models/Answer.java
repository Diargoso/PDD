package com.trafficrules.exam.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

/**
 * Сущность "Вариант ответа".
 * Хранит один из вариантов ответа для конкретного вопроса.
 * Связана с Question отношением Many-to-One.
 * При сериализации в JSON поле "question" игнорируется, чтобы избежать циклической ссылки (Question -> Answer -> Question).
 */
@Entity
@Table(name = "answers")
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * Связь "многие ответы к одному вопросу".
     * @ManyToOne — на стороне "много".
     * @JoinColumn указывает внешний ключ: в таблице answers будет столбец question_id.
     * nullable=false — каждый ответ обязательно принадлежит какому-то вопросу.
     *
     * @JsonIgnore — при преобразовании в JSON не включать объект Question внутрь Answer.
     * Это предотвращает бесконечную рекурсию: Question -> List<Answer> -> Question -> ...
     */
    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    @JsonIgnore
    private Question question;

    /**
     * Текст варианта ответа (например, "Разрешается движение со скоростью не более 60 км/ч").
     * nullable=false — обязательное поле.
     */
    @Column(nullable = false)
    private String text;

    /**
     * Флаг правильности: true — этот ответ является верным для данного вопроса.
     * Вопрос может иметь только один правильный ответ (идеально), но технически допустимо несколько.
     * В данной системе на один вопрос ожидается ровно один правильный ответ.
     */
    @Column(name = "is_correct", nullable = false)
    private boolean correct;

    /**
     * Порядок отображения ответов при показе вопроса (0, 1, 2, ...).
     * Используется, чтобы ответы выводились не в случайном порядке, а в заданном.
     * По умолчанию 0, можно не заполнять.
     */
    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    // Конструктор по умолчанию
    public Answer() {}

    // Геттеры и сеттеры
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Question getQuestion() { return question; }
    public void setQuestion(Question question) { this.question = question; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public boolean isCorrect() { return correct; }
    public void setCorrect(boolean correct) { this.correct = correct; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
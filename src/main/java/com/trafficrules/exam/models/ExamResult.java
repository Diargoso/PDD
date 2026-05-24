package com.trafficrules.exam.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Сущность "Результат экзамена".
 * Сохраняет итоги каждого прохождения экзамена учеником.
 * Связана с AccessKey (многие результаты к одному ключу? Нет: один ключ может использоваться только один раз, поэтому связь ManyToOne,
 * но фактически один ключ может иметь не более одного результата. Однако оставлено ManyToOne для гипотетической возможности повторного использования).
 * В системе после успешного сохранения результата ключ деактивируется, поэтому дальнейшая сдача по тому же ключу невозможна.
 */
@Entity
@Table(name = "exam_results")
public class ExamResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * Связь с ключом доступа.
     * ManyToOne: у одного ключа может быть много результатов (но по логике — один).
     * Внешний ключ: access_key_id.
     * nullable=false — результат обязательно привязан к какому-то ключу.
     */
    @ManyToOne
    @JoinColumn(name = "access_key_id", nullable = false)
    private AccessKey accessKey;

    /**
     * Количество правильных ответов, данных учеником.
     * Например, 16 из 20.
     */
    private Integer score;

    /**
     * Общее количество вопросов в экзамене (обычно 20).
     * Сохраняется, так как в будущем количество вопросов может меняться.
     */
    @Column(name = "total_questions")
    private Integer totalQuestions;

    /**
     * Дата и время сдачи экзамена.
     * Автоматически устанавливается в момент создания объекта (LocalDateTime.now()).
     */
    @Column(name = "exam_date")
    private LocalDateTime examDate = LocalDateTime.now();

    /**
     * Вычисляемое поле — сдал ли экзамен (проходной балл 80%).
     * Аннотация @Transient означает, что это поле не сохраняется в базу данных.
     * При чтении из БД оно вычисляется на основе score и totalQuestions.
     *
     * @return true, если процент правильных ответов >= 80.
     */
    @Transient
    public boolean isPassed() {
        // Защита от деления на ноль: если totalQuestions=0, то считаем false
        if (totalQuestions == null || totalQuestions == 0) return false;
        return score >= totalQuestions * 0.8;
    }

    // Геттеры и сеттеры
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public AccessKey getAccessKey() { return accessKey; }
    public void setAccessKey(AccessKey accessKey) { this.accessKey = accessKey; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public Integer getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(Integer totalQuestions) { this.totalQuestions = totalQuestions; }

    public LocalDateTime getExamDate() { return examDate; }
    public void setExamDate(LocalDateTime examDate) { this.examDate = examDate; }
}
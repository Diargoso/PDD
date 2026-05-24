package com.trafficrules.exam.models;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Сущность "Вопрос".
 * Содержит текст вопроса, категорию (A/B/C/D), ссылку на изображение и список вариантов ответов.
 * Отношение с Answer: один вопрос — много ответов (OneToMany), каскадное удаление.
 * При загрузке вопроса ответы загружаются сразу (FetchType.EAGER), чтобы избежать дополнительных запросов.
 */
@Entity
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * Текст вопроса (может быть длинным, поэтому columnDefinition = "TEXT").
     * nullable=false — текст обязателен.
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    /**
     * Категория, к которой относится вопрос: A, B, C, D.
     * nullable=false.
     */
    @Column(nullable = false, length = 1)
    private String category;

    /**
     * URL изображения-иллюстрации к вопросу (необязательный).
     * Может быть относительным (например, /images/znak.png) или абсолютным.
     */
    @Column(name = "image_url")
    private String imageUrl;

    /**
     * Список вариантов ответов на этот вопрос.
     * CascadeType.ALL — все операции (сохранение, удаление) каскадируются на ответы.
     * orphanRemoval=true — если удалить ответ из списка, он также удалится из БД.
     * FetchType.EAGER — при загрузке вопроса сразу подгружаем все ответы (удобно для отображения).
     * mappedBy = "question" — указывает, что владельцем связи является поле "question" в классе Answer.
     */
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Answer> answers = new ArrayList<>();

    // Конструктор по умолчанию
    public Question() {}

    // Геттеры и сеттеры
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public List<Answer> getAnswers() { return answers; }
    public void setAnswers(List<Answer> answers) { this.answers = answers; }
}
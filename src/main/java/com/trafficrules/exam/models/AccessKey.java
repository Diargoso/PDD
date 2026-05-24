package com.trafficrules.exam.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Сущность "Ключ доступа".
 * Хранит одноразовые ключи, которые выдаются ученикам для прохождения экзамена.
 * Каждый ключ привязан к определённой категории (A, B, C, D) и может быть активным или неактивным.
 * После использования (успешной сдачи экзамена) ключ деактивируется.
 */
@Entity                     // Указывает, что этот класс является JPA-сущностью (будет отображён на таблицу в БД)
@Table(name = "access_keys") // Явно задаём имя таблицы в базе данных
public class AccessKey {

    /**
     * Первичный ключ, автоинкремент (генерируется БД).
     * Тип GenerationType.IDENTITY означает, что БД сама увеличивает значение (подходит для MySQL, PostgreSQL).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * Уникальное значение ключа (строка, которую вводит пользователь).
     * nullable=false — не может быть null.
     * unique=true — значение должно быть уникальным в таблице.
     * length=50 — ограничиваем длину строки (для оптимизации индекса).
     */
    @Column(name = "key_value", nullable = false, unique = true, length = 50)
    private String keyValue;

    /**
     * Описание ключа (необязательное поле). Может содержать комментарий, кому выдан ключ.
     * По умолчанию @Column без параметров использует имя поля как имя столбца.
     */
    private String description;

    /**
     * Категория транспортного средства: A, B, C, D.
     * nullable=false — обязательное поле.
     * length=1 — один символ.
     */
    @Column(nullable = false, length = 1)
    private String category;

    /**
     * Флаг активности ключа.
     * active = true — ключ можно использовать для входа.
     * active = false — ключ уже был использован или заблокирован администратором.
     * По умолчанию true (новый ключ активен).
     * Имя столбца в БД: is_active.
     */
    @Column(name = "is_active")
    private boolean active = true;

    /**
     * Дата и время создания ключа.
     * Устанавливается программно при создании (в AdminApiController).
     * Можно было бы использовать @PrePersist, но для наглядности установка в контроллере.
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // Конструктор по умолчанию (обязателен для JPA)
    public AccessKey() {}

    // Геттеры и сеттеры — обеспечивают доступ к полям (JPA использует их для чтения/записи)
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getKeyValue() { return keyValue; }
    public void setKeyValue(String keyValue) { this.keyValue = keyValue; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
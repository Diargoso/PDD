package com.trafficrules.exam.repositories;

import com.trafficrules.exam.models.AccessKey;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Репозиторий для работы с сущностью AccessKey (ключи доступа).
 * Spring Data JPA автоматически создаёт реализацию этого интерфейса и внедряет её (например, в сервисы).
 *
 * Наследуемые методы (без явной реализации):
 * - save(), findAll(), findById(), deleteById(), existsById() и др.
 *
 * я добавил один пользовательский метод для поиска активного ключа по его строковому значению.
 */
public interface AccessKeyRepository extends JpaRepository<AccessKey, Integer> {

    /**
     * Находит ключ доступа по значению (keyValue) с условием, что ключ активен (active = true).
     * Возвращает Optional, чтобы избежать NullPointerException.
     *
     * Как это работает: Spring Data JPA анализирует имя метода:
     * - findByKeyValueAndActiveTrue → ищет по полю keyValue и полю active (где active == true)
     *
     * SQL-запрос: SELECT * FROM access_keys WHERE key_value = ? AND is_active = true
     *
     * @param keyValue значение ключа (например, "ABC123")
     * @return Optional, содержащий AccessKey, если найден, иначе пустой Optional
     */
    Optional<AccessKey> findByKeyValueAndActiveTrue(String keyValue);
}
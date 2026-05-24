package com.trafficrules.exam.controllers;

/**
 * DTO (Data Transfer Object) для приёма массива ответов от клиента.
 * Используется в методе submitExam контроллера ExamController.
 */
public class UserAnswersDto {
    private int[] answers;  // Массив индексов выбранных ответов (длина = количество вопросов)

    public int[] getAnswers() {
        return answers;
    }
}
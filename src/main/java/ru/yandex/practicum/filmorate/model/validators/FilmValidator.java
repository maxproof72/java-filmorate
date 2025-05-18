package ru.yandex.practicum.filmorate.model.validators;

import ru.yandex.practicum.filmorate.exceptions.ValidationException;

import java.time.LocalDate;

public class FilmValidator {

    // Максимальная длина описания фильма
    public static final int MAX_DESCRIPTION_LENGTH = 200;

    // Минимальная дата релиза фильма
    public static final LocalDate MOVIE_BIRTHDAY = LocalDate.of(1895, 12, 28);


    private FilmValidator() {
    }

    /**
     * Проверка даты релиза
     * @param releaseDate Дата релиза
     * @throws ValidationException Дата релиза не указана или некорректна
     * @apiNote Дата релиза — не раньше 28 декабря 1895 года;
     */
    public static void checkReleaseDate(LocalDate releaseDate) {
        if (releaseDate == null) {
            throw new ValidationException("Не указана дата релиза");
        }
        if (releaseDate.isBefore(MOVIE_BIRTHDAY)) {
            throw new ValidationException("Некорректная дата релиза: " + releaseDate);
        }
    }

    /**
     * Проверка описания фильма
     * @param description Описание
     * @throws ValidationException если длина описания превышает заданное число символов
     */
    public static void checkDescription(String description) {

        if (description != null && description.length() > MAX_DESCRIPTION_LENGTH) {
            throw new ValidationException("Слишком длинное описание (не более " +
                    MAX_DESCRIPTION_LENGTH + " символов)");
        }
    }

    /**
     * Проверка длительности фильма
     * @param duration Длительность фильма в минутах
     * @throws ValidationException если задана нулевая или отрицательная длительность
     */
    public static void checkDuration(Integer duration) {
        if (duration == null) {
            throw new ValidationException("Не указана длительность фильма");
        }
        if (duration <= 0) {
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
    }
}

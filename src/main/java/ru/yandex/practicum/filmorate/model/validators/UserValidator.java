package ru.yandex.practicum.filmorate.model.validators;

import ru.yandex.practicum.filmorate.exceptions.ValidationException;

import java.time.LocalDate;
import java.util.regex.Pattern;

public class UserValidator {

    private static final Pattern emailPattern =
            Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    private UserValidator() {
    }

    /**
     * Проверка адреса электронной почты
     * @param email Адрес электронной почты
     * @throws ValidationException если адрес пустой или некорректный
     */
    public static void checkEmail(String email) {

        if (email == null) {
            throw new ValidationException("Электронная почта не может быть пустой");
        }
        if (!emailPattern.matcher(email).matches()) {
            throw new ValidationException("Некорректный адрес электронной почты: " + email);
        }
    }

    /**
     * Проверка логина
     * @param login Логин
     * @throws ValidationException если логин пустой или содержит пробельные символы
     */
    public static void checkLogin(String login) {

        if (login == null || login.isBlank()) {
            throw new ValidationException("Логин не может быть пустым");
        }
        if (login.matches(".*\\W.*")) {
            throw new ValidationException("Логин не может содержать пробелы");
        }
    }

    /**
     * Проверка даты рождения
     * @param birthday Дата рождения
     * @throws ValidationException если дата рождения будет только в будущем
     */
    public static void checkBirthday(LocalDate birthday) {

        if (birthday == null) {
            throw new ValidationException("Не указана дата рождения");
        }
        if (birthday.isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }
}

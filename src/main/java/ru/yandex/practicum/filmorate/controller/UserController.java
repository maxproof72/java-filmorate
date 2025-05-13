package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.regex.Pattern;


/**
 * Контроллер списка пользователей
 */
@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    private static final Pattern emailPattern =
            Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    private final Map<Integer, User> users = new HashMap<>();


    /**
     * Возвращает новый id пользователя
     * @return Новый id пользователя
     */
    private int getFreshId() {
        return users.keySet().stream()
                .max(Integer::compareTo)
                .orElse(0) + 1;
    }


    // region Checkers

    /**
     * Проверка адреса электронной почты
     * @param email Адрес электронной почты
     * @throws ValidationException если адрес пустой или некорректный
     */
    private void checkEmail(String email) {

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
    private void checkLogin(String login) {

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
    private void checkBirthday(Date birthday) {

        if (birthday == null) {
            throw new ValidationException("Не указана дата рождения");
        }
        if (birthday.after(new Date())) {
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }

    // endregion


    // region Mapping

    @GetMapping
    public Collection<User> getUsers() {
        return users.values();
    }

    @PostMapping
    public User createUser(@RequestBody User user) {

        try {
            // Проверка адреса электронной почты
            checkEmail(user.getEmail());

            // Проверка логина
            checkLogin(user.getLogin());

            // Проверка даты рождения
            checkBirthday(user.getBirthday());

            // Создание нового пользователя
            user.setId(getFreshId());
            if (user.getName() == null || user.getName().isBlank()) {
                user.setName(user.getLogin());
            }
            users.put(user.getId(), user);
            log.info("Добавлен пользователь {}", user);
            return user;

        } catch (ValidationException ve) {
            log.warn(ve.getMessage());
            throw ve;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw e;
        }
    }

    @PutMapping
    public User updateUser(@RequestBody User user) {

        try {
            if (user.getId() == null) {
                throw new ValidationException("Не указан id пользователя");
            }

            User existingUser = users.get(user.getId());
            if (existingUser == null) {
                throw new ValidationException("Пользователь с id = " + user.getId() + " не найден");
            }

            // Вначале проверяем все измененные поля, а потом уже их обновляем,
            // чтобы не получить частично обновленный объект
            String newEmail = user.getEmail();
            if (newEmail != null && !Objects.equals(existingUser.getLogin(), newEmail)) {
                checkEmail(user.getEmail());
            } else {
                newEmail = existingUser.getEmail();
            }

            // Проверка нового логина (если задан)
            String newLogin = user.getLogin();
            if (newLogin != null && !Objects.equals(existingUser.getLogin(), newLogin)) {
                checkLogin(user.getLogin());
            } else {
                newLogin = existingUser.getLogin();
            }

            // Проверка новой даты рождения (если задана)
            Date newBirthday = user.getBirthday();
            if (newBirthday != null && !Objects.equals(existingUser.getBirthday(), newBirthday)) {
                checkBirthday(user.getBirthday());
            } else {
                newBirthday = existingUser.getBirthday();
            }

            // Все проверки пройдены - можно обновляться
            existingUser.setEmail(newEmail);
            existingUser.setLogin(newLogin);
            if (user.getName() != null) {
                existingUser.setName(user.getName());
            }
            existingUser.setBirthday(newBirthday);
            log.info("Информация пользователя с id = {} обновлена", user.getId());
            return existingUser;

        } catch (ValidationException ve) {
            log.warn(ve.getMessage());
            throw ve;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw e;
        }
    }

    // endregion
}

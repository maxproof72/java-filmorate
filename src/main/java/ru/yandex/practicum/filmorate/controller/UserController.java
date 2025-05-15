package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.validators.UserValidator;

import java.time.LocalDate;
import java.util.*;


/**
 * Контроллер списка пользователей
 */
@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    private final Map<Long, User> users = new HashMap<>();

    private long id;


    @GetMapping
    public Collection<User> getUsers() {
        return users.values();
    }

    @PostMapping
    public User createUser(@RequestBody User user) {

        try {
            // Проверка адреса электронной почты
            UserValidator.checkEmail(user.getEmail());

            // Проверка логина
            UserValidator.checkLogin(user.getLogin());

            // Проверка даты рождения
            UserValidator.checkBirthday(user.getBirthday());

            // Создание нового пользователя
            user.setId(++id);
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
                UserValidator.checkEmail(user.getEmail());
            } else {
                newEmail = existingUser.getEmail();
            }

            // Проверка нового логина (если задан)
            String newLogin = user.getLogin();
            if (newLogin != null && !Objects.equals(existingUser.getLogin(), newLogin)) {
                UserValidator.checkLogin(user.getLogin());
            } else {
                newLogin = existingUser.getLogin();
            }

            // Проверка новой даты рождения (если задана)
            LocalDate newBirthday = user.getBirthday();
            if (newBirthday != null && !Objects.equals(existingUser.getBirthday(), newBirthday)) {
                UserValidator.checkBirthday(user.getBirthday());
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
}

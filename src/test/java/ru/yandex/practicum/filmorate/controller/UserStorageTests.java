package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;

public class UserStorageTests {

    private UserStorage userStorage;
    private static final LocalDate DATE_IN_FUTURE = LocalDate.now().plusDays(1);


    // region Helpers

    private void restoreValidUser(User user, String login) {

        user.setName("John Dow");
        user.setLogin(login);
        user.setEmail("john_dow.12345@fakemail.jp");
        user.setBirthday(LocalDate.now());
    }

    private void restoreValidUser(User user) {
        restoreValidUser(user, "login");
    }

    private User makeValidUser(String login) {

        User user = new User();
        restoreValidUser(user, login);
        return user;
    }

    // endregion


    @BeforeEach
    public void setUp() {
        userStorage = new InMemoryUserStorage();
    }

    @Test
    public void testCreateUser() {

        User user = makeValidUser("john_dow");

        // Проверка добавления валидной учетной записи
        User result = userStorage.addUser(user);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.getId());
        user.setId(result.getId());
        Assertions.assertEquals(user, result);
        Assertions.assertEquals(1, userStorage.getUsers().size());

        // Проверка адреса электронной почты
        user.setEmail(null);
        Assertions.assertThrows(ValidationException.class, () -> userStorage.addUser(user));
        user.setEmail("john_dow.12345_at_fakemail.jp");
        Assertions.assertThrows(ValidationException.class, () -> userStorage.addUser(user));
        user.setEmail("@john_dow.12345_at_fakemail.jp");
        Assertions.assertThrows(ValidationException.class, () -> userStorage.addUser(user));
        user.setEmail("john_dow.12345 @ fakemail.jp");
        Assertions.assertThrows(ValidationException.class, () -> userStorage.addUser(user));
        restoreValidUser(user);

        // Проверка логина
        user.setLogin(null);
        Assertions.assertThrows(ValidationException.class, () -> userStorage.addUser(user));
        user.setLogin("");
        Assertions.assertThrows(ValidationException.class, () -> userStorage.addUser(user));
        user.setLogin("  \t");
        Assertions.assertThrows(ValidationException.class, () -> userStorage.addUser(user));
        restoreValidUser(user);

        // Проверка даты рождения
        user.setBirthday(null);
        Assertions.assertThrows(ValidationException.class, () -> userStorage.addUser(user));
        user.setBirthday(DATE_IN_FUTURE);
        Assertions.assertThrows(ValidationException.class, () -> userStorage.addUser(user));
        user.setBirthday(LocalDate.now());
        Assertions.assertDoesNotThrow(() -> userStorage.addUser(user));
    }

    @Test
    public void testUpdateUser() {

        // Создание пользовательского профиля
        long id = userStorage.addUser(makeValidUser("Bob123")).getId();

        // Проверка корректного id
        User user = new User();
        Assertions.assertThrows(ValidationException.class, () -> userStorage.updateUser(user));
        user.setId(10L);
        Assertions.assertThrows(NotFoundException.class, () -> userStorage.updateUser(user));

        // Проверка обновления адреса э.п.
        user.setId(id);
        user.setEmail("john_dow.12345_at_fakemail.jp");
        Assertions.assertThrows(ValidationException.class, () -> userStorage.updateUser(user));
        user.setEmail("@john_dow.12345_at_fakemail.jp");
        Assertions.assertThrows(ValidationException.class, () -> userStorage.updateUser(user));
        user.setEmail("john_dow.12345 @ fakemail.jp");
        Assertions.assertThrows(ValidationException.class, () -> userStorage.updateUser(user));
        user.setEmail("john_dow.12345@fakemail.jp");
        Assertions.assertDoesNotThrow(() -> userStorage.updateUser(user));

        // Проверка обновления логина
        user.setEmail(null);
        user.setLogin("");
        Assertions.assertThrows(ValidationException.class, () -> userStorage.updateUser(user));
        user.setLogin("  \t");
        Assertions.assertThrows(ValidationException.class, () -> userStorage.updateUser(user));
        user.setLogin("leo");
        Assertions.assertDoesNotThrow(() -> userStorage.updateUser(user));

        // Проверка обновления даты рождения
        user.setLogin(null);
        user.setBirthday(DATE_IN_FUTURE);
        Assertions.assertThrows(ValidationException.class, () -> userStorage.updateUser(user));
        user.setBirthday(LocalDate.now());
        Assertions.assertDoesNotThrow(() -> userStorage.updateUser(user));
    }
}

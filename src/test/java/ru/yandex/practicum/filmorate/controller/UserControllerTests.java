package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

public class UserControllerTests {

    private UserController userController;
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
        userController = new UserController();
    }

    @Test
    public void testCreateUser() {

        User user = makeValidUser("john_dow");

        // Проверка добавления валидной учетной записи
        User result = userController.createUser(user);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.getId());
        user.setId(result.getId());
        Assertions.assertEquals(user, result);
        Assertions.assertEquals(1, userController.getUsers().size());

        // Проверка адреса электронной почты
        user.setEmail(null);
        Assertions.assertThrows(ValidationException.class, () -> userController.createUser(user));
        user.setEmail("john_dow.12345_at_fakemail.jp");
        Assertions.assertThrows(ValidationException.class, () -> userController.createUser(user));
        user.setEmail("@john_dow.12345_at_fakemail.jp");
        Assertions.assertThrows(ValidationException.class, () -> userController.createUser(user));
        user.setEmail("john_dow.12345 @ fakemail.jp");
        Assertions.assertThrows(ValidationException.class, () -> userController.createUser(user));
        restoreValidUser(user);

        // Проверка логина
        user.setLogin(null);
        Assertions.assertThrows(ValidationException.class, () -> userController.createUser(user));
        user.setLogin("");
        Assertions.assertThrows(ValidationException.class, () -> userController.createUser(user));
        user.setLogin("  \t");
        Assertions.assertThrows(ValidationException.class, () -> userController.createUser(user));
        restoreValidUser(user);

        // Проверка даты рождения
        user.setBirthday(null);
        Assertions.assertThrows(ValidationException.class, () -> userController.createUser(user));
        user.setBirthday(DATE_IN_FUTURE);
        Assertions.assertThrows(ValidationException.class, () -> userController.createUser(user));
        user.setBirthday(LocalDate.now());
        Assertions.assertDoesNotThrow(() -> userController.createUser(user));
    }

    @Test
    public void testUpdateUser() {

        // Создание пользовательского профиля
        long id = userController.createUser(makeValidUser("Bob123")).getId();

        // Проверка корректного id
        User user = new User();
        Assertions.assertThrows(ValidationException.class, () -> userController.updateUser(user));
        user.setId(10L);
        Assertions.assertThrows(ValidationException.class, () -> userController.updateUser(user));

        // Проверка обновления адреса э.п.
        user.setId(id);
        user.setEmail("john_dow.12345_at_fakemail.jp");
        Assertions.assertThrows(ValidationException.class, () -> userController.updateUser(user));
        user.setEmail("@john_dow.12345_at_fakemail.jp");
        Assertions.assertThrows(ValidationException.class, () -> userController.updateUser(user));
        user.setEmail("john_dow.12345 @ fakemail.jp");
        Assertions.assertThrows(ValidationException.class, () -> userController.updateUser(user));
        user.setEmail("john_dow.12345@fakemail.jp");
        Assertions.assertDoesNotThrow(() -> userController.updateUser(user));

        // Проверка обновления логина
        user.setEmail(null);
        user.setLogin("");
        Assertions.assertThrows(ValidationException.class, () -> userController.updateUser(user));
        user.setLogin("  \t");
        Assertions.assertThrows(ValidationException.class, () -> userController.updateUser(user));
        user.setLogin("leo");
        Assertions.assertDoesNotThrow(() -> userController.updateUser(user));

        // Проверка обновления даты рождения
        user.setLogin(null);
        user.setBirthday(DATE_IN_FUTURE);
        Assertions.assertThrows(ValidationException.class, () -> userController.updateUser(user));
        user.setBirthday(LocalDate.now());
        Assertions.assertDoesNotThrow(() -> userController.updateUser(user));
    }
}

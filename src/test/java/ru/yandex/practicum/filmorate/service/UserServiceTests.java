package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;

public class UserServiceTests {

    private static final LocalDate DATE_IN_FUTURE = LocalDate.now().plusDays(1);

    private UserService userService;


    @BeforeEach
    public void setUp() {
        userService = new UserService(new InMemoryUserStorage());
    }


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

    private User registerUser() {

        int totalUsers = userService.getUsers().size();
        String suffix = (totalUsers == 0) ? "" : "" + totalUsers;
        User user = new User();
        user.setName("User" + suffix);
        user.setLogin("user_" + suffix);
        user.setEmail("user" + suffix + "@email.com");
        user.setBirthday(LocalDate.now().minusYears(18));
        return userService.addUser(user);
    }

    // endregion


    @Test
    public void testCreateUser() {

        User user = makeValidUser("john_dow");

        // Проверка добавления валидной учетной записи
        User result = userService.addUser(user);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.getId());
        user.setId(result.getId());
        Assertions.assertEquals(user, result);
        Assertions.assertEquals(1, userService.getUsers().size());

        // Проверка адреса электронной почты
        user.setEmail(null);
        Assertions.assertThrows(ValidationException.class, () -> userService.addUser(user));
        user.setEmail("john_dow.12345_at_fakemail.jp");
        Assertions.assertThrows(ValidationException.class, () -> userService.addUser(user));
        user.setEmail("@john_dow.12345_at_fakemail.jp");
        Assertions.assertThrows(ValidationException.class, () -> userService.addUser(user));
        user.setEmail("john_dow.12345 @ fakemail.jp");
        Assertions.assertThrows(ValidationException.class, () -> userService.addUser(user));
        restoreValidUser(user);

        // Проверка логина
        user.setLogin(null);
        Assertions.assertThrows(ValidationException.class, () -> userService.addUser(user));
        user.setLogin("");
        Assertions.assertThrows(ValidationException.class, () -> userService.addUser(user));
        user.setLogin("  \t");
        Assertions.assertThrows(ValidationException.class, () -> userService.addUser(user));
        restoreValidUser(user);

        // Проверка даты рождения
        user.setBirthday(null);
        Assertions.assertThrows(ValidationException.class, () -> userService.addUser(user));
        user.setBirthday(DATE_IN_FUTURE);
        Assertions.assertThrows(ValidationException.class, () -> userService.addUser(user));
        user.setBirthday(LocalDate.now());
        Assertions.assertDoesNotThrow(() -> userService.addUser(user));
    }

    @Test
    public void testUpdateUser() {

        // Создание пользовательского профиля
        long id = userService.addUser(makeValidUser("Bob123")).getId();

        // Проверка корректного id
        User user = new User();
        Assertions.assertThrows(ValidationException.class, () -> userService.updateUser(user));
        user.setId(10L);
        Assertions.assertThrows(NotFoundException.class, () -> userService.updateUser(user));

        // Проверка обновления адреса э.п.
        user.setId(id);
        user.setEmail("john_dow.12345_at_fakemail.jp");
        Assertions.assertThrows(ValidationException.class, () -> userService.updateUser(user));
        user.setEmail("@john_dow.12345_at_fakemail.jp");
        Assertions.assertThrows(ValidationException.class, () -> userService.updateUser(user));
        user.setEmail("john_dow.12345 @ fakemail.jp");
        Assertions.assertThrows(ValidationException.class, () -> userService.updateUser(user));
        user.setEmail("john_dow.12345@fakemail.jp");
        Assertions.assertDoesNotThrow(() -> userService.updateUser(user));

        // Проверка обновления логина
        user.setEmail(null);
        user.setLogin("");
        Assertions.assertThrows(ValidationException.class, () -> userService.updateUser(user));
        user.setLogin("  \t");
        Assertions.assertThrows(ValidationException.class, () -> userService.updateUser(user));
        user.setLogin("leo");
        Assertions.assertDoesNotThrow(() -> userService.updateUser(user));

        // Проверка обновления даты рождения
        user.setLogin(null);
        user.setBirthday(DATE_IN_FUTURE);
        Assertions.assertThrows(ValidationException.class, () -> userService.updateUser(user));
        user.setBirthday(LocalDate.now());
        Assertions.assertDoesNotThrow(() -> userService.updateUser(user));
    }


    @Test
    public void addFriendTest() {

        final User user1 = registerUser();
        final User user2 = registerUser();

        // Нельзя самого себя выбрать в друзья
        Assertions.assertThrows(ValidationException.class,
                () ->  userService.addFriend(user1.getId(), user1.getId()));

        // Нельзя выбрать несуществующего друга
        Assertions.assertThrows(NotFoundException.class,
                () ->  userService.addFriend(user2.getId(), 9999));

        userService.addFriend(user1.getId(), user2.getId());
        User updatedUser1 = userService.getUser(user1.getId());
        User updatedUser2 = userService.getUser(user2.getId());
        Assertions.assertEquals(1, updatedUser1.getFriends().size());
        Assertions.assertTrue(updatedUser1.getFriends().contains(updatedUser2.getId()));
        Assertions.assertEquals(1, updatedUser2.getFriends().size());
        Assertions.assertTrue(updatedUser2.getFriends().contains(updatedUser1.getId()));
    }

    @Test
    public void removeFriendTest() {
        User user1 = registerUser();
        User user2 = registerUser();
        userService.addFriend(user1.getId(), user2.getId());
        userService.removeFriend(user1.getId(), user2.getId());
        Assertions.assertTrue(user1.getFriends().isEmpty());
        Assertions.assertTrue(user2.getFriends().isEmpty());
    }

    @Test
    public void getFriendsTest() {
        User user1 = registerUser();
        User user2 = registerUser();
        User user3 = registerUser();
        userService.addFriend(user1.getId(), user2.getId());
        userService.addFriend(user3.getId(), user1.getId());

        Assertions.assertEquals(2, userService.getFriends(user1.getId()).size());
        Assertions.assertEquals(1, userService.getFriends(user2.getId()).size());
        Assertions.assertEquals(1, userService.getFriends(user3.getId()).size());
    }

    @Test
    public void getCommonFriendsTest() {
        User user1 = registerUser();
        User user2 = registerUser();
        User user3 = registerUser();
        userService.addFriend(user1.getId(), user2.getId());
        userService.addFriend(user3.getId(), user1.getId());

        var commonFriends = userService.getCommonFriends(user2.getId(), user3.getId());
        Assertions.assertEquals(1, commonFriends.size());
        Assertions.assertEquals(1, commonFriends.getFirst().getId());
    }
}

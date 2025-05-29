package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;

public class UserServiceTests {

    private UserService userService;
    private UserStorage userStorage;

    @BeforeEach
    public void setUp() {
        userStorage = new InMemoryUserStorage();
        userService = new UserService(userStorage);
    }

    private User registerUser() {

        int totalUsers = userStorage.getUsers().size();
        String suffix = (totalUsers == 0) ? "" : "" + totalUsers;
        User user = new User();
        user.setName("User" + suffix);
        user.setLogin("user_" + suffix);
        user.setEmail("user" + suffix + "@email.com");
        user.setBirthday(LocalDate.now().minusYears(18));
        return userStorage.addUser(user);
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
        User updatedUser1 = userStorage.getUser(user1.getId());
        User updatedUser2 = userStorage.getUser(user2.getId());
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

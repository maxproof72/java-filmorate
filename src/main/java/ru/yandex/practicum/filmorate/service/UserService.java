package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class UserService {

    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    private void checkNonEqualIds(long id1, long id2) {

        if (id1 == id2) {
            final String msg = "Одинаковые id в данном запросе недопустимы";
            log.warn(msg);
            throw new ValidationException(msg);
        }
    }

    public void addFriend(long userId, long friendId) {

        checkNonEqualIds(userId,friendId);
        User user = userStorage.getUser(userId);
        User friend = userStorage.getUser(friendId);
        user.getFriends().add(friendId);
        friend.getFriends().add(userId);
        log.info("Пользователь {} добавил в друзья пользователя {}", userId, friendId);
    }

    public void removeFriend(long userId, long friendId) {

        checkNonEqualIds(userId,friendId);
        User user = userStorage.getUser(userId);
        User friend = userStorage.getUser(friendId);
        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
        log.info("Пользователь {} удалил из друзей пользователя {}", userId, friendId);
    }

    public List<User> getFriends(long userId) {

        User user = userStorage.getUser(userId);
        var friends = user.getFriends().stream().map(userStorage::getUser).toList();
        log.trace("Запрос друзей пользователя {} возвращает {} записей", userId, friends.size());
        return friends;
    }

    public List<User> getCommonFriends(long userId1, long userId2) {

        checkNonEqualIds(userId1, userId2);
        User user1 = userStorage.getUser(userId1);
        User user2 = userStorage.getUser(userId2);
        Set<Long> commonFriendIds = new HashSet<>(user1.getFriends());
        commonFriendIds.retainAll(user2.getFriends());
        var commonFriends = commonFriendIds.stream().map(userStorage::getUser).toList();
        log.trace("Запрос общих друзей пользователей {} и {} вернул {} записей", userId1, userId2, commonFriends.size());
        return commonFriends;
    }
}

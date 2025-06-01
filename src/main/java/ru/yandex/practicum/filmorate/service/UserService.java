package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.validators.UserValidator;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.*;

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

    public Collection<User> getUsers() {
        return userStorage.getUsers();
    }

    public User getUser(long id) {
        return userStorage.getUser(id);
    }

    public User addUser(User user) {

        try {
            // Проверка адреса электронной почты
            UserValidator.checkEmail(user.getEmail());

            // Проверка логина
            UserValidator.checkLogin(user.getLogin());

            // Проверка даты рождения
            UserValidator.checkBirthday(user.getBirthday());

            // Создание нового пользователя
            return userStorage.addUser(user);

        } catch (ValidationException ve) {
            log.warn(ve.getMessage());
            throw ve;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw e;
        }
    }

    public User updateUser(User user) {
        try {
            Long requestId = user.getId();
            if (requestId == null) {
                throw new ValidationException("Не указан id пользователя");
            }

            // Вначале проверяем все измененные поля, а потом уже их обновляем,
            // чтобы не получить частично обновленный объект
            String newEmail = user.getEmail();
            if (newEmail != null) {
                UserValidator.checkEmail(user.getEmail());
            }

            // Проверка нового логина (если задан)
            String newLogin = user.getLogin();
            if (newLogin != null) {
                UserValidator.checkLogin(user.getLogin());
            }

            // Проверка новой даты рождения (если задана)
            LocalDate newBirthday = user.getBirthday();
            if (newBirthday != null) {
                UserValidator.checkBirthday(user.getBirthday());
            }

            // Все проверки пройдены - можно обновляться
            return userStorage.updateUser(user);

        } catch (ValidationException ve) {
            log.warn(ve.getMessage());
            throw ve;
        } catch (NotFoundException nfe) {
            throw nfe;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw e;
        }
    }
}

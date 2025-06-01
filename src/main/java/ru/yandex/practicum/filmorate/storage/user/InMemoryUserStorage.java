package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();

    private long id;


    public Collection<User> getUsers() {
        return users.values();
    }

    public User addUser(User user) {

        user.setId(++id);
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        users.put(user.getId(), user);
        log.info("Добавлен пользователь {}", user);
        return user;
    }

    @Override
    public User getUser(long id) {

        User user = users.get(id);
        if (user == null) {
            String msg = "Пользователь с id = " + id + " не найден";
            log.warn(msg);
            throw new NotFoundException(msg);
        }
        return user;
    }

    public User updateUser(User user) {

        User existingUser = getUser(user.getId());

        if (user.getEmail() != null)
            existingUser.setEmail(user.getEmail());

        if (user.getLogin() != null)
            existingUser.setLogin(user.getLogin());

        if (user.getBirthday() != null)
            existingUser.setBirthday(user.getBirthday());

        if (user.getName() != null)
            existingUser.setName(user.getName());

        log.info("Информация пользователя с id = {} обновлена", user.getId());
        return existingUser;
    }
}

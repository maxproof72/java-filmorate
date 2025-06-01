package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.validators.FilmValidator;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

@Service
@Slf4j
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;


    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public void likeFilm(long filmId, long userId) {

        Film film = filmStorage.getFilm(filmId);
        User user = userStorage.getUser(userId);
        film.getLikes().add(user.getId());
        film.setRate(film.getLikes().size());
        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
    }

    public void unlikeFilm(long filmId, long userId) {

        Film film = filmStorage.getFilm(filmId);
        User user = userStorage.getUser(userId);
        film.getLikes().remove(user.getId());
        film.setRate(film.getLikes().size());
        log.info("Пользователь {} снял лайк у фильма {}", userId, filmId);
    }

    public List<Film> getPopularFilms(int count) {

        var popularFilms = filmStorage.getFilms().stream()
                .sorted(Comparator.comparingInt(film -> ((Film) film).getRate()).reversed())
                .limit(count)
                .toList();
        log.trace("Запрос популярных фильмов возвращает {} записей", popularFilms.size());
        return popularFilms;
    }

    public Collection<Film> getFilms() {
        return filmStorage.getFilms();
    }

    public Film getFilm(long id) {
        return filmStorage.getFilm(id);
    }

    public Film addFilm(Film film) {

        try {
            // Название не может быть пустым;
            final String name = film.getName();
            if (name == null || name.isBlank()) {
                throw new ValidationException("Название фильма не может быть пустым");
            }

            // Проверка описания
            final String description = film.getDescription();
            if (description != null)
                FilmValidator.checkDescription(description);

            // Проверка даты релиза
            FilmValidator.checkReleaseDate(film.getReleaseDate());

            // Проверка продолжительности фильма
            FilmValidator.checkDuration(film.getDuration());

            // Создание нового фильма
            return filmStorage.addFilm(film);

        } catch (ValidationException ve) {
            log.warn(ve.getMessage());
            throw ve;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw e;
        }
    }

    public Film updateFilm(Film film) {

        try {
            Long requestedId = film.getId();
            if (requestedId == null) {
                throw new ValidationException("Не указан id фильма");
            }

            // Вначале производим все проверки, а затем обновляем объект,
            // чтобы избежать его частичного обновления
            // Проверка нового описания (если задано)
            if (film.getDescription() != null)
                FilmValidator.checkDescription(film.getDescription());

            // Проверка новой даты релиза (если задана)
            if (film.getReleaseDate() != null)
                FilmValidator.checkReleaseDate(film.getReleaseDate());

            // Проверка новой длительности (если задана)
            if (film.getDuration() != null)
                FilmValidator.checkDuration(film.getDuration());

            // Все проверки пройдены - можно обновляться
            return filmStorage.updateFilm(film);

        } catch (ValidationException ve) {
            log.warn(ve.getMessage());
            throw ve;
        } catch (NotFoundException ne) {
            throw ne;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw e;
        }
    }
}

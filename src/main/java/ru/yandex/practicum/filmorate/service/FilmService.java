package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
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
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film) {
        return filmStorage.updateFilm(film);
    }
}

package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;

public class FilmServiceTests {

    private FilmStorage filmStorage;
    private UserStorage userStorage;
    private FilmService filmService;

    @BeforeEach
    public void setUp() {
        filmStorage = new InMemoryFilmStorage();
        userStorage = new InMemoryUserStorage();
        filmService = new FilmService(filmStorage, userStorage);
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

    private Film registerFilm() {

        int totalFilms = filmStorage.getFilms().size();
        String suffix = (totalFilms == 0) ? "" : " " + totalFilms;
        Film film = new Film();
        film.setName("Film" + suffix);
        film.setDescription("Film" + suffix + " description");
        film.setReleaseDate(LocalDate.now().minusYears(5));
        film.setDuration(120);
        return filmStorage.addFilm(film);
    }

    @Test
    public void testLikeFilm() {

        User user1 = registerUser();
        User user2 = registerUser();
        Film film = registerFilm();
        filmService.likeFilm(film.getId(), user1.getId());
        Assertions.assertEquals(1, filmStorage.getFilm(film.getId()).getLikes().size());
        filmService.likeFilm(film.getId(), user1.getId());
        Assertions.assertEquals(1, filmStorage.getFilm(film.getId()).getLikes().size());
        filmService.likeFilm(film.getId(), user2.getId());
        Assertions.assertEquals(2, filmStorage.getFilm(film.getId()).getLikes().size());
    }

    @Test
    public void testUnlikeFilm() {

        User user1 = registerUser();
        User user2 = registerUser();
        Film film = registerFilm();
        filmService.likeFilm(film.getId(), user1.getId());
        filmService.likeFilm(film.getId(), user2.getId());
        Assertions.assertEquals(2, filmStorage.getFilm(film.getId()).getLikes().size());
        filmService.unlikeFilm(film.getId(), user1.getId());
        Assertions.assertEquals(1, filmStorage.getFilm(film.getId()).getLikes().size());
        filmService.unlikeFilm(film.getId(), user2.getId());
        Assertions.assertEquals(0, filmStorage.getFilm(film.getId()).getLikes().size());
    }

    @Test
    public void testGetPopularFilms() {

        User user1 = registerUser();
        User user2 = registerUser();
        User user3 = registerUser();
        Film film1 = registerFilm();
        Film film2 = registerFilm();
        Film film3 = registerFilm();    // 3x users X 3x films
        filmService.likeFilm(film1.getId(), user1.getId());
        filmService.likeFilm(film2.getId(), user1.getId());
        filmService.likeFilm(film2.getId(), user2.getId());
        filmService.likeFilm(film2.getId(), user3.getId());
        filmService.likeFilm(film3.getId(), user2.getId());
        filmService.likeFilm(film3.getId(), user3.getId());     // film1: 1, film2: 3, film 3: 2
        List<Film> popularFilms = filmService.getPopularFilms(3);
        Assertions.assertEquals(3, popularFilms.size());
        Assertions.assertArrayEquals(new long[] {2, 3, 1},
                popularFilms.stream().mapToLong(Film::getId).toArray());
        popularFilms = filmService.getPopularFilms(2);
        Assertions.assertEquals(2, popularFilms.size());
        Assertions.assertArrayEquals(new long[] {2, 3},
                popularFilms.stream().mapToLong(Film::getId).toArray());
    }
}

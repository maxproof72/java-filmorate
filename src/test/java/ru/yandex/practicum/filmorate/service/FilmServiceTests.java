package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.validators.FilmValidator;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public class FilmServiceTests {

    private static final String LIMITED_DESCRIPTION = "X".repeat(FilmValidator.MAX_DESCRIPTION_LENGTH);
    private static final String TOO_BIG_DESCRIPTION = "X".repeat(FilmValidator.MAX_DESCRIPTION_LENGTH + 1);
    private static final LocalDate USUAL_DATE = LocalDate.of(2020, 6, 22);
    private static final LocalDate TOO_OLD_DATE = FilmValidator.MOVIE_BIRTHDAY.minusDays(1);


    private UserService userService;
    private FilmService filmService;

    // region Helpers

    private void restoreValidFilm(Film film, String name) {

        film.setName(name);
        film.setDescription("Film of the century");
        film.setReleaseDate(USUAL_DATE);
        film.setDuration(100);
    }

    private void restoreValidFilm(Film film) {
        restoreValidFilm(film, "Film");
    }

    private Film makeValidFilm(String name) {

        Film film = new Film();
        restoreValidFilm(film, name);
        return film;
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

    private Film registerFilm() {

        int totalFilms = filmService.getFilms().size();
        String suffix = (totalFilms == 0) ? "" : " " + totalFilms;
        Film film = new Film();
        film.setName("Film" + suffix);
        film.setDescription("Film" + suffix + " description");
        film.setReleaseDate(LocalDate.now().minusYears(5));
        film.setDuration(120);
        return filmService.addFilm(film);
    }

    // endregion


    @BeforeEach
    public void setUp() {
        
        FilmStorage filmStorage = new InMemoryFilmStorage();
        UserStorage userStorage = new InMemoryUserStorage();
        filmService = new FilmService(filmStorage, userStorage);
        userService = new UserService(userStorage);
    }

    @Test
    public void testGetFilms() {

        Assertions.assertDoesNotThrow(() -> filmService.addFilm(makeValidFilm("Film1")));
        Assertions.assertDoesNotThrow(() -> filmService.addFilm(makeValidFilm("Film2")));
        Assertions.assertDoesNotThrow(() -> filmService.addFilm(makeValidFilm("Film3")));
        Collection<Film> films = filmService.getFilms();
        Assertions.assertNotNull(films);
        Assertions.assertEquals(3, films.size());
        Assertions.assertTrue(films.stream().anyMatch(film -> "Film1".equals(film.getName())));
        Assertions.assertTrue(films.stream().anyMatch(film -> "Film2".equals(film.getName())));
        Assertions.assertTrue(films.stream().anyMatch(film -> "Film3".equals(film.getName())));
    }

    @Test
    public void testAddFilm() {

        Film film = makeValidFilm("Film");

        // Проверка добавления валидной записи
        Film result = filmService.addFilm(film);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.getId());
        film.setId(result.getId());
        Assertions.assertEquals(film, result);
        Assertions.assertEquals(1, filmService.getFilms().size());
        restoreValidFilm(film);

        // Проверка пустого названия
        film.setName(null);
        Assertions.assertThrows(ValidationException.class, () -> filmService.addFilm(film));
        film.setName("");
        Assertions.assertThrows(ValidationException.class, () -> filmService.addFilm(film));
        film.setName("   ");
        Assertions.assertThrows(ValidationException.class, () -> filmService.addFilm(film));
        Assertions.assertEquals(1, filmService.getFilms().size());
        restoreValidFilm(film);

        // Проверка длины описания
        film.setDescription(LIMITED_DESCRIPTION);
        Assertions.assertDoesNotThrow(() -> filmService.addFilm(film));
        film.setDescription(TOO_BIG_DESCRIPTION);
        Assertions.assertThrows(ValidationException.class, () -> filmService.addFilm(film));
        Assertions.assertEquals(2, filmService.getFilms().size());
        restoreValidFilm(film);

        // Проверка даты релиза
        film.setReleaseDate(null);
        Assertions.assertThrows(ValidationException.class, () -> filmService.addFilm(film));
        film.setReleaseDate(TOO_OLD_DATE);
        Assertions.assertThrows(ValidationException.class, () -> filmService.addFilm(film));
        film.setReleaseDate(FilmValidator.MOVIE_BIRTHDAY);
        Assertions.assertDoesNotThrow(() -> filmService.addFilm(film));
        restoreValidFilm(film);

        // Проверка длительности
        film.setDuration(null);
        Assertions.assertThrows(ValidationException.class, () -> filmService.addFilm(film));
        film.setDuration(-1);
        Assertions.assertThrows(ValidationException.class, () -> filmService.addFilm(film));
        film.setDuration(0);
        Assertions.assertThrows(ValidationException.class, () -> filmService.addFilm(film));
        film.setDuration(1);
        Assertions.assertDoesNotThrow(() -> filmService.addFilm(film));
    }

    @Test
    public void testUpdateFilm() {

        // Создание записи фильма
        long id = filmService.addFilm(makeValidFilm("Film1")).getId();

        // Проверка корректного id
        Film film = new Film();
        Assertions.assertThrows(ValidationException.class, () -> filmService.updateFilm(film));
        film.setId(10L);
        Assertions.assertThrows(NotFoundException.class, () -> filmService.updateFilm(film));

        // Проверка обновления названия фильма
        film.setId(id);
        film.setName("Film2");
        Film result = filmService.updateFilm(film);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.getId());
        Assertions.assertEquals(film.getName(), result.getName());

        // Проверка обновления описания
        film.setName(null);
        film.setDescription(TOO_BIG_DESCRIPTION);
        Assertions.assertThrows(ValidationException.class, () -> filmService.updateFilm(film));
        film.setDescription(LIMITED_DESCRIPTION);
        Film result2 = filmService.updateFilm(film);
        Assertions.assertEquals(LIMITED_DESCRIPTION, result2.getDescription());

        // Проверка даты релиза
        film.setDescription(null);
        film.setReleaseDate(TOO_OLD_DATE);
        Assertions.assertThrows(ValidationException.class, () -> filmService.updateFilm(film));
        film.setReleaseDate(FilmValidator.MOVIE_BIRTHDAY);
        Assertions.assertDoesNotThrow(() -> filmService.updateFilm(film));

        // Проверка длительности
        film.setReleaseDate(null);
        film.setDuration(-1);
        Assertions.assertThrows(ValidationException.class, () -> filmService.updateFilm(film));
        film.setDuration(0);
        Assertions.assertThrows(ValidationException.class, () -> filmService.updateFilm(film));
        film.setDuration(1);
        Assertions.assertDoesNotThrow(() -> filmService.updateFilm(film));
    }
    
    @Test
    public void testLikeFilm() {

        User user1 = registerUser();
        User user2 = registerUser();
        Film film = registerFilm();
        filmService.likeFilm(film.getId(), user1.getId());
        Assertions.assertEquals(1, filmService.getFilm(film.getId()).getLikes().size());
        filmService.likeFilm(film.getId(), user1.getId());
        Assertions.assertEquals(1, filmService.getFilm(film.getId()).getLikes().size());
        filmService.likeFilm(film.getId(), user2.getId());
        Assertions.assertEquals(2, filmService.getFilm(film.getId()).getLikes().size());
    }

    @Test
    public void testUnlikeFilm() {

        User user1 = registerUser();
        User user2 = registerUser();
        Film film = registerFilm();
        filmService.likeFilm(film.getId(), user1.getId());
        filmService.likeFilm(film.getId(), user2.getId());
        Assertions.assertEquals(2, filmService.getFilm(film.getId()).getLikes().size());
        filmService.unlikeFilm(film.getId(), user1.getId());
        Assertions.assertEquals(1, filmService.getFilm(film.getId()).getLikes().size());
        filmService.unlikeFilm(film.getId(), user2.getId());
        Assertions.assertEquals(0, filmService.getFilm(film.getId()).getLikes().size());
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

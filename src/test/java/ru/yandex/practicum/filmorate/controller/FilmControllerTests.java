package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

public class FilmControllerTests {

    private FilmController filmController;
    private static final String LIMITED_DESCRIPTION = "X".repeat(FilmController.MAX_DESCRIPTION_LENGTH);
    private static final String TOO_BIG_DESCRIPTION = "X".repeat(FilmController.MAX_DESCRIPTION_LENGTH + 1);
    private static final Date USUAL_DATE;
    private static final Date TOO_OLD_DATE;
    static {
        Calendar cal = Calendar.getInstance();
        cal.set(2000, Calendar.JUNE, 22);
        USUAL_DATE = cal.getTime();
        cal.set(1895, Calendar.DECEMBER, 27);
        TOO_OLD_DATE = cal.getTime();
    }


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

    // endregion


    @BeforeEach
    public void setUp() {
        filmController = new FilmController();
    }


    @Test
    public void testGetFilms() {

        Assertions.assertDoesNotThrow(() -> filmController.addFilm(makeValidFilm("Film1")));
        Assertions.assertDoesNotThrow(() -> filmController.addFilm(makeValidFilm("Film2")));
        Assertions.assertDoesNotThrow(() -> filmController.addFilm(makeValidFilm("Film3")));
        Collection<Film> films = filmController.getFilms();
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
        Film result = filmController.addFilm(film);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.getId());
        film.setId(result.getId());
        Assertions.assertEquals(film, result);
        Assertions.assertEquals(1, filmController.getFilms().size());
        restoreValidFilm(film);

        // Проверка пустого названия
        film.setName(null);
        Assertions.assertThrows(ValidationException.class, () -> filmController.addFilm(film));
        film.setName("");
        Assertions.assertThrows(ValidationException.class, () -> filmController.addFilm(film));
        film.setName("   ");
        Assertions.assertThrows(ValidationException.class, () -> filmController.addFilm(film));
        Assertions.assertEquals(1, filmController.getFilms().size());
        restoreValidFilm(film);

        // Проверка длины описания
        film.setDescription(LIMITED_DESCRIPTION);
        Assertions.assertDoesNotThrow(() -> filmController.addFilm(film));
        film.setDescription(TOO_BIG_DESCRIPTION);
        Assertions.assertThrows(ValidationException.class, () -> filmController.addFilm(film));
        Assertions.assertEquals(2, filmController.getFilms().size());
        restoreValidFilm(film);

        // Проверка даты релиза
        film.setReleaseDate(null);
        Assertions.assertThrows(ValidationException.class, () -> filmController.addFilm(film));
        film.setReleaseDate(TOO_OLD_DATE);
        Assertions.assertThrows(ValidationException.class, () -> filmController.addFilm(film));
        film.setReleaseDate(FilmController.MOVIE_BIRTHDAY);
        Assertions.assertDoesNotThrow(() -> filmController.addFilm(film));
        restoreValidFilm(film);

        // Проверка длительности
        film.setDuration(null);
        Assertions.assertThrows(ValidationException.class, () -> filmController.addFilm(film));
        film.setDuration(-1);
        Assertions.assertThrows(ValidationException.class, () -> filmController.addFilm(film));
        film.setDuration(0);
        Assertions.assertThrows(ValidationException.class, () -> filmController.addFilm(film));
        film.setDuration(1);
        Assertions.assertDoesNotThrow(() -> filmController.addFilm(film));
    }

    @Test
    public void testUpdateFilm() {

        // Создание записи фильма
        int id = filmController.addFilm(makeValidFilm("Film1")).getId();

        // Проверка корректного id
        Film film = new Film();
        Assertions.assertThrows(ValidationException.class, () -> filmController.updateFilm(film));
        film.setId(10);
        Assertions.assertThrows(ValidationException.class, () -> filmController.updateFilm(film));

        // Проверка обновления названия фильма
        film.setId(id);
        film.setName("Film2");
        Film result = filmController.updateFilm(film);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.getId());
        Assertions.assertEquals(film.getName(), result.getName());

        // Проверка обновления описания
        film.setName(null);
        film.setDescription(TOO_BIG_DESCRIPTION);
        Assertions.assertThrows(ValidationException.class, () -> filmController.updateFilm(film));
        film.setDescription(LIMITED_DESCRIPTION);
        Film result2 = filmController.updateFilm(film);
        Assertions.assertEquals(LIMITED_DESCRIPTION, result2.getDescription());

        // Проверка даты релиза
        film.setDescription(null);
        film.setReleaseDate(TOO_OLD_DATE);
        Assertions.assertThrows(ValidationException.class, () -> filmController.updateFilm(film));
        film.setReleaseDate(FilmController.MOVIE_BIRTHDAY);
        Assertions.assertDoesNotThrow(() -> filmController.updateFilm(film));

        // Проверка длительности
        film.setReleaseDate(null);
        film.setDuration(-1);
        Assertions.assertThrows(ValidationException.class, () -> filmController.updateFilm(film));
        film.setDuration(0);
        Assertions.assertThrows(ValidationException.class, () -> filmController.updateFilm(film));
        film.setDuration(1);
        Assertions.assertDoesNotThrow(() -> filmController.updateFilm(film));
    }
}

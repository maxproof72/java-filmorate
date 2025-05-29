package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.validators.FilmValidator;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;

import java.time.LocalDate;
import java.util.Collection;


public class FilmStorageTests {

    private FilmStorage filmStorage;
    private static final String LIMITED_DESCRIPTION = "X".repeat(FilmValidator.MAX_DESCRIPTION_LENGTH);
    private static final String TOO_BIG_DESCRIPTION = "X".repeat(FilmValidator.MAX_DESCRIPTION_LENGTH + 1);
    private static final LocalDate USUAL_DATE = LocalDate.of(2020, 6, 22);
    private static final LocalDate TOO_OLD_DATE = FilmValidator.MOVIE_BIRTHDAY.minusDays(1);


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
        filmStorage = new InMemoryFilmStorage();
    }


    @Test
    public void testGetFilms() {

        Assertions.assertDoesNotThrow(() -> filmStorage.addFilm(makeValidFilm("Film1")));
        Assertions.assertDoesNotThrow(() -> filmStorage.addFilm(makeValidFilm("Film2")));
        Assertions.assertDoesNotThrow(() -> filmStorage.addFilm(makeValidFilm("Film3")));
        Collection<Film> films = filmStorage.getFilms();
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
        Film result = filmStorage.addFilm(film);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.getId());
        film.setId(result.getId());
        Assertions.assertEquals(film, result);
        Assertions.assertEquals(1, filmStorage.getFilms().size());
        restoreValidFilm(film);

        // Проверка пустого названия
        film.setName(null);
        Assertions.assertThrows(ValidationException.class, () -> filmStorage.addFilm(film));
        film.setName("");
        Assertions.assertThrows(ValidationException.class, () -> filmStorage.addFilm(film));
        film.setName("   ");
        Assertions.assertThrows(ValidationException.class, () -> filmStorage.addFilm(film));
        Assertions.assertEquals(1, filmStorage.getFilms().size());
        restoreValidFilm(film);

        // Проверка длины описания
        film.setDescription(LIMITED_DESCRIPTION);
        Assertions.assertDoesNotThrow(() -> filmStorage.addFilm(film));
        film.setDescription(TOO_BIG_DESCRIPTION);
        Assertions.assertThrows(ValidationException.class, () -> filmStorage.addFilm(film));
        Assertions.assertEquals(2, filmStorage.getFilms().size());
        restoreValidFilm(film);

        // Проверка даты релиза
        film.setReleaseDate(null);
        Assertions.assertThrows(ValidationException.class, () -> filmStorage.addFilm(film));
        film.setReleaseDate(TOO_OLD_DATE);
        Assertions.assertThrows(ValidationException.class, () -> filmStorage.addFilm(film));
        film.setReleaseDate(FilmValidator.MOVIE_BIRTHDAY);
        Assertions.assertDoesNotThrow(() -> filmStorage.addFilm(film));
        restoreValidFilm(film);

        // Проверка длительности
        film.setDuration(null);
        Assertions.assertThrows(ValidationException.class, () -> filmStorage.addFilm(film));
        film.setDuration(-1);
        Assertions.assertThrows(ValidationException.class, () -> filmStorage.addFilm(film));
        film.setDuration(0);
        Assertions.assertThrows(ValidationException.class, () -> filmStorage.addFilm(film));
        film.setDuration(1);
        Assertions.assertDoesNotThrow(() -> filmStorage.addFilm(film));
    }

    @Test
    public void testUpdateFilm() {

        // Создание записи фильма
        long id = filmStorage.addFilm(makeValidFilm("Film1")).getId();

        // Проверка корректного id
        Film film = new Film();
        Assertions.assertThrows(ValidationException.class, () -> filmStorage.updateFilm(film));
        film.setId(10L);
        Assertions.assertThrows(NotFoundException.class, () -> filmStorage.updateFilm(film));

        // Проверка обновления названия фильма
        film.setId(id);
        film.setName("Film2");
        Film result = filmStorage.updateFilm(film);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.getId());
        Assertions.assertEquals(film.getName(), result.getName());

        // Проверка обновления описания
        film.setName(null);
        film.setDescription(TOO_BIG_DESCRIPTION);
        Assertions.assertThrows(ValidationException.class, () -> filmStorage.updateFilm(film));
        film.setDescription(LIMITED_DESCRIPTION);
        Film result2 = filmStorage.updateFilm(film);
        Assertions.assertEquals(LIMITED_DESCRIPTION, result2.getDescription());

        // Проверка даты релиза
        film.setDescription(null);
        film.setReleaseDate(TOO_OLD_DATE);
        Assertions.assertThrows(ValidationException.class, () -> filmStorage.updateFilm(film));
        film.setReleaseDate(FilmValidator.MOVIE_BIRTHDAY);
        Assertions.assertDoesNotThrow(() -> filmStorage.updateFilm(film));

        // Проверка длительности
        film.setReleaseDate(null);
        film.setDuration(-1);
        Assertions.assertThrows(ValidationException.class, () -> filmStorage.updateFilm(film));
        film.setDuration(0);
        Assertions.assertThrows(ValidationException.class, () -> filmStorage.updateFilm(film));
        film.setDuration(1);
        Assertions.assertDoesNotThrow(() -> filmStorage.updateFilm(film));
    }
}


-- таблица пользователей
CREATE TABLE users (
  id INTEGER PRIMARY KEY,       -- PK
  email TEXT NOT NULL,          -- электронная почта
  login TEXT NOT NULL,          -- логин
  name TEXT NOT NULL,           -- отображаемое имя
  birthday DATE                 -- день рождения
);

-- test data
INSERT INTO users VALUES (0001, 'yana.phil@mail.ru', 'yana-phil', 'Яна Филиппова', CAST('1972-05-15' AS DATE));
INSERT INTO users VALUES (0002, 'kirill.kisl@ya.ru', 'kira72', 'Кирилл Кислов', CAST('1972-07-12' AS DATE));
INSERT INTO users VALUES (0003, 'buha@ya.ru', 'bukharinSuper', 'Ромка Бухарин', CAST('1972-07-05' AS DATE));
INSERT INTO users VALUES (0004, 'bobrova72@gmail.com', 'bobrova', 'Ленка Боброва', CAST('1972-10-19' AS DATE));
INSERT INTO users VALUES (0005, 'mee@maxproof.ru', 'maxproof', 'Макс Парфентьев', CAST('1972-05-18' AS DATE));

-- fetch 
SELECT * FROM users;

-- таблица дружбы
CREATE TABLE friends (
  id INTEGER PRIMARY KEY,       -- PK
  source_id INTEGER NOT NULL,   -- FK: пользователь, подавший заявку на дружбу
  target_id INTEGER NOT NULL,   -- FK: пользователь, с которым хотят дружить
  accepted BOOLEAN              -- дружба согласована
);

-- test data
INSERT INTO friends VALUES (0001, 3, 1, FALSE);
INSERT INTO friends VALUES (0002, 4, 5, TRUE);
INSERT INTO friends VALUES (0003, 2, 1, TRUE);
INSERT INTO friends VALUES (0004, 2, 3, FALSE);
INSERT INTO friends VALUES (0005, 4, 2, TRUE);
INSERT INTO friends VALUES (0006, 2, 5, TRUE);
INSERT INTO friends VALUES (0007, 1, 4, TRUE);
INSERT INTO friends VALUES (0008, 3, 4, TRUE);

-- запрос перечня подтвержденной дружбы
SELECT u1.name, u2.name
FROM (users AS u1 INNER JOIN friends AS f ON u1.id=f.source_id)
INNER JOIN users AS u2 ON f.target_id=u2.id
WHERE f.accepted;

-- запрос списка друзей пользователя id=2
SELECT source_id FROM friends
WHERE accepted AND target_id=2
union
SELECT target_id FROM friends
WHERE accepted AND source_id=2;

-- запрос списка общих друзей пользователей id=1 и id=5
(SELECT source_id FROM friends
WHERE accepted AND target_id=1
union
SELECT target_id FROM friends
WHERE accepted AND source_id=1)
intersect
(SELECT source_id FROM friends
WHERE accepted AND target_id=5
union
SELECT target_id FROM friends
WHERE accepted AND source_id=5);

-- таблица перечня жанров
CREATE TABLE genre (
  id INTEGER PRIMARY KEY,       -- PK
  name TEXT NOT NULL            -- наименование жанра
);

-- test data
INSERT INTO genre VALUES(0001, 'Комедия');
INSERT INTO genre VALUES(0002, 'Драма');
INSERT INTO genre VALUES(0003, 'Мультфильм');
INSERT INTO genre VALUES(0004, 'Триллер');
INSERT INTO genre VALUES(0005, 'Документальный');
INSERT INTO genre VALUES(0006, 'Боевик');
INSERT INTO genre VALUES(0007, 'Фантастика');
INSERT INTO genre VALUES(0008, 'Ужасы');

-- таблица MPA рейтинга
CREATE TABLE mpa_rating (
  id INTEGER PRIMARY KEY,       -- PK
  name varchar(10) NOT NULL,    -- рейтинг MPA
  description TEXT              -- описание рейтинга
);

-- test data
INSERT INTO mpa_rating VALUES(0001,	'G', 'У фильма нет возрастных ограничений');
INSERT INTO mpa_rating VALUES(0002,	'PG', 'Детям рекомендуется смотреть фильм с родителями');
INSERT INTO mpa_rating VALUES(0003,	'PG-13', 'Детям до 13 лет просмотр не желателен');
INSERT INTO mpa_rating VALUES(0004,	'R', 'Лицам до 17 лет просматривать фильм можно только в присутствии взрослого');
INSERT INTO mpa_rating VALUES(0005,	'NC-17', 'Лицам до 18 лет просмотр запрещён');

-- таблица фильмов
CREATE TABLE films (
  id INTEGER PRIMARY KEY,       -- PK
  name TEXT NOT NULL,           -- название фильма
  description TEXT,             -- описание фильма
  release_date DATE NOT NULL,   -- дата выхода
  duration INTEGER,             -- длительность в минутах
  user_rating INTEGER,          -- оценка пользователей (*)
  mpa_rating_id INTEGER         -- FK: рейтинг MPA
);

-- test data
INSERT INTO films VALUES(0001, 'Нечто',	'Фантастика по роману Кто идет', CAST('1982-05-12' AS DATE), 109, 0, 4);
INSERT INTO films VALUES(0002, 'Оно', 'По Кингу', CAST('2013-11-15' AS DATE), 185, 0, 3);
INSERT INTO films VALUES(0003, 'Бездна', 'Про подводную станцию', CAST('2010-06-13' AS DATE), 168, 0, 2);
INSERT INTO films VALUES(0004, 'Ущелье', 'Про страшный туман на дне ущелья', CAST('2024-06-11' AS DATE), 110, 0, 4);
INSERT INTO films VALUES(0005, 'Красное предупреждение', 'Бред про яйца Клеопатры', CAST('2023-05-18' AS DATE), 135, 0, 3);

-- таблица жанров фильмов
CREATE TABLE film_genres (
  id INTEGER PRIMARY KEY,       -- PK
  film_id INTEGER NOT NULL,     -- FK: фильм
  genre_id INTEGER NOT NULL     -- FK: жанр
);

-- test data
INSERT INTO film_genres VALUES(0001, 1, 4);
INSERT INTO film_genres VALUES(0002, 1, 2);
INSERT INTO film_genres VALUES(0005, 2, 7);
INSERT INTO film_genres VALUES(0006, 3, 7);
INSERT INTO film_genres VALUES(0007, 3, 2);
INSERT INTO film_genres VALUES(0008, 4, 7);
INSERT INTO film_genres VALUES(0009, 4, 6);
INSERT INTO film_genres VALUES(00010, 5, 1);
INSERT INTO film_genres VALUES(00011, 5, 6);

-- запрос получения всех фильмов с рейтингом 'PG-13'
SELECT films.name
FROM films INNER JOIN mpa_rating ON films.mpa_rating_id=mpa_rating.id
WHERE mpa_rating.name = 'PG-13';

-- таблица лайков
CREATE TABLE likes (
  id INTEGER PRIMARY KEY,       -- PK  
  film_id INTEGER NOT NULL,     -- FK: фильм
  user_id INTEGER NOT NULL      -- FK: пользователь
);

-- test data
INSERT INTO likes VALUES(0001, 1, 2);
INSERT INTO likes VALUES(0002, 1, 5);
INSERT INTO likes VALUES(0003, 2, 3);
INSERT INTO likes VALUES(0004, 3, 1);
INSERT INTO likes VALUES(0005, 3, 4);
INSERT INTO likes VALUES(0006, 4, 5);
INSERT INTO likes VALUES(0007, 5, 5);
INSERT INTO likes VALUES(0008, 5, 4);
INSERT INTO likes VALUES(0009, 3, 5);

-- запрос рейтинга пользователей, голосовавших чаще других
SELECT users.name, Count(film_id) AS film_count
FROM users INNER JOIN likes ON users.id = likes.user_id
GROUP BY users.name, likes.user_id
ORDER BY film_count DESC
LIMIT 5;

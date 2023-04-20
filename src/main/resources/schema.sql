DROP TABLE IF EXISTS public.mpa, public.users, public.films, public.genres, public.genre, public.friend, public."LIKE";

CREATE TABLE IF NOT EXISTS public.mpa (
                                           id INT NOT NULL PRIMARY KEY,
                                           name VARCHAR(16) NOT NULL
);

CREATE TABLE IF NOT EXISTS public.users (
                                            id INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                                            email VARCHAR(64) UNIQUE NOT NULL,
                                            login VARCHAR(64) UNIQUE NOT NULL,
                                            name VARCHAR(64) NOT NULL,
                                            birthday DATE NOT NULL CHECK (birthday < NOW())
);

CREATE TABLE IF NOT EXISTS public.films (
                                            id INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                                            name VARCHAR(64) UNIQUE NOT NULL,
                                            description VARCHAR(200) UNIQUE NOT NULL,
                                            duration INT NOT NULL,
                                            release_date DATE NOT NULL,
                                            rate REAL,
                                            mpa INT REFERENCES public.mpa(id) --ON DELETE RESTRICT ON UPDATE NO ACTION
);

CREATE TABLE IF NOT EXISTS public.genres (
                                             id INT PRIMARY KEY AUTO_INCREMENT,
                                             name VARCHAR(64) NOT NULL
);

CREATE TABLE IF NOT EXISTS public.genre (
                                            film_id INT NOT NULL REFERENCES public.films(id) ON DELETE CASCADE ON UPDATE NO ACTION,
                                            genre_id INT NOT NULL REFERENCES public.genres(id) ON DELETE CASCADE ON UPDATE NO ACTION
);

CREATE TABLE IF NOT EXISTS public.friend (
                                              user_id INT NOT NULL REFERENCES public.users(id) ON DELETE CASCADE ON UPDATE NO ACTION,
                                              friend_id INT NOT NULL REFERENCES public.users(id) ON DELETE CASCADE ON UPDATE NO ACTION,
                                              status VARCHAR(64) NOT NULL
);

CREATE TABLE IF NOT EXISTS public."LIKE" (
                                            user_id INT NOT NULL REFERENCES public.users(id) ON DELETE CASCADE ON UPDATE NO ACTION,
                                            film_id INT NOT NULL REFERENCES public.films(id) ON DELETE CASCADE ON UPDATE NO ACTION
);

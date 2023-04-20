MERGE INTO public.mpa KEY (id) VALUES (1, 'G');
MERGE INTO public.mpa KEY (id) VALUES (2, 'PG');
MERGE INTO public.mpa KEY (id) VALUES (3, 'PG-13');
MERGE INTO public.mpa KEY (id) VALUES (4, 'R');
MERGE INTO public.mpa KEY (id) VALUES (5, 'NC-17');

INSERT INTO public.genres (name) VALUES ('Комедия');
INSERT INTO public.genres (name) VALUES ('Драма');
INSERT INTO public.genres (name) VALUES ('Мультфильм');
INSERT INTO public.genres (name) VALUES ('Триллер');
INSERT INTO public.genres (name) VALUES ('Документальный');
INSERT INTO public.genres (name) VALUES ('Боевик');
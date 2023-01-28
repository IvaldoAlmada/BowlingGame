CREATE TABLE games
(
    id serial NOT NULL,
    name character varying NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE frames
(
    id      serial  NOT NULL,
    PRIMARY KEY (id),
    number integer NOT NULL,
    game_id integer NOT NULL
);

ALTER TABLE frames
    ADD FOREIGN KEY (game_id) REFERENCES games (id);

CREATE TABLE rolls
(
    id       serial  NOT NULL,
    PRIMARY KEY (id),
    number integer NOT NULL,
    score    integer,
    frame_id integer NOT NULL
);

ALTER TABLE rolls
    ADD FOREIGN KEY (frame_id) REFERENCES frames (id);
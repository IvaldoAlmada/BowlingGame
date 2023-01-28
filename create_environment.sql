CREATE
DATABASE bowling_game;
\c
bowling_game;


CREATE TABLE games
(
    id serial NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE frames
(
    id      serial  NOT NULL,
    PRIMARY KEY (id),
    game_id integer NOT NULL
);

ALTER TABLE frames
    ADD FOREIGN KEY (game_id) REFERENCES games (id);

CREATE TABLE rows
(
    id       serial  NOT NULL,
    PRIMARY KEY (id),
    score    integer,
    frame_id integer NOT NULL
);

ALTER TABLE rows
    ADD FOREIGN KEY (frame_id) REFERENCES frames (id)


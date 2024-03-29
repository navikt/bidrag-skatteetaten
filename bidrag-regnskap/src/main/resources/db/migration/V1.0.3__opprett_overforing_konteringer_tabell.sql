CREATE TABLE IF NOT EXISTS overforing_konteringer
(
    overforing_id       integer PRIMARY KEY GENERATED BY DEFAULT AS IDENTITY (INCREMENT 1 START 1 MINVALUE 1),
    kontering_id        integer REFERENCES konteringer,
    referansekode       text,
    feilmelding         text,
    tidspunkt           timestamp NOT NULL,
    kanal               text      NOT NULL,
    opprettet_tidspunkt timestamp DEFAULT current_timestamp
);

CREATE INDEX kontering_id_index ON overforing_konteringer (kontering_id);
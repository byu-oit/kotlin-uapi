
create schema if not exists library;

set schema library;

create table if not exists publisher (
  publisher_id bigint identity primary key not null,
  name varchar(100) not null
);

create table if not exists book (
  oclc bigint not null primary key,
  isbn varchar(30),
  title varchar(255) not null,
  subtitles varchar(255),
  published_year smallint not null,
  publisher bigint not null references publisher(publisher_id),
);

create table if not exists author (
  author_id bigint identity primary key,
  name varchar(255) not null
);

create table if not exists book_authors (
  oclc varchar(13) not null references book(oclc),
  author_id bigint not null references author(author_id),
  primary key (oclc, author_id)
);

create table if not exists genre (
  genre_id bigint identity primary key not null,
  name varchar(255) not null
);

create table if not exists book_genres (
  oclc varchar(13) not null references book(oclc),
  genre_id bigint not null references genre(genre_id),
  primary key (oclc, genre_id)
);

create table if not exists book_copy (
  copy_id bigint identity primary key not null,
  oclc varchar(13) not null references book(oclc)
);

create table if not exists cardholder (
  cardholder_id bigint identity primary key not null,
  name varchar(255) not null
);

create table if not exists checked_out_copy (
  checked_out_copy_id bigint identity primary key not null,
  copy_id bigint not null references book_copy(copy_id),
  cardholder_id bigint not null references cardholder(cardholder_id),
  checked_out_datetime timestamp not null default current_timestamp,
  due_date date not null,
  returned_datetime timestamp,
  reshelved boolean
);


insert into publisher (publisher_id, name)
values (1, 'Simon & Schuster'),
       (2, 'Random House'),
       (3, 'Tor'),
       (4, 'Gnome Press'),
       (5, 'Macmillan'),
       (6, 'Signet'),
       (7, 'HarperCollins')
       ;

insert into book (oclc, isbn, title, subtitles, published_year, publisher)
values (733291011, '978-0451530653', 'The War of the Worlds', null, 1898, 6),
       (35231812, '0-684-83339-5', 'Catch-22', null, 1961, 1),
       (799352269, '978-0-7653-2635-5', 'The Way of Kings', 'Book One of the Stormlight Archive', 2010, 3),
       (889161015, '978-0-7653-2636-2', 'Words of Radiance', 'Book Two of the Stormlight Archive', 2014, 3),
       (969863614, '978-0-7653-2637-9', 'Oathbringer', 'Book Three of the Stormlight Archive', 2018, 3),
       (53896777, '0-553-29335-4', 'Foundation', null, 1951, 4),
       (890303755, '0-333-47110-5', 'The Player of Games', null, 1988, 5),
       (23033258, null, 'Mere Christianity', null, 1952, 7)
       ;

insert into author (AUTHOR_ID, name)
values (1, 'H. G. Wells'),
       (2, 'Joseph Heller'),
       (3, 'Brandon Sanderson'),
       (4, 'Isaac Asimov'),
       (5, 'Iaian M. Banks'),
       (6, 'C. S. Lewis')
       ;

insert into book_authors (oclc, author_id)
 values (733291011, 1),
        (35231812, 2),
        (799352269, 3),
        (889161015, 3),
        (969863614, 3),
        (53896777, 4),
        (890303755, 5),
        (23033258, 6)
        ;

insert into genre (genre_id, name)
values (1, 'Fiction'),
       (2, 'Science Fiction'),
       (3, 'Fantasy'),
       (4, 'Theology'),
       (5, 'Humor')
;

insert into book_genres (oclc, GENRE_ID)
values (733291011, 2),
       (35231812, 1),
       (35231812, 5),
       (799352269, 3),
       (889161015, 3),
       (969863614, 3),
       (53896777, 2),
       (890303755, 2),
       (23033258, 4)
        ;

insert into book_copy(copy_id, oclc)
values (1, 733291011),
       (2, 35231812) ,
       (3, 35231812) ,
       (4, 35231812) ,
       (5, 35231812) ,
       (6, 799352269),
       (7, 799352269),
       (8, 889161015),
       (9, 889161015),
       (10, 969863614),
       (11, 969863614),
       (12, 53896777) ,
       (13, 53896777) ,
       (14, 890303755),
       (15, 890303755),
       (16, 23033258),
       (17, 23033258)
;

insert into cardholder (CARDHOLDER_ID, name)
values (1, 'Joe Student'),
       (2, 'Cosmo Cougar'),
       (3, 'Shallan Davar'),
       (4, 'Lucy Pevensie')
;

insert into checked_out_copy (copy_id, CARDHOLDER_ID, checked_out_datetime, due_date, returned_datetime, reshelved)
values (1, 1, TIMESTAMP '2010-01-01 00:00:00', date '2010-01-15', timestamp '2010-01-14 12:13:45', true),
       (1, 2, timestamp '2013-02-02 10:52:34', date '2013-02-16', timestamp '2013-02-20 14:01:02', true),
       (1, 3,  timestamp '2018-08-30 12:48:03', date '2018-09-13', null, false),
       (2, 3,  timestamp '2018-08-30 12:48:03', date '2018-09-13', null, false),
       (6, 3,  timestamp '2018-08-30 12:48:03', date '2018-09-13', null, false),
       (8, 3,  timestamp '2018-08-30 12:48:03', date '2018-09-13', null, false),
       (10, 3, timestamp '2018-08-30 12:48:03', date '2018-09-13', null, false),
       (12, 3, timestamp '2018-08-30 12:48:03', date '2018-09-13', null, false),
       (15, 3, timestamp '2018-08-30 12:48:03', date '2018-09-13', null, false),
       (16, 3, timestamp '2018-08-30 12:48:03', date '2018-09-13', null, false),
       (17, 4, timestamp '2018-08-28 17:43:12', date '2018-09-11', null, false)
;

commit;


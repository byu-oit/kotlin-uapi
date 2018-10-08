
create schema if not exists library;

set schema library;

create table if not exists publisher (
  publisher_id bigint identity primary key not null,
  name varchar(100) not null
);

create table if not exists book (
  book_id bigint identity not null primary key,
  oclc bigint not null unique,
  isbn varchar(30),
  title varchar(255) not null,
  published_year smallint not null,
  publisher_id bigint not null references publisher(publisher_id),
);

create table if not exists book_subtitles(
  book_id bigint not null references book(book_id),
  subtitle_order int not null,
  subtitle varchar(255) not null,
  primary key (book_id, subtitle_order)
);

create table if not exists author (
  author_id bigint identity primary key,
  name varchar(255) not null
);

create table if not exists book_authors (
  book_id bigint not null references book(book_id),
  author_id bigint not null references author(author_id),
  primary key (book_id, author_id)
);

create table if not exists genre (
  genre_code varchar(5) primary key not null,
  name varchar(255) not null
);

create table if not exists book_genres (
  book_id bigint not null references book(book_id),
  genre_code varchar(5) not null references genre(genre_code),
  primary key (book_id, genre_code)
);

create table if not exists book_copy (
  copy_id bigint identity primary key not null,
  book_id varchar(13) not null references book(book_id)
);

create table if not exists cardholder (
  cardholder_id bigint identity primary key not null,
  net_id varchar2(20) null unique,
  name varchar(255) not null
);

create table if not exists loans (
  loan_id bigint identity primary key not null,
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
       (7, 'HarperCollins'),
       (8, 'Oxford University Press')
       ;

insert into book (book_id, oclc, isbn, title, published_year, publisher_id)
values (1, 733291011, '978-0451530653', 'The War of the Worlds', 1898, 6),
       (2, 35231812, '0-684-83339-5', 'Catch-22', 1961, 1),
       (3, 799352269, '978-0-7653-2635-5', 'The Way of Kings', 2010, 3),
       (4, 889161015, '978-0-7653-2636-2', 'Words of Radiance', 2014, 3),
       (5, 969863614, '978-0-7653-2637-9', 'Oathbringer', 2018, 3),
       (6, 53896777, '0-553-29335-4', 'Foundation', 1951, 4),
       (7, 890303755, '0-333-47110-5', 'The Player of Games', 1988, 5),
       (8, 23033258, null, 'Mere Christianity', 1952, 7),
       (9, 71126670, '0-199-55397-1', 'The Life and Strange Surprizing Adventures of Robinson Crusoe, Of York, Mariner', 1719, 8)
       ;

insert into book_subtitles (book_id, subtitle_order, subtitle)
values (3, 1, 'Book One of the Stormlight Archive'),
       (4, 1, 'Book Two of the Stormlight Archive'),
       (5, 1, 'Book Three of the Stormlight Archive'),
       (9, 1, 'Who lived Eight and Twenty Years, all alone in an un-inhabited Island on the Coast of America, near the Mouth of the Great River of Oroonoque'),
       (9, 2, 'Having been cast on Shore by Shipwreck, wherein all the Men perished but himself'),
       (9, 3, 'With An Account how he was at last as strangely deliver''d by Pyrates'),
       ;

insert into author (AUTHOR_ID, name)
values (1, 'H. G. Wells'),
       (2, 'Joseph Heller'),
       (3, 'Brandon Sanderson'),
       (4, 'Isaac Asimov'),
       (5, 'Iaian M. Banks'),
       (6, 'C. S. Lewis'),
       (7, 'Daniel Defoe')
       ;

insert into book_authors (book_id, author_id)
 values (1, 1),
        (2, 2),
        (3, 3),
        (4, 3),
        (5, 3),
        (6, 4),
        (7, 5),
        (8, 6),
        (9, 7)
        ;

insert into genre (genre_code, name)
values ('FI', 'Fiction'),
       ('SFI', 'Science Fiction'),
       ('FAN', 'Fantasy'),
       ('THEO', 'Theology'),
       ('LOL', 'Humor'),
       ('ADV', 'Adventure'),
       ('HFI', 'Historical Fiction')
;

insert into book_genres (book_id, GENRE_CODE)
values (1, 'SFI'),
       (2, 'FI'),
       (2, 'LOL'),
       (2, 'HFI'),
       (3, 'FAN'),
       (4, 'FAN'),
       (5, 'FAN'),
       (6, 'SFI'),
       (7, 'SFI'),
       (8, 'THEO'),
       (9, 'ADV'),
       (9, 'HFI')
        ;

insert into book_copy(copy_id, book_id)
values (1, 1),
       (2, 2) ,
       (3, 2) ,
       (4, 2) ,
       (5, 2) ,
       (6, 3),
       (7, 3),
       (8, 4),
       (9, 4),
       (10, 5),
       (11, 5),
       (12, 6) ,
       (13, 6) ,
       (14, 7),
       (15, 7),
       (16, 8),
       (17, 8),
       (18, 9)
;

insert into cardholder (CARDHOLDER_ID, net_id, name)
values (1, 'jstudent', 'Joe Student'),
       (2, 'cosmo', 'Cosmo Cougar'),
       (3, 'realshallan', 'Shallan Davar'),
       (4, 'thevaliant', 'Lucy Pevensie')
;

insert into loans (copy_id, CARDHOLDER_ID, checked_out_datetime, due_date, returned_datetime, reshelved)
values (1,  1, TIMESTAMP '2010-01-01 00:00:00', date '2010-01-15', timestamp '2010-01-14 12:13:45', true),
       (1,  2, timestamp '2013-02-02 10:52:34', date '2013-02-16', timestamp '2013-02-20 14:01:02', true),
       (1,  3,  timestamp '2018-08-30 12:48:03', date '2018-09-13', null, false),
       (2,  3,  timestamp '2018-08-30 12:48:03', date '2018-09-13', null, false),
       (6,  3,  timestamp '2018-08-30 12:48:03', date '2018-09-13', null, false),
       (8,  3,  timestamp '2018-08-30 12:48:03', date '2018-09-13', null, false),
       (10, 3, timestamp '2018-08-30 12:48:03', date '2018-09-13', null, false),
       (12, 3, timestamp '2018-08-30 12:48:03', date '2018-09-13', null, false),
       (15, 3, timestamp '2018-08-30 12:48:03', date '2018-09-13', null, false),
       (16, 3, timestamp '2018-08-30 12:48:03', date '2018-09-13', null, false),
       (17, 4, timestamp '2018-08-28 17:43:12', date '2018-09-11', null, false),
       (18, 4, timestamp '2018-08-28 17:43:12', date '2018-09-11', null, false)
;

commit;


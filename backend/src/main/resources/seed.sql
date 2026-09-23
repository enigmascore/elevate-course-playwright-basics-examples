-- The seed EVERY fresh backend starts from ( e2e profile: create-drop + this file ).
-- The course pages show this file, so the student knows exactly what "the seeded
-- books" are. Passwords: alice@example.com / bookworm, bob@example.com / pageturner.

insert into users ( id, name, email, password_hash, activated )
values ( 1, 'Alice', 'alice@example.com',
         '$2y$10$EfUl7lGX19hdS1IZW3i45ezMu6B4rc5O0lwoJHMUmzdK27kmY5nmK', true ),
       ( 2, 'Bob', 'bob@example.com',
         '$2y$10$uRB9aDwZYWl5K/98yvZbMuWZftl0zlGD.fpgo9J3/UtmsjdEohOaO', true )
on conflict ( id ) do nothing;

insert into books ( id, title, author, genre, already_read )
values ( 1, 'The Hobbit', 'J. R. R. Tolkien', 'Fiction', true ),
       ( 2, 'A Brief History of Time', 'Stephen Hawking', 'Science', false ),
       ( 3, 'The Silk Roads', 'Peter Frankopan', 'History', false ),
       ( 4, 'Matilda', 'Roald Dahl', 'Children', true ),
       ( 5, 'Sapiens', 'Yuval Noah Harari', 'Non-fiction', false ),
       ( 6, 'Dune', 'Frank Herbert', 'Fiction', false )
on conflict ( id ) do nothing;

insert into loans ( id, book_title, borrower, due_date )
values ( 1, 'The Hobbit', 'Carol', '2026-10-01' ),
       ( 2, 'Dune', 'Dave', '2026-10-08' ),
       ( 3, 'Matilda', 'Erin', '2026-10-15' ),
       ( 4, 'Sapiens', 'Frank', '2026-10-22' )
on conflict ( id ) do nothing;

-- keep the id sequences ahead of the seeded rows
select setval( 'users_seq', 100 );
select setval( 'books_seq', 100 );
select setval( 'loans_seq', 100 );

# Bookshelf - the Basic Playwright course's example app

A small, WORKING full-stack web application and the Playwright suite that
tests it. The **Basic Playwright** course teaches from this repository:
every code snippet on a course page is an excerpt of a file in here, and
links to it. You clone this repository and run it; you never submit
anything from it - the graded work happens in your own copy of the
Recipe Box template ( the course tells you when ).

## What Bookshelf does

Register with an activation email, log in, forgotten-password email, a
books list with server-side search, an "Add book" form ( with an optional
cover image ), a delete-confirm modal, a logout confirm, and a loans page
that loads slowly on purpose. Every one of those exists so that a test can
be written against it.

| Part          | Where       | How it runs                                   |
| ------------- | ----------- | --------------------------------------------- |
| Front end     | `frontend/` | Vite + React + TypeScript, on your machine     |
| Backend       | `backend/`  | Java Spring Boot, in a docker container         |
| Database      | docker      | Postgres 17                                    |
| Email         | docker      | MailHog - every email the app sends lands here |
| The tests     | `e2e/`      | Playwright, on your machine                    |

You never need Java on your machine: the backend is compiled and run
inside docker. You never need to READ `frontend/` or `backend/` either -
you run the app and you test it.

## Prerequisites

- **Docker Desktop**, running.
- **nvm** and **Node 24**: `nvm install` in this folder reads `.nvmrc`.
- **pnpm**: `npm install -g pnpm`.

## Run it

```
git clone https://github.com/enigmascore/elevate-course-playwright-basics-examples.git
cd elevate-course-playwright-basics-examples
nvm use
make docker-up     # Postgres + MailHog, and BUILDS the backend image ( a few minutes the first time )
make install       # pnpm install in e2e/ and frontend/, then Playwright's Chromium
make test          # the whole Playwright suite, headless
make report        # open the HTML report of that run
```

`make test` starts the front end if it is not already running, and each
test suite starts and stops its own backend container - so you never start
the backend by hand. MailHog's inbox is at http://localhost:8027.

### Exploring the app yourself

```
make dev           # backend on the dev profile ( data survives restarts ) + the front end
```

Open http://localhost:5173 and log in as one of the seeded users. When you
are done exploring, run **`make dev-stop`** before the next `make test`:
the tests refuse to start while a backend is already running on port 8086,
because each suite needs its own fresh one.

## Seeded users and data

`backend/src/main/resources/seed.sql` is loaded into EVERY fresh backend
the tests start:

| Email               | Password     |
| ------------------- | ------------ |
| alice@example.com   | `bookworm`   |
| bob@example.com     | `pageturner` |

Six books, four loans. Both users are already activated.

## The make targets

| Target                    | What it does                                                        |
| ------------------------- | ------------------------------------------------------------------- |
| `make docker-up`          | start Postgres + MailHog; build the backend image ( not started )   |
| `make docker-down`        | stop and remove the containers ( the database volume is kept )      |
| `make install`            | dependencies for `e2e/` and `frontend/`, plus Chromium              |
| `make test`               | the Playwright suite, headless                                      |
| `make test-headed`        | the same with the browser visible ( `SLOWMO=500 make test-headed` ) |
| `make test-ui`            | Playwright's UI mode                                                |
| `make report`             | open the last HTML report                                           |
| `make dev` / `dev-stop`   | run / stop the app for manual exploration                           |

Course-author targets ( `backend-test-unit`, `backend-test-integration`,
`backend-verify` ) run the backend's own JUnit tests and need a JDK 25 on
the host. Students never run them.

## The map

```
docker-compose.yml   postgres ( 5435 ), mailhog ( 1027 / 8027 ), backend ( 8086 )
backend/             Spring Boot app + its unit tests ( *Test ) and integration tests ( *IT )
frontend/            Vite + React + TypeScript app on 5173; /api is proxied to the backend
e2e/                 the Playwright suite - see e2e/README.md
```

## Bookshelf and Recipe Box

The course's graded task uses a second, smaller app of the same shape:
Recipe Box, in the `elevate-course-playwright-basics` template. Both apps
use the same ports, so run one at a time - `make docker-down` here before
`make docker-up` there, and vice versa.

import { type FormEvent, useCallback, useEffect, useRef, useState } from "react";

import { api, ApiError, type Book, type FieldErrors } from "../api";
import FieldError from "../components/FieldError";

const GENRES = ["Fiction", "Non-fiction", "Science", "History", "Children"];

// mirrors the backend's BookValidator rules
function validate(title: string, author: string, genre: string, cover: File | null): FieldErrors {
  const errors: FieldErrors = {};
  if (!title.trim()) errors.title = "Title is required";
  if (!author.trim()) errors.author = "Author is required";
  if (!genre) errors.genre = "Choose a genre";
  if (cover && !["image/png", "image/jpeg"].includes(cover.type)) {
    errors.cover = "Cover must be a PNG or JPEG image";
  }
  return errors;
}

export default function BooksPage() {
  const [query, setQuery] = useState("");
  const [books, setBooks] = useState<Book[]>([]);
  const latestRequest = useRef(0);

  const [title, setTitle] = useState("");
  const [author, setAuthor] = useState("");
  const [genre, setGenre] = useState("");
  const [alreadyRead, setAlreadyRead] = useState(false);
  const [cover, setCover] = useState<File | null>(null);
  const coverInput = useRef<HTMLInputElement>(null);
  const [errors, setErrors] = useState<FieldErrors>({});
  const [notice, setNotice] = useState<string | null>(null);

  const [toDelete, setToDelete] = useState<Book | null>(null);

  // server-side search: every keystroke is a real GET /api/books?q=...; the
  // counter drops answers that arrive out of order
  const load = useCallback(async (q: string) => {
    const requestNumber = ++latestRequest.current;
    const result = await api.books(q);
    if (requestNumber === latestRequest.current) setBooks(result);
  }, []);

  useEffect(() => {
    void load(query);
  }, [query, load]);

  async function addBook(event: FormEvent) {
    event.preventDefault();
    setNotice(null);
    const clientErrors = validate(title, author, genre, cover);
    setErrors(clientErrors);
    if (Object.keys(clientErrors).length > 0) return;

    const form = new FormData();
    form.append("title", title);
    form.append("author", author);
    form.append("genre", genre);
    form.append("alreadyRead", String(alreadyRead));
    if (cover) form.append("cover", cover);

    try {
      const created = await api.addBook(form);
      setTitle("");
      setAuthor("");
      setGenre("");
      setAlreadyRead(false);
      setCover(null);
      if (coverInput.current) coverInput.current.value = "";
      setNotice(`Added "${created.title}"`);
      await load(query);
    } catch (e) {
      if (e instanceof ApiError && e.errors) setErrors(e.errors);
      else setErrors({ form: "Something went wrong - please try again" });
    }
  }

  async function confirmDelete() {
    if (!toDelete) return;
    await api.deleteBook(toDelete.id);
    setToDelete(null);
    await load(query);
  }

  return (
    <>
      <h1>Books</h1>

      <div className="field">
        <label htmlFor="search">Search</label>
        <input
          id="search"
          type="search"
          placeholder="Title or author"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
        />
      </div>

      <table>
        <thead>
          <tr>
            <th>Cover</th>
            <th>Title</th>
            <th>Author</th>
            <th>Genre</th>
            <th>Read</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          {books.map((book) => (
            <tr key={book.id} data-testid="book-row">
              <td>
                {book.hasCover ? (
                  <img className="cover" src={`/api/books/${book.id}/cover`} alt={`Cover of ${book.title}`} />
                ) : (
                  <span className="muted">-</span>
                )}
              </td>
              <td>{book.title}</td>
              <td>{book.author}</td>
              <td>{book.genre}</td>
              <td>{book.alreadyRead ? "Yes" : "No"}</td>
              <td>
                <button
                  type="button"
                  className="secondary"
                  aria-label={`Delete ${book.title}`}
                  onClick={() => setToDelete(book)}
                >
                  Delete
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      {books.length === 0 && <p className="muted">No books match.</p>}

      <h2>Add book</h2>
      <form className="card" onSubmit={addBook} noValidate>
        <div className="field">
          <label htmlFor="title">Title</label>
          <input id="title" type="text" value={title} onChange={(e) => setTitle(e.target.value)} aria-describedby="title-error" />
          <FieldError id="title-error" message={errors.title} />
        </div>
        <div className="field">
          <label htmlFor="author">Author</label>
          <input id="author" type="text" value={author} onChange={(e) => setAuthor(e.target.value)} aria-describedby="author-error" />
          <FieldError id="author-error" message={errors.author} />
        </div>
        <div className="field">
          <label htmlFor="genre">Genre</label>
          <select id="genre" value={genre} onChange={(e) => setGenre(e.target.value)} aria-describedby="genre-error">
            <option value="">Choose...</option>
            {GENRES.map((g) => (
              <option key={g} value={g}>
                {g}
              </option>
            ))}
          </select>
          <FieldError id="genre-error" message={errors.genre} />
        </div>
        <div className="field inline">
          <input id="already-read" type="checkbox" checked={alreadyRead} onChange={(e) => setAlreadyRead(e.target.checked)} />
          <label htmlFor="already-read">Already read</label>
        </div>
        <div className="field">
          <label htmlFor="cover">Cover image</label>
          <input
            id="cover"
            ref={coverInput}
            type="file"
            accept="image/png,image/jpeg"
            onChange={(e) => setCover(e.target.files?.[0] ?? null)}
            aria-describedby="cover-error"
          />
          <FieldError id="cover-error" message={errors.cover} />
        </div>
        <FieldError id="form-error" message={errors.form} />
        {notice && <p className="notice">{notice}</p>}
        <button type="submit">Add book</button>
      </form>

      {toDelete && (
        <div className="backdrop">
          <div className="modal" role="dialog" aria-modal="true" aria-labelledby="delete-title">
            <h2 id="delete-title">Delete book?</h2>
            <p>Remove &ldquo;{toDelete.title}&rdquo; from your shelf?</p>
            <div className="actions">
              <button type="button" className="secondary" onClick={() => setToDelete(null)}>
                Cancel
              </button>
              <button type="button" className="danger" onClick={confirmDelete}>
                Delete
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
}

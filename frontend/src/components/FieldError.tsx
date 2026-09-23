// One line of red text under a field; `id` lets the input point at it with aria-describedby.
export default function FieldError({ id, message }: { id: string; message?: string }) {
  if (!message) return null;
  return (
    <p className="error" id={id} role="alert">
      {message}
    </p>
  );
}

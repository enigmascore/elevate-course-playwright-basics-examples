// Helpers for the MailHog HTTP API ( http://localhost:8027 ). Bookshelf sends
// email ASYNCHRONOUSLY - a request queues a job and a scheduled sender delivers
// it a moment later - so tests must POLL for a message, never assert immediately.

const MAILHOG_URL = "http://localhost:8027";

export type MailhogMessage = {
  ID: string;
  Content: {
    Headers: Record<string, string[]>;
    Body: string;
  };
};

type SearchResult = { total: number; items: MailhogMessage[] };

export async function clearMessages(): Promise<void> {
  const response = await fetch(`${MAILHOG_URL}/api/v1/messages`, { method: "DELETE" });
  if (!response.ok) {
    throw new Error(
      `MailHog DELETE /api/v1/messages failed ( ${response.status} ) - is MailHog up? make docker-up`,
    );
  }
}

export async function waitForMessageTo(
  email: string,
  options: { subjectContains?: string; timeoutMs?: number } = {},
): Promise<MailhogMessage> {
  const { subjectContains, timeoutMs = 30_000 } = options;
  const deadline = Date.now() + timeoutMs;

  while (Date.now() < deadline) {
    const response = await fetch(`${MAILHOG_URL}/api/v2/search?kind=to&query=${encodeURIComponent(email)}`);
    if (response.ok) {
      const result = (await response.json()) as SearchResult;
      const match = result.items.find(
        (message) =>
          !subjectContains ||
          (message.Content.Headers.Subject ?? []).some((subject) => subject.includes(subjectContains)),
      );
      if (match) return match;
    }
    await new Promise((resolve) => setTimeout(resolve, 500));
  }

  const subjectNote = subjectContains ? ` with subject containing "${subjectContains}"` : "";
  throw new Error(
    `No email to ${email}${subjectNote} after ${timeoutMs / 1000}s - the sender runs once a ` +
      `second; check the inbox at ${MAILHOG_URL} and e2e/test-results/backend.log`,
  );
}

// A mail body MAY be quoted-printable encoded ( the header says so ): soft line
// breaks ( =\r\n ) split long urls and every '=' becomes =3D. Decode ONLY when
// the message declares that encoding - a plain body with "?token=fa5..." in it
// would otherwise have its "=fa" mangled into a character.
function decodedBody(message: MailhogMessage): string {
  const encoding = (message.Content.Headers["Content-Transfer-Encoding"] ?? []).join("").toLowerCase();
  const body = message.Content.Body;
  if (!encoding.includes("quoted-printable")) return body;
  return body
    .replace(/=\r?\n/g, "")
    .replace(/=([0-9A-F]{2})/gi, (_, hex: string) => String.fromCharCode(parseInt(hex, 16)));
}

export function extractLink(message: MailhogMessage, pathFragment: string): string {
  const body = decodedBody(message);
  const link = body.match(/https?:\/\/[^\s"'<>]+/g)?.find((url) => url.includes(pathFragment));
  if (!link) {
    throw new Error(`No link containing "${pathFragment}" in the email body. Decoded body:\n${body}`);
  }
  return link;
}

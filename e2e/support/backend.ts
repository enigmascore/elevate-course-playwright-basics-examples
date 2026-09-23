import { type ChildProcess, spawn, spawnSync } from "node:child_process";
import fs from "node:fs";
import path from "node:path";

// Start / stop the backend CONTAINER. Every start is a fresh Spring context and
// - because the e2e profile is create-drop + seed.sql - a fresh database holding
// exactly the seed. The image is built by `make docker-up`; this file only
// starts and stops it.

const BACKEND_URL = "http://localhost:8086";
// answers 200 without a JWT once the context is up
const HEALTH_PATH = "/api/health";
const READY_TIMEOUT_MS = 120_000;
// the repository root, where docker-compose.yml lives
const COMPOSE_DIR = path.resolve(import.meta.dirname, "../..");
// the container's full output lands here ( overwritten per suite ) so a 500 seen
// in the browser is diagnosable without re-running anything
const BACKEND_LOG = path.resolve(import.meta.dirname, "../test-results/backend.log");

let logFollower: ChildProcess | undefined;
let logTail = "";

function compose(...args: string[]): string {
  const result = spawnSync("docker", ["compose", ...args], { cwd: COMPOSE_DIR, encoding: "utf8" });
  if (result.status !== 0) {
    throw new Error(`docker compose ${args.join(" ")} failed:\n${result.stderr || result.stdout}`);
  }
  return result.stdout;
}

async function isReady(): Promise<boolean> {
  try {
    return (await fetch(`${BACKEND_URL}${HEALTH_PATH}`)).ok;
  } catch {
    return false;
  }
}

function containerHasExited(): boolean {
  const state = compose("ps", "--all", "--format", "{{.State}}", "backend").trim();
  return state === "exited" || state === "dead";
}

export async function startBackend(): Promise<void> {
  if (await isReady()) {
    throw new Error(
      `Something is already answering on ${BACKEND_URL} - a backend from an earlier run, or ` +
        `\`make dev\`? Run \`make dev-stop\` first: every suite needs its own fresh backend.`,
    );
  }

  const startedAt = new Date().toISOString();
  compose("up", "-d", "backend");

  // follow the container's log into a file, keeping the tail for error messages
  fs.mkdirSync(path.dirname(BACKEND_LOG), { recursive: true });
  const logStream = fs.createWriteStream(BACKEND_LOG);
  logTail = "";
  logFollower = spawn(
    "docker",
    ["compose", "logs", "--follow", "--no-log-prefix", "--since", startedAt, "backend"],
    { cwd: COMPOSE_DIR, stdio: ["ignore", "pipe", "pipe"] },
  );
  const capture = (chunk: Buffer) => {
    logStream.write(chunk);
    logTail = (logTail + chunk.toString()).slice(-4000);
  };
  logFollower.stdout?.on("data", capture);
  logFollower.stderr?.on("data", capture);

  const deadline = Date.now() + READY_TIMEOUT_MS;
  while (Date.now() < deadline) {
    if (await isReady()) return;
    if (containerHasExited()) {
      await stopBackend();
      throw new Error(`The backend container exited during startup:\n${logTail}`);
    }
    await new Promise((resolve) => setTimeout(resolve, 1000));
  }

  await stopBackend();
  throw new Error(`Backend not ready on ${BACKEND_URL} after ${READY_TIMEOUT_MS / 1000}s:\n${logTail}`);
}

export async function stopBackend(): Promise<void> {
  logFollower?.kill();
  logFollower = undefined;
  compose("stop", "backend");
}

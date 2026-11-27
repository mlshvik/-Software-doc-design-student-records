import type { ProblemDetail } from './types';

const TOKEN_KEY = 'student-records.token';

/** Помилка запиту до API з даними з тіла відповіді (Problem Details). */
export class ApiError extends Error {
  readonly status: number;
  readonly problem: ProblemDetail;

  constructor(status: number, problem: ProblemDetail) {
    super(problem.detail || problem.title || `Помилка ${status}`);
    this.status = status;
    this.problem = problem;
  }

  /** Помилки валідації окремих полів форми. */
  get fieldErrors(): Record<string, string> {
    return this.problem.errors ?? {};
  }
}

export const tokenStorage = {
  get(): string | null {
    try {
      return localStorage.getItem(TOKEN_KEY);
    } catch {
      return null;
    }
  },
  set(token: string | null): void {
    try {
      if (token) localStorage.setItem(TOKEN_KEY, token);
      else localStorage.removeItem(TOKEN_KEY);
    } catch {
      /* сховище недоступне – токен житиме лише до перезавантаження */
    }
  },
};

let unauthorizedHandler: (() => void) | null = null;

/** Викликається, коли сервер повернув 401 (токен прострочений) – AuthContext розлогінює користувача. */
export function onUnauthorized(handler: (() => void) | null): void {
  unauthorizedHandler = handler;
}

type Query = Record<string, string | number | boolean | null | undefined>;

function buildUrl(path: string, query?: Query): string {
  const params = new URLSearchParams();
  Object.entries(query ?? {}).forEach(([key, value]) => {
    if (value !== null && value !== undefined && value !== '') params.append(key, String(value));
  });
  const qs = params.toString();
  return `/api${path}${qs ? `?${qs}` : ''}`;
}

async function request<T>(method: string, path: string, body?: unknown, query?: Query): Promise<T> {
  const headers: Record<string, string> = { Accept: 'application/json' };
  const token = tokenStorage.get();
  if (token) headers.Authorization = `Bearer ${token}`;
  if (body !== undefined) headers['Content-Type'] = 'application/json';

  let response: Response;
  try {
    response = await fetch(buildUrl(path, query), {
      method,
      headers,
      body: body !== undefined ? JSON.stringify(body) : undefined,
    });
  } catch {
    throw new ApiError(0, { detail: 'Сервер недоступний. Перевірте, чи запущено бекенд.' });
  }

  if (response.status === 401 && path !== '/auth/login') {
    unauthorizedHandler?.();
  }
  if (!response.ok) {
    let problem: ProblemDetail = { status: response.status };
    try {
      problem = { ...problem, ...(await response.json()) };
    } catch {
      /* тіло відповіді не JSON */
    }
    throw new ApiError(response.status, problem);
  }
  if (response.status === 204) return undefined as T;
  const text = await response.text();
  return (text ? JSON.parse(text) : undefined) as T;
}

export const api = {
  get: <T>(path: string, query?: Query) => request<T>('GET', path, undefined, query),
  post: <T>(path: string, body?: unknown, query?: Query) => request<T>('POST', path, body ?? {}, query),
  put: <T>(path: string, body: unknown) => request<T>('PUT', path, body),
  patch: <T>(path: string, body?: unknown) => request<T>('PATCH', path, body ?? {}),
  delete: (path: string) => request<void>('DELETE', path),
};

// Общие утилиты Edge Functions
export const cors = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
};

export function json(status: number, body: unknown): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { ...cors, "Content-Type": "application/json" },
  });
}

// единый формат ошибок: { "error": { "code": "...", "message": "..." } }
export function fail(status: number, code: string, message: string): Response {
  return json(status, { error: { code, message } });
}

// идентификаторы в базе — целые числа (int4)
export const isId = (v: unknown): v is number =>
  typeof v === "number" && Number.isInteger(v) && v > 0 && v <= 2147483647;

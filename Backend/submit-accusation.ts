// POST /functions/v1/submit-accusation — финальное обвинение
import { createClient } from "https://esm.sh/@supabase/supabase-js@2";
// Общие утилиты Edge Functions
const cors = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
};

function json(status: number, body: unknown): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { ...cors, "Content-Type": "application/json" },
  });
}

// единый формат ошибок: { "error": { "code": "...", "message": "..." } }
function fail(status: number, code: string, message: string): Response {
  return json(status, { error: { code, message } });
}

// идентификаторы в базе — целые числа (int4)
const isId = (v: unknown): v is number =>
  typeof v === "number" && Number.isInteger(v) && v > 0 && v <= 2147483647;


Deno.serve(async (req) => {
  if (req.method === "OPTIONS") return new Response("ok", { headers: cors });
  if (req.method !== "POST") return fail(405, "method_not_allowed", "Используйте POST");

  const authHeader = req.headers.get("Authorization");
  if (!authHeader) return fail(401, "unauthorized", "Нужен токен: заголовок Authorization");

  const url = Deno.env.get("SUPABASE_URL")!;
  const user = createClient(url, Deno.env.get("SUPABASE_ANON_KEY")!, { global: { headers: { Authorization: authHeader } } });
  const admin = createClient(url, Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!);

  const { data: u, error: uErr } = await user.auth.getUser();
  if (uErr || !u.user) return fail(401, "unauthorized", "Токен недействителен или истёк");

  let body: { investigation_id?: unknown; suspect_id?: unknown };
  try { body = await req.json(); } catch { return fail(400, "validation_error", "Тело запроса не JSON"); }
  if (!isId(body.investigation_id) || !isId(body.suspect_id)) {
    return fail(400, "validation_error", "Нужны investigation_id и suspect_id (целые числа)");
  }

  const { data: inv } = await user.from("investigations").select("id, case_id, status").eq("id", body.investigation_id).maybeSingle();
  if (!inv) return fail(404, "not_found", "Расследование не найдено");
  if (inv.status !== "in_progress") return fail(409, "already_accused", "Обвинение уже выдвинуто");

  const { data: suspect } = await user.from("suspects").select("id, name").eq("id", body.suspect_id).eq("case_id", inv.case_id).maybeSingle();
  if (!suspect) return fail(404, "not_found", "Подозреваемый не найден в этом деле");

  // кто на самом деле виновен — знает только сервер
  const { data: culprit } = await admin.from("suspects_secret")
    .select("truth, suspects!inner(id, name, case_id)").eq("is_culprit", true).eq("suspects.case_id", inv.case_id).single();
  const real = culprit!.suspects as unknown as { id: number; name: string };
  const isCorrect = real.id === suspect.id;
  const explanation = isCorrect
    ? `Верно! ${real.name} — преступник. ${culprit!.truth}`
    : `Это ошибка. Преступником был ${real.name}.`;

  const { error: aErr } = await admin.from("accusations").insert({
    investigation_id: inv.id, suspect_id: suspect.id, is_correct: isCorrect, explanation,
  });
  if (aErr) return fail(aErr.code === "23505" ? 409 : 500, aErr.code === "23505" ? "already_accused" : "internal_error", "Не удалось сохранить обвинение");

  await admin.from("investigations").update({ status: isCorrect ? "solved" : "failed", finished_at: new Date().toISOString() }).eq("id", inv.id);
  return json(200, { is_correct: isCorrect, explanation });
});

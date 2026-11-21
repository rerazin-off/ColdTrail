// POST сохраняет обвинение, GET возвращает сохранённый итог владельцу.
import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

// Общие утилиты Edge Functions
const cors = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers":
    "authorization, x-client-info, apikey, content-type",
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

interface AccusationResult {
  isCorrect: boolean;
  explanation: string;
}

function calculateAccusation(
  realSuspectId: number,
  selectedSuspectId: number,
  realSuspectName: string,
  truth: string,
): AccusationResult {
  const isCorrect = realSuspectId === selectedSuspectId;

  const explanation = isCorrect
    ? `Верно! ${realSuspectName} — преступник. ${truth}`
    : `Это ошибка. Преступником был ${realSuspectName}. ${truth}`;

  return {
    isCorrect,
    explanation,
  };
}

Deno.serve(async (req) => {
  if (req.method === "OPTIONS") return new Response("ok", { headers: cors });
  if (req.method !== "POST" && req.method !== "GET") {
    return fail(405, "method_not_allowed", "Используйте GET или POST");
  }

  const authHeader = req.headers.get("Authorization");
  if (!authHeader) {
    return fail(401, "unauthorized", "Нужен токен: заголовок Authorization");
  }

  const url = Deno.env.get("SUPABASE_URL")!;
  const user = createClient(url, Deno.env.get("SUPABASE_ANON_KEY")!, {
    global: { headers: { Authorization: authHeader } },
  });
  const admin = createClient(url, Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!);

  const { data: u, error: uErr } = await user.auth.getUser();
  if (uErr || !u.user) {
    return fail(401, "unauthorized", "Токен недействителен или истёк");
  }

  const reading = req.method === "GET";
  let body: { investigation_id?: unknown; suspect_id?: unknown };
  try {
    body = reading
      ? { investigation_id: Number(new URL(req.url).searchParams.get("investigation_id")) }
      : await req.json();
  } catch {
    return fail(400, "validation_error", "Тело запроса не JSON");
  }
  if (!body || !isId(body.investigation_id) || (!reading && !isId(body.suspect_id))) {
    return fail(400, "validation_error", "Некорректные идентификаторы обвинения");
  }
  const { data: inv } = await user.from("investigations").select(
    "id, case_id, status",
  ).eq("id", body.investigation_id).maybeSingle();
  if (!inv) return fail(404, "not_found", "Расследование не найдено");
  const findSaved = () => user.from("accusations")
    .select("id, suspect_id, is_correct, explanation")
    .eq("investigation_id", inv.id).maybeSingle();
  const saved = await findSaved();
  if (saved.error) return fail(500, "internal_error", "Не удалось прочитать обвинение");
  if (reading && !saved.data) return fail(404, "not_found", "Обвинение ещё не выдвинуто");
  if (!saved.data && inv.status !== "in_progress") {
    return fail(409, "already_accused", "Расследование уже завершено");
  }

  // Ответ доступен только владельцу расследования и только после сохранения обвинения.
  const { data: culprit, error: culpritError } = await admin.from("suspects_secret")
    .select("truth, suspects!inner(id, name, case_id)").eq("is_culprit", true)
    .eq("suspects.case_id", inv.case_id).single();
  if (culpritError || !culprit) return fail(500, "internal_error", "Итог дела недоступен");
  const real = culprit.suspects as unknown as { id: number; name: string };
  const respondWithResult = async (accusation: {
    id: number;
    suspect_id: number;
    is_correct: boolean;
    explanation: string;
  }) => {
    if (!reading && inv.status === "in_progress") {
      const { error } = await admin.from("investigations").update({
        status: accusation.is_correct ? "solved" : "failed",
        finished_at: new Date().toISOString(),
      }).eq("id", inv.id).eq("status", "in_progress");
      if (error) return fail(500, "internal_error", "Не удалось завершить расследование");
    }
    return json(200, { ...accusation, culprit_id: real.id });
  };
  if (saved.data) return respondWithResult(saved.data);

  const { data: suspect } = await user.from("suspects").select("id")
    .eq("id", body.suspect_id).eq("case_id", inv.case_id).maybeSingle();
  if (!suspect) return fail(404, "not_found", "Подозреваемый не найден в этом деле");

  const result = calculateAccusation(real.id, suspect.id, real.name, culprit.truth);
  const { data: accusation, error: aErr } = await admin.from("accusations").insert({
    investigation_id: inv.id,
    suspect_id: suspect.id,
    is_correct: result.isCorrect,
    explanation: result.explanation,
  }).select("id, suspect_id, is_correct, explanation").single();
  if (aErr?.code === "23505") {
    const retry = await findSaved();
    if (retry.data) return respondWithResult(retry.data);
  }
  if (aErr || !accusation) return fail(500, "internal_error", "Не удалось сохранить обвинение");

  return respondWithResult(accusation);
});

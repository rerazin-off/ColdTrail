// POST /functions/v1/npc-chat — реплика подозреваемого от языковой модели
// Секреты (GROQ_API_KEY, GEMINI_API_KEY) хранятся только на сервере: supabase secrets set ...
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

const DAILY_LIMIT = 30; // ответов ИИ на игрока в сутки
const MAX_TEXT = 300; // длина вопроса игрока
const HISTORY = 12; // сколько последних реплик помнит персонаж

type Msg = { role: "system" | "user" | "assistant"; content: string };

async function askGroq(messages: Msg[]): Promise<string> {
  const r = await fetch("https://api.groq.com/openai/v1/chat/completions", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${Deno.env.get("GROQ_API_KEY")}`,
    },
    body: JSON.stringify({
      model: "llama-3.3-70b-versatile",
      messages,
      temperature: 0.8,
      max_tokens: 300,
    }),
    signal: AbortSignal.timeout(15000),
  });
  if (!r.ok) throw new Error(`groq ${r.status}`);
  const data = await r.json();
  return data.choices[0].message.content.trim();
}

async function askGemini(messages: Msg[]): Promise<string> {
  const model = Deno.env.get("GEMINI_MODEL") ?? "gemini-2.0-flash";
  const system = messages.find((m) => m.role === "system")!.content;
  const contents = messages.filter((m) => m.role !== "system").map((m) => ({
    role: m.role === "assistant" ? "model" : "user",
    parts: [{ text: m.content }],
  }));
  const r = await fetch(
    `https://generativelanguage.googleapis.com/v1beta/models/${model}:generateContent?key=${
      Deno.env.get("GEMINI_API_KEY")
    }`,
    {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        systemInstruction: { parts: [{ text: system }] },
        contents,
        generationConfig: { temperature: 0.8, maxOutputTokens: 300 },
      }),
      signal: AbortSignal.timeout(15000),
    },
  );
  if (!r.ok) throw new Error(`gemini ${r.status}`);
  const data = await r.json();
  return data.candidates[0].content.parts[0].text.trim();
}

Deno.serve(async (req) => {
  if (req.method === "OPTIONS") return new Response("ok", { headers: cors });
  if (req.method !== "POST") {
    return fail(405, "method_not_allowed", "Используйте POST");
  }

  const authHeader = req.headers.get("Authorization");
  if (!authHeader) {
    return fail(401, "unauthorized", "Нужен токен: заголовок Authorization");
  }

  const url = Deno.env.get("SUPABASE_URL")!;
  // клиент от имени игрока: все запросы проходят через RLS
  const user = createClient(url, Deno.env.get("SUPABASE_ANON_KEY")!, {
    global: { headers: { Authorization: authHeader } },
  });
  // клиент с правами сервера: только для секретов, лимита и записи реплик
  const admin = createClient(url, Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!);

  const { data: u, error: uErr } = await user.auth.getUser();
  if (uErr || !u.user) {
    return fail(401, "unauthorized", "Токен недействителен или истёк");
  }

  let body: {
    investigation_id?: unknown;
    suspect_id?: unknown;
    text?: unknown;
    clue_id?: unknown;
  };
  try {
    body = await req.json();
  } catch {
    return fail(400, "validation_error", "Тело запроса не JSON");
  }
  const text = typeof body.text === "string" ? body.text.trim() : "";
  if (
    !isId(body.investigation_id) || !isId(body.suspect_id) || text.length < 1 ||
    text.length > MAX_TEXT ||
    (body.clue_id !== undefined && !isId(body.clue_id))
  ) {
    return fail(
      400,
      "validation_error",
      `Нужны investigation_id, suspect_id (целые числа) и text длиной 1–${MAX_TEXT}`,
    );
  }

  // расследование видно только владельцу (RLS)
  const { data: inv } = await user.from("investigations").select(
    "id, case_id, status",
  ).eq("id", body.investigation_id).maybeSingle();
  if (!inv) return fail(404, "not_found", "Расследование не найдено");
  if (inv.status !== "in_progress") {
    return fail(409, "investigation_closed", "Расследование уже завершено");
  }

  const { data: suspect } = await user.from("suspects").select(
    "id, name, profile",
  ).eq("id", body.suspect_id).eq("case_id", inv.case_id).maybeSingle();
  if (!suspect) {
    return fail(404, "not_found", "Подозреваемый не найден в этом деле");
  }

  // предъявленная улика должна лежать в инвентаре игрока
  let clueNote = "";
  if (body.clue_id) {
    const { data: cc } = await user.from("collected_clues").select(
      "clues(title, description)",
    )
      .eq("investigation_id", inv.id).eq("clue_id", body.clue_id).maybeSingle();
    if (!cc) return fail(422, "clue_not_collected", "Эта улика ещё не найдена");
    const c = cc.clues as unknown as { title: string; description: string };
    clueNote =
      `\nИгрок предъявляет улику «${c.title}»: ${c.description}. Отреагируй на неё в образе.`;
  }

  // лимит ответов в сутки: считаем реплики подозреваемых за сегодня в dialogue_messages
  const { data: used } = await admin.rpc("npc_replies_today", {
    p_user: u.user.id,
  });
  if ((used ?? 0) >= DAILY_LIMIT) {
    return fail(
      429,
      "daily_limit",
      `Лимит ${DAILY_LIMIT} ответов в сутки исчерпан. Попробуйте завтра`,
    );
  }

  // секреты читает только сервер: persona_prompt (suspects) и truth (suspects_secret)
  const { data: persona } = await admin.from("suspects").select(
    "persona_prompt",
  ).eq("id", suspect.id).single();
  const { data: secret } = await admin.from("suspects_secret").select("truth")
    .eq("id_suspect", suspect.id).single();
  const { data: past } = await admin.from("dialogue_messages").select(
    "author, text",
  )
    .eq("investigation_id", inv.id).eq("suspect_id", suspect.id).order("id", {
      ascending: false,
    }).limit(HISTORY);

  const system = `${persona!.persona_prompt}\n` +
    `Тебя зовут ${suspect.name}. Открытое описание: ${suspect.profile}\n` +
    `Правда о случившемся (знаешь только ты, игроку не раскрывай прямо): ${
      secret!.truth
    }\n` +
    `Правила: отвечай по-русски, 1–3 предложения, не выходи из роли. Ты можешь лгать, путаться и менять показания, ` +
    `если на тебя давят уликами. Помни свои прежние ответы и не противоречь им без причины.${clueNote}`;
  const messages: Msg[] = [{ role: "system", content: system }];
  for (const m of (past ?? []).reverse()) {
    messages.push({
      role: m.author === "player" ? "user" : "assistant",
      content: m.text,
    });
  }
  messages.push({ role: "user", content: text });

  let reply: string;
  try {
    reply = await askGroq(messages);
  } catch {
    try {
      reply = await askGemini(messages);
    } catch {
      return fail(
        502,
        "llm_unavailable",
        "Подозреваемый сейчас не отвечает. Повторите позже",
      );
    }
  }

  const { data: saved, error: sErr } = await admin.from("dialogue_messages")
    .insert([
      {
        investigation_id: inv.id,
        suspect_id: suspect.id,
        author: "player",
        text,
      },
      {
        investigation_id: inv.id,
        suspect_id: suspect.id,
        author: "suspect",
        text: reply.slice(0, 1000),
      },
    ]).select("id, author");
  if (sErr) return fail(500, "internal_error", "Не удалось сохранить диалог");

  const messageId = saved!.find((m) => m.author === "suspect")!.id;
  return json(200, { reply, message_id: messageId });
});

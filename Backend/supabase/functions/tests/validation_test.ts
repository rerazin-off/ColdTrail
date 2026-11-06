import { assertEquals } from "@std/assert";
import { isId, isValidNpcMessage } from "../_shared/validation.ts";

Deno.test("ID: положительное целое число принимается", () => {
  assertEquals(isId(1), true);
});

Deno.test("ID: максимальное значение int4 принимается", () => {
  assertEquals(isId(2147483647), true);
});

Deno.test("ID: ноль отклоняется", () => {
  assertEquals(isId(0), false);
});

Deno.test("ID: отрицательное число отклоняется", () => {
  assertEquals(isId(-5), false);
});

Deno.test("ID: дробное число отклоняется", () => {
  assertEquals(isId(1.5), false);
});

Deno.test("ID: строка отклоняется", () => {
  assertEquals(isId("1"), false);
});

Deno.test("ID: число больше int4 отклоняется", () => {
  assertEquals(isId(2147483648), false);
});

Deno.test("NPC: непустое сообщение принимается", () => {
  assertEquals(isValidNpcMessage("Кто был в галерее?"), true);
});

Deno.test("NPC: сообщение из пробелов отклоняется", () => {
  assertEquals(isValidNpcMessage("   "), false);
});

Deno.test("NPC: пустое сообщение отклоняется", () => {
  assertEquals(isValidNpcMessage(""), false);
});

Deno.test("NPC: сообщение длиннее 300 символов отклоняется", () => {
  assertEquals(isValidNpcMessage("А".repeat(301)), false);
});

Deno.test("NPC: значение не являющееся строкой отклоняется", () => {
  assertEquals(isValidNpcMessage(123), false);
});

import { assertEquals } from "@std/assert";
import { calculateAccusation } from "../_shared/accusation.ts";

Deno.test("Обвинение: правильный подозреваемый", () => {
  const result = calculateAccusation(
    3,
    3,
    "Марк Волков",
    "Он отключил камеру наблюдения.",
  );

  assertEquals(result.isCorrect, true);
  assertEquals(
    result.explanation,
    "Верно! Марк Волков — преступник. Он отключил камеру наблюдения.",
  );
});

Deno.test("Обвинение: неправильный подозреваемый", () => {
  const result = calculateAccusation(
    3,
    1,
    "Марк Волков",
    "Он отключил камеру наблюдения.",
  );

  assertEquals(result.isCorrect, false);
  assertEquals(
    result.explanation,
    "Это ошибка. Преступником был Марк Волков.",
  );
});

Deno.test("Обвинение: одинаковые ID считаются правильным выбором", () => {
  const result = calculateAccusation(2, 2, "Анна", "Правда");

  assertEquals(result.isCorrect, true);
});

Deno.test("Обвинение: разные ID считаются ошибкой", () => {
  const result = calculateAccusation(2, 4, "Анна", "Правда");

  assertEquals(result.isCorrect, false);
});

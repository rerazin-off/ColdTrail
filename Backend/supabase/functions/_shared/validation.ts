export function isId(value: unknown): value is number {
  return (
    typeof value === "number" &&
    Number.isInteger(value) &&
    value > 0 &&
    value <= 2147483647
  );
}

export function isValidNpcMessage(value: unknown): boolean {
  if (typeof value !== "string") {
    return false;
  }

  const text = value.trim();

  return text.length >= 1 && text.length <= 300;
}

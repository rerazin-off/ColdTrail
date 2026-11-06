export interface AccusationResult {
  isCorrect: boolean;
  explanation: string;
}

export function calculateAccusation(
  realSuspectId: number,
  selectedSuspectId: number,
  realSuspectName: string,
  truth: string,
): AccusationResult {
  const isCorrect = realSuspectId === selectedSuspectId;

  const explanation = isCorrect
    ? `Верно! ${realSuspectName} — преступник. ${truth}`
    : `Это ошибка. Преступником был ${realSuspectName}.`;

  return {
    isCorrect,
    explanation,
  };
}

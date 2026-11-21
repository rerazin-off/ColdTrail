BEGIN;

CREATE OR REPLACE FUNCTION public.get_morse_challenges(p_investigation integer)
RETURNS TABLE (
    id integer, title text, hint text, signal text, reward_clue integer,
    solved boolean, reward_claimed boolean
)
LANGUAGE sql STABLE SECURITY DEFINER SET search_path = public AS $$
    SELECT p.id, p.title, p.hint, encoded.signal, p.reward_clue,
           sp.puzzle_id IS NOT NULL, cc.clue_id IS NOT NULL
    FROM public.investigations i
    JOIN public.puzzles p ON p.case_id = i.case_id AND p.kind = 'morse'
    JOIN public.clues c ON c.id = p.reward_clue AND c.case_id = i.case_id
    LEFT JOIN public.solved_puzzles sp ON sp.investigation_id = i.id AND sp.puzzle_id = p.id
    LEFT JOIN public.collected_clues cc ON cc.investigation_id = i.id AND cc.clue_id = c.id
    CROSS JOIN LATERAL (
        SELECT string_agg(alphabet.morse, ' ' ORDER BY letters.position) AS signal
        FROM regexp_split_to_table(upper(trim(p.answer)), '') WITH ORDINALITY AS letters(symbol, position)
        JOIN (VALUES
            ('A','.-'), ('B','-...'), ('C','-.-.'), ('D','-..'), ('E','.'),
            ('F','..-.'), ('G','--.'), ('H','....'), ('I','..'), ('J','.---'),
            ('K','-.-'), ('L','.-..'), ('M','--'), ('N','-.'), ('O','---'),
            ('P','.--.'), ('Q','--.-'), ('R','.-.'), ('S','...'), ('T','-'),
            ('U','..-'), ('V','...-'), ('W','.--'), ('X','-..-'), ('Y','-.--'), ('Z','--..'),
            ('0','-----'), ('1','.----'), ('2','..---'), ('3','...--'), ('4','....-'),
            ('5','.....'), ('6','-....'), ('7','--...'), ('8','---..'), ('9','----.')
        ) AS alphabet(symbol, morse) ON alphabet.symbol = letters.symbol
    ) encoded
    WHERE i.id = p_investigation AND i.user_id = auth.uid()
      AND (i.status = 'in_progress' OR sp.puzzle_id IS NOT NULL)
      AND trim(p.answer) COLLATE "C" ~ '^[A-Za-z0-9]{1,24}$'
      AND encoded.signal IS NOT NULL;
$$;

REVOKE EXECUTE ON FUNCTION public.get_morse_challenges(integer) FROM PUBLIC, anon;
GRANT EXECUTE ON FUNCTION public.get_morse_challenges(integer) TO authenticated;

CREATE OR REPLACE FUNCTION public.check_puzzle(p_investigation integer, p_puzzle integer, p_answer text)
RETURNS boolean
LANGUAGE plpgsql SECURITY DEFINER SET search_path = public AS $$
DECLARE
    case_id_for_player integer;
    ok boolean;
    reward integer;
BEGIN
    SELECT i.case_id INTO case_id_for_player
    FROM public.investigations i
    WHERE i.id = p_investigation AND i.user_id = auth.uid() AND i.status = 'in_progress'
    FOR UPDATE;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'active investigation not found' USING ERRCODE = '42501';
    END IF;

    SELECT lower(trim(p.answer)) = lower(trim(p_answer)), p.reward_clue INTO ok, reward
    FROM public.puzzles p
    LEFT JOIN public.clues c ON c.id = p.reward_clue AND c.case_id = p.case_id
    WHERE p.id = p_puzzle AND p.case_id = case_id_for_player
      AND (p.reward_clue IS NULL OR c.id IS NOT NULL);

    IF NOT FOUND THEN
        RAISE EXCEPTION 'puzzle not found in this case' USING ERRCODE = 'P0002';
    END IF;

    IF ok IS TRUE THEN
        INSERT INTO public.solved_puzzles (investigation_id, puzzle_id)
        VALUES (p_investigation, p_puzzle) ON CONFLICT DO NOTHING;
        IF reward IS NOT NULL THEN
            INSERT INTO public.collected_clues (investigation_id, clue_id)
            VALUES (p_investigation, reward) ON CONFLICT DO NOTHING;
        END IF;
    END IF;
    RETURN coalesce(ok, false);
END;
$$;

REVOKE EXECUTE ON FUNCTION public.check_puzzle(integer, integer, text) FROM PUBLIC, anon;
GRANT EXECUTE ON FUNCTION public.check_puzzle(integer, integer, text) TO authenticated;

-- Добавляем загадку в исходное дело, если оно есть и у него пока нет Морзе.
INSERT INTO public.puzzles (case_id, kind, title, hint, answer, reward_clue)
SELECT c.id, 'morse', 'Сигнал у витрины',
       'Расшифруйте три латинские буквы сигнала.', 'ART', clue.id
FROM public.cases c
JOIN LATERAL (
    SELECT id FROM public.clues WHERE case_id = c.id AND title = 'Отпечаток' ORDER BY id LIMIT 1
) clue ON true
WHERE c.id = 1 AND c.title = 'Дело №01: Пропажа'
  AND NOT EXISTS (SELECT 1 FROM public.puzzles p WHERE p.case_id = c.id AND p.kind = 'morse');

COMMIT;

BEGIN;

-- Public game artwork is separate from private photographs taken by players.
INSERT INTO storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
VALUES ('clue-images', 'clue-images', true, 1048576, ARRAY['image/png','image/webp','image/jpeg'])
ON CONFLICT (id) DO NOTHING;

DROP POLICY IF EXISTS "clue photos: own update" ON storage.objects;
CREATE POLICY "clue photos: own update" ON storage.objects FOR UPDATE TO authenticated
USING (bucket_id = 'clue-photos' AND (storage.foldername(name))[1] = auth.uid()::text)
WITH CHECK (bucket_id = 'clue-photos' AND (storage.foldername(name))[1] = auth.uid()::text);

CREATE OR REPLACE FUNCTION public.get_camera_challenges(p_investigation integer)
RETURNS TABLE (
    id integer, title text, hint text, reward_clue integer,
    solved boolean, reward_claimed boolean
)
LANGUAGE sql STABLE SECURITY DEFINER SET search_path = '' AS $$
    SELECT p.id, p.title, p.hint, c.id, sp.puzzle_id IS NOT NULL, cc.clue_id IS NOT NULL
    FROM public.investigations i
    JOIN public.puzzles p ON p.case_id = i.case_id AND p.kind = 'camera'
    JOIN public.clues c ON c.id = p.reward_clue AND c.case_id = i.case_id
    LEFT JOIN public.solved_puzzles sp ON sp.investigation_id = i.id AND sp.puzzle_id = p.id
    LEFT JOIN public.collected_clues cc ON cc.investigation_id = i.id AND cc.clue_id = c.id
    WHERE i.id = p_investigation AND i.user_id = auth.uid()
        AND (i.status = 'in_progress' OR sp.puzzle_id IS NOT NULL);
$$;

CREATE OR REPLACE FUNCTION public.complete_camera_puzzle(p_investigation integer, p_puzzle integer)
RETURNS integer
LANGUAGE plpgsql SECURITY DEFINER SET search_path = '' AS $$
DECLARE
    player_id uuid := auth.uid();
    current_status text;
    reward integer;
BEGIN
    SELECT i.status, c.id INTO current_status, reward
    FROM public.investigations i
    JOIN public.puzzles p ON p.case_id = i.case_id AND p.kind = 'camera'
    JOIN public.clues c ON c.id = p.reward_clue AND c.case_id = i.case_id
    WHERE i.id = p_investigation AND i.user_id = player_id AND p.id = p_puzzle
    FOR UPDATE OF i;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'camera challenge not found' USING ERRCODE = '42501';
    END IF;

    IF EXISTS (SELECT 1 FROM public.solved_puzzles WHERE investigation_id = p_investigation AND puzzle_id = p_puzzle)
        AND EXISTS (SELECT 1 FROM public.collected_clues WHERE investigation_id = p_investigation AND clue_id = reward) THEN
        RETURN reward;
    END IF;
    IF current_status <> 'in_progress' THEN
        RAISE EXCEPTION 'investigation is closed' USING ERRCODE = '42501';
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM storage.objects
        WHERE bucket_id = 'clue-photos' AND name = player_id::text || '/' || reward::text || '.jpg'
    ) THEN
        RAISE EXCEPTION 'photo has not been uploaded' USING ERRCODE = 'P0002';
    END IF;

    INSERT INTO public.solved_puzzles (investigation_id, puzzle_id)
    VALUES (p_investigation, p_puzzle) ON CONFLICT DO NOTHING;
    INSERT INTO public.collected_clues (investigation_id, clue_id)
    VALUES (p_investigation, reward) ON CONFLICT DO NOTHING;
    RETURN reward;
END;
$$;

REVOKE EXECUTE ON FUNCTION public.get_camera_challenges(integer) FROM PUBLIC, anon;
REVOKE EXECUTE ON FUNCTION public.complete_camera_puzzle(integer, integer) FROM PUBLIC, anon;
GRANT EXECUTE ON FUNCTION public.get_camera_challenges(integer) TO authenticated;
GRANT EXECUTE ON FUNCTION public.complete_camera_puzzle(integer, integer) TO authenticated;

-- Make the scenario playable in the original case if it has no camera puzzle.
INSERT INTO public.puzzles (case_id, kind, title, hint, answer, reward_clue)
SELECT c.case_id, 'camera', 'Поиск улики камерой',
       'Медленно повернитесь с телефоном, пока в кадре не появится улика.', 'photo', c.id
FROM public.clues c
JOIN public.cases ca ON ca.id = c.case_id
WHERE c.case_id = 1 AND ca.title = 'Дело №01: Пропажа' AND c.title = 'Фото с камеры'
  AND NOT EXISTS (SELECT 1 FROM public.puzzles p WHERE p.case_id = c.case_id AND p.kind = 'camera')
  AND NOT EXISTS (SELECT 1 FROM public.puzzles p WHERE p.reward_clue = c.id)
ORDER BY c.id
LIMIT 1;

COMMIT;

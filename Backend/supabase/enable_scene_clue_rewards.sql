BEGIN;

CREATE OR REPLACE FUNCTION public.complete_scene_camera_puzzle(
    p_investigation integer, p_puzzle integer, p_selected_clue integer
)
RETURNS integer
LANGUAGE plpgsql SECURITY DEFINER SET search_path = '' AS $$
DECLARE
    current_status text;
    reward integer;
BEGIN
    SELECT i.status, c.id INTO current_status, reward
    FROM public.investigations i
    JOIN public.puzzles p ON p.case_id = i.case_id AND p.kind = 'camera'
    JOIN public.clues c ON c.id = p.reward_clue AND c.case_id = i.case_id
    WHERE i.id = p_investigation AND i.user_id = auth.uid() AND p.id = p_puzzle
    FOR UPDATE OF i;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'camera challenge not found' USING ERRCODE = '42501';
    END IF;
    IF p_selected_clue IS DISTINCT FROM reward THEN
        RAISE EXCEPTION 'incorrect scene clue' USING ERRCODE = '22023';
    END IF;
    IF EXISTS (SELECT 1 FROM public.solved_puzzles WHERE investigation_id = p_investigation AND puzzle_id = p_puzzle)
       AND EXISTS (SELECT 1 FROM public.collected_clues WHERE investigation_id = p_investigation AND clue_id = reward) THEN
        RETURN reward;
    END IF;
    IF current_status <> 'in_progress' THEN
        RAISE EXCEPTION 'investigation is closed' USING ERRCODE = '42501';
    END IF;

    INSERT INTO public.solved_puzzles (investigation_id, puzzle_id)
    VALUES (p_investigation, p_puzzle) ON CONFLICT DO NOTHING;
    INSERT INTO public.collected_clues (investigation_id, clue_id)
    VALUES (p_investigation, reward) ON CONFLICT DO NOTHING;
    RETURN reward;
END;
$$;

REVOKE EXECUTE ON FUNCTION public.complete_scene_camera_puzzle(integer, integer, integer) FROM PUBLIC, anon;
GRANT EXECUTE ON FUNCTION public.complete_scene_camera_puzzle(integer, integer, integer) TO authenticated;

COMMIT;

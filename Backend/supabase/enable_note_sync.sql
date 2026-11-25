BEGIN;

-- Ключ позволяет безопасно повторять синхронизацию одной и той же заметки.
ALTER TABLE public.notes ADD COLUMN IF NOT EXISTS client_id uuid;
CREATE UNIQUE INDEX IF NOT EXISTS notes_client_id_key ON public.notes (client_id);

NOTIFY pgrst, 'reload schema';
COMMIT;

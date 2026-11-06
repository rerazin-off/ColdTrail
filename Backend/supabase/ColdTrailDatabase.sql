
-- Публичный профиль игрока (связан с auth.users из supobase)
create table public.profiles (
 id uuid primary key references auth.users(id) on delete cascade,
 nickname text not null check (char_length(nickname) between 2 and 30),
 birthday date,
 create_at timestamptz not null default now()
);

-- Дела
create table public.cases (
 id serial primary key,
 title text not null,
 introduction text not null,
 difficulty smallint not null default 1 check (difficulty between 1 and 5),
 create_at timestamptz not null default now()
);

-- Подозреваемые. persona_prompt клиенту не отдаётся (см. раздел «Права»)
create table public.suspects (
 id serial primary key,
 case_id integer not null references public.cases(id) on delete cascade,
 nametext not null,
 photo text,
 profile text not null,
 persona_prompt text not null,
 created_at timestamptz not null default now()
);

-- Правда о подозреваемом: доступна только серверу
create table public.suspects_secret (
 id_suspect integer primary key references public.suspects(id) on delete cascade,
 truth text not null,
 is_culprit boolean not null default false
);

-- Улики
create table public.clues (
 id serial primary key,
 case_id integer not null references public.cases(id) on delete cascade,
 title text not null,
 description text not null,
 image text,
 create_at timestamptz not null default now()
);

-- Головоломки. answer клиенту не отдаётся (см. раздел «Права»)
create table public.puzzles (
 id serial primary key,
 case_id integer not null references public.cases(id) on delete cascade,
 kind text not null check (kind in ('morse','code','camera')),
 title text not null,
 hint text,
 answer text not null,
 reward_clue integer references public.clues(id) on delete set null
);

-- Расследования: игрок + дело
create table public.investigations (
 id serial primary key,
 user_id uuid not null default auth.uid() references auth.users(id) on delete cascade,
 case_id integer not null references public.cases(id) on delete cascade,
 status text not null default 'in_progress' check (status in ('in_progress','solved','failed')),
 start_at timestamptz not null default now(),
 finished_at timestamptz,
 unique (user_id, case_id)
);

-- Инвентарь игрока
create table public.collected_clues (
 investigation_id integer not null references public.investigations(id) on delete cascade,
 clue_id integer not null references public.clues(id) on delete cascade,
 collected_at timestamptz not null default now(),
 primary key (investigation_id, clue_id)
);

-- Решённые головоломки
create table public.solved_puzzles (
 investigation_id integer not null references public.investigations(id) on delete cascade,
 puzzle_id integer not null references public.puzzles(id) on delete cascade,
 solved_at timestamptz not null default now(),
 primary key (investigation_id, puzzle_id)
);

-- История допросов
create table public.dialogue_messages (
 id serial primary key,
 investigation_id integer not null references public.investigations(id) on delete cascade,
 suspect_id integer not null references public.suspects(id) on delete cascade,
 author text not null check (author in ('player','suspect')),
 text text not null check (char_length(text) between 1 and 1000),
 create_at timestamptz not null default now()
);
create index on public.dialogue_messages (investigation_id, suspect_id, id);

-- Блокнот
create table public.notes (
 id serial primary key,
 investigation_id integer not null references public.investigations(id) on delete cascade,
 text text not null check (char_length(text) between 1 and 1000),
 create_at timestamptz not null default now(),
 update_at timestamptz not null default now()
);

-- Финальное обвинение (одно на расследование)
create table public.accusations (
 id serial primary key,
 investigation_id integer not null unique references public.investigations(id) on delete cascade,
 suspect_id integer not null references public.suspects(id),
 is_correct boolean not null,
 explanation text not null,
 created_at timestamptz not null default now()
);


create or replace function public.owns_investigation(inv integer)
returns boolean language sql stable security definer set search_path = public as $$
 select exists (select 1 from public.investigations where id = inv and user_id = auth.uid());
$$;

create or replace function public.touch_update_at()
returns trigger language plpgsql as $$
begin new.update_at = now(); return new; end $$;
create trigger notes_touch before update on public.notes
 for each row execute function public.touch_update_at();

-- Профиль создаётся автоматически при регистрации
create or replace function public.handle_new_user()
returns trigger language plpgsql security definer set search_path = public as $$
begin
 insert into public.profiles (id, nickname)
 values (new.id, coalesce(nullif(new.raw_user_meta_data->>'nickname', ''), split_part(new.email, '@', 1)));
 return new;
end $$;
create trigger on_auth_user_created after insert on auth.users
 for each row execute function public.handle_new_user();

-- Проверка ответа головоломки на сервере (ответ клиенту не отдаётся)
create or replace function public.check_puzzle(p_investigation integer, p_puzzle integer, p_answer text)
returns boolean language plpgsql security definer set search_path = public as $$
declare ok boolean; reward integer;
begin
 if not public.owns_investigation(p_investigation) then
 raise exception 'forbidden' using errcode = '42501';
 end if;
 select lower(trim(p.answer)) = lower(trim(p_answer)), p.reward_clue
 into ok, reward
 from public.puzzles p where p.id = p_puzzle;
 if ok is null then raise exception 'puzzle not found' using errcode = 'P0002'; end if;
 if ok then
 insert into public.solved_puzzles (investigation_id, puzzle_id)
 values (p_investigation, p_puzzle) on conflict do nothing;
 if reward is not null then
 insert into public.collected_clues (investigation_id, clue_id)
 values (p_investigation, reward) on conflict do nothing;
 end if;
 end if;
 return ok;
end $$;

-- Сколько ответов подозреваемых игрок получил сегодня (для лимита)

create or replace function public.npc_replies_today(p_user uuid)
returns integer language sql stable security definer set search_path = public as $$
 select count(*)::integer
 from public.dialogue_messages m
 join public.investigations i on i.id = m.investigation_id
 where i.user_id = p_user and m.author = 'suspect' and m.create_at >= date_trunc('day', now());
$$;

-- Без токена игрока (роль anon) таблицы недоступны
revoke all on all tables in schema public from anon;
revoke truncate, references, trigger on all tables in schema public from authenticated;
revoke all on public.suspects_secret from anon, authenticated;
revoke execute on function public.npc_replies_today(uuid) from public, anon, authenticated;
revoke execute on function public.check_puzzle(integer, integer, text) from public, anon;
grant execute on function public.check_puzzle(integer, integer, text) to authenticated;
grant execute on function public.npc_replies_today(uuid) to service_role;

-- Игрок не может менять статус расследования и писать в диалоги/обвинения напрямую
revoke insert, update, delete on public.dialogue_messages, public.accusations,
 public.solved_puzzles, public.cases, public.suspects, public.clues, public.puzzles from authenticated, anon;
revoke update on public.investigations from authenticated;

-- Секретные столбцы закрыты правами на столбцы: persona_prompt и answer клиент прочитать не может.
revoke select on public.suspects, public.puzzles from authenticated;
grant select (id, case_id, name, profile, photo, created_at) on public.suspects to authenticated;
grant select (id, case_id, kind, title, hint, reward_clue) on public.puzzles to authenticated;

--Защита на уровне строк
alter table public.profiles  enable row level security;
alter table public.cases enable row level security;
alter table public.suspects  enable row level security;
alter table public.suspects_secret  enable row level security;
alter table public.clues enable row level security;
alter table public.puzzles enable row level security;
alter table public.investigations enable row level security;
alter table public.collected_clues  enable row level security;
alter table public.solved_puzzles enable row level security;
alter table public.dialogue_messages enable row level security;
alter table public.notes enable row level security;
alter table public.accusations enable row level security;


create policy profiles_select on public.profiles for select to authenticated using (id = auth.uid());
create policy profiles_insert on public.profiles for insert to authenticated with check (id = auth.uid());
create policy profiles_update on public.profiles for update to authenticated using (id = auth.uid()) with check (id = auth.uid());

-- Дела, подозреваемые, улики и головоломки читают все авторизованные игроки
create policy cases_read on public.cases for select to authenticated using (true);
create policy suspects_read on public.suspects for select to authenticated using (true);
create policy clues_read on public.clues for select to authenticated using (true);
create policy puzzles_read on public.puzzles for select to authenticated using (true);

create policy inv_select on public.investigations for select to authenticated using (user_id = auth.uid());
create policy inv_insert on public.investigations for insert to authenticated
 with check (user_id = auth.uid() and status = 'in_progress');
create policy inv_delete on public.investigations for delete to authenticated using (user_id = auth.uid());

create policy cc_select on public.collected_clues for select to authenticated using (public.owns_investigation(investigation_id));
create policy cc_insert on public.collected_clues for insert to authenticated with check (public.owns_investigation(investigation_id));
create policy cc_update on public.collected_clues for update to authenticated
 using (public.owns_investigation(investigation_id)) with check (public.owns_investigation(investigation_id));
create policy cc_delete on public.collected_clues for delete to authenticated using (public.owns_investigation(investigation_id));

create policy sp_select on public.solved_puzzles for select to authenticated using (public.owns_investigation(investigation_id));
create policy dm_select on public.dialogue_messages for select to authenticated using (public.owns_investigation(investigation_id));
create policy acc_select on public.accusations for select to authenticated using (public.owns_investigation(investigation_id));

create policy notes_all on public.notes for all to authenticated
 using (public.owns_investigation(investigation_id)) with check (public.owns_investigation(investigation_id));

-- Права на таблицы для ролей API
grant select on public.cases, public.clues,
 public.solved_puzzles, public.dialogue_messages, public.accusations to authenticated;
grant select, insert, update on public.profiles to authenticated;
grant select, insert, delete on public.investigations to authenticated;
grant select, insert, update, delete on public.collected_clues, public.notes to authenticated;

-- Для столбцов serial игроку нужен доступ к счётчикам
grant usage, select on sequence public.investigations_id_seq, public.notes_id_seq to authenticated;


-- Хранилище фото улик: приватный бакет, файлы лежат в папке с id пользователя: <user_id>/<clue_id>.webp
insert into storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
values ('clue-photos', 'clue-photos', false, 1048576, array['image/webp','image/jpeg','image/png'])
on conflict (id) do nothing;

-- Хранилище фото для подозреваемых
insert into storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
values ('suspect-photos', 'suspect-photos', true, 1048576, array['image/webp','image/jpeg','image/png'])
on conflict (id) do nothing;

create policy "clue photos: own read" on storage.objects for select to authenticated
 using (bucket_id = 'clue-photos' and (storage.foldername(name))[1] = auth.uid()::text);
create policy "clue photos: own upload" on storage.objects for insert to authenticated
 with check (bucket_id = 'clue-photos' and (storage.foldername(name))[1] = auth.uid()::text);
create policy "clue photos: own delete" on storage.objects for delete to authenticated
 using (bucket_id = 'clue-photos' and (storage.foldername(name))[1] = auth.uid()::text);

-- Тестовые начальные данные
insert into public.cases (id, title, introduction, difficulty) values
 (1, 'Дело №01: Пропажа',
 'Вечером из городской галереи исчезла картина. Витрина осталась целой, а служебный вход был доступен сотрудникам. Камеры зафиксировали движение в 22:14.', 2);

insert into public.suspects (id, case_id, name, profile, persona_prompt) values
 (1,1,'Иван Петров','Сотрудник галереи. Спокойный, вежливый, всегда на месте.',
 'Ты Иван Петров, сотрудник галереи. Говоришь спокойно и вежливо. Скрываешь, что в 22:00 вышел покурить и оставил служебный вход открытым. Если на тебя давят уликой — путаешься, потом признаёшься.'),
 (2,1,'Анна Смирнова','Реставратор. Работает с картинами, часто задерживается допоздна.',
 'Ты Анна Смирнова, реставратор. Нервничаешь при вопросах о ночи. Скрываешь, что была в мастерской и видела мужчину у служебного входа.'),
 (3,1,'Марк Волков','Охранник. Отвечает за служебный вход и камеры.',
 'Ты Марк Волков, охранник. Уверенно отрицаешь всё. Ты вынес картину сам, отключив камеру на 22:14. Лжёшь о том, что камера сломана давно. Под давлением уликами (ключ-карта, запись камеры) начинаешь противоречить себе.'),
 (4,1,'Елена Орлова','Посетитель. Пришла на выставку за час до закрытия.',
 'Ты Елена Орлова, посетитель. Говоришь коротко и раздражённо. Ничего не знаешь, ушла до закрытия.');

insert into public.suspects_secret (id_suspect, truth, is_culprit) values
 (1,'Невиновен, но оставил вход открытым.', false),
 (2,'Невиновна, видела охранника у входа.', false),
 (3,'Преступник.', true),
 (4,'Невиновна.', false);

insert into public.clues (id, case_id, title, description, image) values
 (1,1,'Записка','Найдена у служебного входа.','clues/note.webp'),
 (2,1,'Фото с камеры','Кадр с временем 22:14.','clues/camera.webp'),
 (3,1,'Ключ-карта','Доступ в служебные зоны.','clues/keycard.webp'),
 (4,1,'Отпечаток','Найден на стекле витрины.','clues/fingerprint.webp');

insert into public.puzzles (id, case_id, kind, title, hint, answer, reward_clue) values
 (1,1,'code','Код доступа','Используйте время с камеры наблюдения.','2214',3);

-- после вставки с заданными id сдвигаем счётчики, чтобы новые записи получали следующие номера
select setval(pg_get_serial_sequence('public.cases','id'), (select max(id) from public.cases));
select setval(pg_get_serial_sequence('public.suspects','id'), (select max(id) from public.suspects));
select setval(pg_get_serial_sequence('public.clues','id'), (select max(id) from public.clues));
select setval(pg_get_serial_sequence('public.puzzles','id'), (select max(id) from public.puzzles));



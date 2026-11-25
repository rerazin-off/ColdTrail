package com.example.mrdetective.data.di

import com.example.mrdetective.data.repository.AccusationRepositoryImpl
import com.example.mrdetective.data.repository.AuthRepositoryImpl
import com.example.mrdetective.data.repository.CameraChallengeRepositoryImpl
import com.example.mrdetective.data.repository.CaseRepositoryImpl
import com.example.mrdetective.data.repository.ClueRepositoryImpl
import com.example.mrdetective.data.repository.CodeChallengeRepositoryImpl
import com.example.mrdetective.data.repository.DialogueRepositoryImpl
import com.example.mrdetective.data.repository.InventoryRepositoryImpl
import com.example.mrdetective.data.repository.MorseChallengeRepositoryImpl
import com.example.mrdetective.data.repository.NotebookRepositoryImpl
import com.example.mrdetective.data.repository.PhotoRepositoryImpl
import com.example.mrdetective.data.repository.ProfileStatisticsRepositoryImpl
import com.example.mrdetective.data.repository.ProgressRepositoryImpl
import com.example.mrdetective.data.repository.PuzzleRepositoryImpl
import com.example.mrdetective.data.repository.SceneClueRepositoryImpl
import com.example.mrdetective.data.repository.SceneRepositoryImpl
import com.example.mrdetective.data.repository.SuspectRepositoryImpl
import com.example.mrdetective.domain.repositories.AccusationRepository
import com.example.mrdetective.domain.repositories.AuthRepository
import com.example.mrdetective.domain.repositories.CameraChallengeRepository
import com.example.mrdetective.domain.repositories.CaseRepository
import com.example.mrdetective.domain.repositories.ClueRepository
import com.example.mrdetective.domain.repositories.CodeChallengeRepository
import com.example.mrdetective.domain.repositories.DialogueRepository
import com.example.mrdetective.domain.repositories.InventoryRepository
import com.example.mrdetective.domain.repositories.MorseChallengeRepository
import com.example.mrdetective.domain.repositories.NotebookRepository
import com.example.mrdetective.domain.repositories.PhotoRepository
import com.example.mrdetective.domain.repositories.ProfileStatisticsRepository
import com.example.mrdetective.domain.repositories.ProgressRepository
import com.example.mrdetective.domain.repositories.PuzzleRepository
import com.example.mrdetective.domain.repositories.SceneClueRepository
import com.example.mrdetective.domain.repositories.SceneRepository
import com.example.mrdetective.domain.repositories.SuspectRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Binds data repositories to their domain interfaces. */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    /** Binds the data implementation of [AuthRepository]. */
    @Binds @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    /** Binds the data implementation of [CaseRepository]. */
    @Binds @Singleton
    abstract fun bindCaseRepository(impl: CaseRepositoryImpl): CaseRepository

    /** Binds the data implementation of [SuspectRepository]. */
    @Binds @Singleton
    abstract fun bindSuspectRepository(impl: SuspectRepositoryImpl): SuspectRepository

    /** Binds the data implementation of [ClueRepository]. */
    @Binds @Singleton
    abstract fun bindClueRepository(impl: ClueRepositoryImpl): ClueRepository

    /** Binds the data implementation of [NotebookRepository]. */
    @Binds @Singleton
    abstract fun bindNotebookRepository(impl: NotebookRepositoryImpl): NotebookRepository

    /** Binds the data implementation of [DialogueRepository]. */
    @Binds @Singleton
    abstract fun bindDialogueRepository(impl: DialogueRepositoryImpl): DialogueRepository

    /** Binds the data implementation of [InventoryRepository]. */
    @Binds @Singleton
    abstract fun bindInventoryRepository(impl: InventoryRepositoryImpl): InventoryRepository

    /** Binds the data implementation of [ProgressRepository]. */
    @Binds @Singleton
    abstract fun bindProgressRepository(impl: ProgressRepositoryImpl): ProgressRepository

    /** Binds the data implementation of [ProfileStatisticsRepository]. */
    @Binds @Singleton
    abstract fun bindProfileStatistics(impl: ProfileStatisticsRepositoryImpl): ProfileStatisticsRepository

    /** Binds the data implementation of [PuzzleRepository]. */
    @Binds @Singleton
    abstract fun bindPuzzleRepository(impl: PuzzleRepositoryImpl): PuzzleRepository

    /** Binds the data implementation of [MorseChallengeRepository]. */
    @Binds @Singleton
    abstract fun bindMorseChallengeRepository(impl: MorseChallengeRepositoryImpl): MorseChallengeRepository

    /** Binds the data implementation of [CameraChallengeRepository]. */
    @Binds @Singleton
    abstract fun bindCameraChallengeRepository(impl: CameraChallengeRepositoryImpl): CameraChallengeRepository

    /** Binds the data implementation of [SceneClueRepository]. */
    @Binds @Singleton
    abstract fun bindSceneClueRepository(impl: SceneClueRepositoryImpl): SceneClueRepository

    /** Binds the data implementation of [CodeChallengeRepository]. */
    @Binds @Singleton
    abstract fun bindCodeChallengeRepository(impl: CodeChallengeRepositoryImpl): CodeChallengeRepository

    /** Binds the data implementation of [AccusationRepository]. */
    @Binds @Singleton
    abstract fun bindAccusationRepository(impl: AccusationRepositoryImpl): AccusationRepository

    /** Binds the data implementation of [SceneRepository]. */
    @Binds @Singleton
    abstract fun bindSceneRepository(impl: SceneRepositoryImpl): SceneRepository

    /** Binds the data implementation of [PhotoRepository]. */
    @Binds @Singleton
    abstract fun bindPhotoRepository(impl: PhotoRepositoryImpl): PhotoRepository
}

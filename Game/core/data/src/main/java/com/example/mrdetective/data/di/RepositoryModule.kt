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

/** Связывает репозитории слоя данных с их интерфейсами в домене. */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    /** Подключает реализацию [AuthRepository] из слоя данных. */
    @Binds @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    /** Подключает реализацию [CaseRepository] из слоя данных. */
    @Binds @Singleton
    abstract fun bindCaseRepository(impl: CaseRepositoryImpl): CaseRepository

    /** Подключает реализацию [SuspectRepository] из слоя данных. */
    @Binds @Singleton
    abstract fun bindSuspectRepository(impl: SuspectRepositoryImpl): SuspectRepository

    /** Подключает реализацию [ClueRepository] из слоя данных. */
    @Binds @Singleton
    abstract fun bindClueRepository(impl: ClueRepositoryImpl): ClueRepository

    /** Подключает реализацию [NotebookRepository] из слоя данных. */
    @Binds @Singleton
    abstract fun bindNotebookRepository(impl: NotebookRepositoryImpl): NotebookRepository

    /** Подключает реализацию [DialogueRepository] из слоя данных. */
    @Binds @Singleton
    abstract fun bindDialogueRepository(impl: DialogueRepositoryImpl): DialogueRepository

    /** Подключает реализацию [InventoryRepository] из слоя данных. */
    @Binds @Singleton
    abstract fun bindInventoryRepository(impl: InventoryRepositoryImpl): InventoryRepository

    /** Подключает реализацию [ProgressRepository] из слоя данных. */
    @Binds @Singleton
    abstract fun bindProgressRepository(impl: ProgressRepositoryImpl): ProgressRepository

    /** Подключает реализацию [ProfileStatisticsRepository] из слоя данных. */
    @Binds @Singleton
    abstract fun bindProfileStatistics(impl: ProfileStatisticsRepositoryImpl): ProfileStatisticsRepository

    /** Подключает реализацию [PuzzleRepository] из слоя данных. */
    @Binds @Singleton
    abstract fun bindPuzzleRepository(impl: PuzzleRepositoryImpl): PuzzleRepository

    /** Подключает реализацию [MorseChallengeRepository] из слоя данных. */
    @Binds @Singleton
    abstract fun bindMorseChallengeRepository(impl: MorseChallengeRepositoryImpl): MorseChallengeRepository

    /** Подключает реализацию [CameraChallengeRepository] из слоя данных. */
    @Binds @Singleton
    abstract fun bindCameraChallengeRepository(impl: CameraChallengeRepositoryImpl): CameraChallengeRepository

    /** Подключает реализацию [SceneClueRepository] из слоя данных. */
    @Binds @Singleton
    abstract fun bindSceneClueRepository(impl: SceneClueRepositoryImpl): SceneClueRepository

    /** Подключает реализацию [CodeChallengeRepository] из слоя данных. */
    @Binds @Singleton
    abstract fun bindCodeChallengeRepository(impl: CodeChallengeRepositoryImpl): CodeChallengeRepository

    /** Подключает реализацию [AccusationRepository] из слоя данных. */
    @Binds @Singleton
    abstract fun bindAccusationRepository(impl: AccusationRepositoryImpl): AccusationRepository

    /** Подключает реализацию [SceneRepository] из слоя данных. */
    @Binds @Singleton
    abstract fun bindSceneRepository(impl: SceneRepositoryImpl): SceneRepository

    /** Подключает реализацию [PhotoRepository] из слоя данных. */
    @Binds @Singleton
    abstract fun bindPhotoRepository(impl: PhotoRepositoryImpl): PhotoRepository
}

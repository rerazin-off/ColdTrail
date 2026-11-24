package com.example.mrdetective.data.di

import com.example.mrdetective.data.repository.*
import com.example.mrdetective.domain.repositories.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
    @Binds @Singleton abstract fun bindCaseRepository(impl: CaseRepositoryImpl): CaseRepository
    @Binds @Singleton abstract fun bindSuspectRepository(impl: SuspectRepositoryImpl): SuspectRepository
    @Binds @Singleton abstract fun bindClueRepository(impl: ClueRepositoryImpl): ClueRepository
    @Binds @Singleton abstract fun bindNotebookRepository(impl: NotebookRepositoryImpl): NotebookRepository
    @Binds @Singleton abstract fun bindDialogueRepository(impl: DialogueRepositoryImpl): DialogueRepository
    @Binds @Singleton abstract fun bindInventoryRepository(impl: InventoryRepositoryImpl): InventoryRepository
    @Binds @Singleton abstract fun bindProgressRepository(impl: ProgressRepositoryImpl): ProgressRepository
    @Binds @Singleton abstract fun bindProfileStatistics(impl: ProfileStatisticsRepositoryImpl): ProfileStatisticsRepository
    @Binds @Singleton abstract fun bindPuzzleRepository(impl: PuzzleRepositoryImpl): PuzzleRepository
    @Binds @Singleton abstract fun bindMorseChallengeRepository(impl: MorseChallengeRepositoryImpl): MorseChallengeRepository
    @Binds @Singleton abstract fun bindCameraChallengeRepository(impl: CameraChallengeRepositoryImpl): CameraChallengeRepository
    @Binds @Singleton abstract fun bindSceneClueRepository(impl: SceneClueRepositoryImpl): SceneClueRepository
    @Binds @Singleton abstract fun bindCodeChallengeRepository(impl: CodeChallengeRepositoryImpl): CodeChallengeRepository
    @Binds @Singleton abstract fun bindAccusationRepository(impl: AccusationRepositoryImpl): AccusationRepository
    @Binds @Singleton abstract fun bindSceneRepository(impl: SceneRepositoryImpl): SceneRepository
    @Binds @Singleton abstract fun bindPhotoRepository(impl: PhotoRepositoryImpl): PhotoRepository
}

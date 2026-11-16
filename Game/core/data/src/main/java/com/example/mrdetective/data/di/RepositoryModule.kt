package com.example.mrdetective.data.di

import com.example.mrdetective.data.repository.SupabaseAuthRepositoryImpl
import com.example.mrdetective.data.repository.SupabaseCaseRepositoryImpl
import com.example.mrdetective.data.repository.SupabaseClueRepositoryImpl
import com.example.mrdetective.data.repository.SupabaseNotebookRepositoryImpl
import com.example.mrdetective.data.repository.SupabaseSuspectRepositoryImpl
import com.example.mrdetective.domain.repositories.AuthRepository
import com.example.mrdetective.domain.repositories.CaseRepository
import com.example.mrdetective.domain.repositories.ClueRepository
import com.example.mrdetective.domain.repositories.NotebookRepository
import com.example.mrdetective.domain.repositories.SuspectRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton abstract fun bindAuthRepository(impl: SupabaseAuthRepositoryImpl): AuthRepository
    @Binds @Singleton
    abstract fun bindCaseRepository(impl: SupabaseCaseRepositoryImpl): CaseRepository
    @Binds @Singleton abstract fun bindClueRepository(impl: SupabaseClueRepositoryImpl): ClueRepository
    @Binds @Singleton abstract fun bindNotebookRepository(impl: SupabaseNotebookRepositoryImpl): NotebookRepository
    @Binds @Singleton abstract fun bindSuspectRepository(impl: SupabaseSuspectRepositoryImpl): SuspectRepository
}
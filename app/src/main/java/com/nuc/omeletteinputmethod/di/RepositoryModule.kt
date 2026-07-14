package com.nuc.omeletteinputmethod.di

import com.nuc.omeletteinputmethod.inputC.InputC
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideInputC(): InputC {
        return InputC()
    }
}

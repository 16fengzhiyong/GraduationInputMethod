package com.nuc.omeletteinputmethod.di

import com.nuc.omeletteinputmethod.data.local.ClipboardDao
import com.nuc.omeletteinputmethod.data.local.NoteDao
import com.nuc.omeletteinputmethod.data.local.ShortcutDao
import com.nuc.omeletteinputmethod.data.local.UserDao
import com.nuc.omeletteinputmethod.data.local.UserDictionaryDao
import com.nuc.omeletteinputmethod.data.model.InputStatDao
import com.nuc.omeletteinputmethod.data.remote.TokenManager
import com.nuc.omeletteinputmethod.data.remote.api.ApiService
import com.nuc.omeletteinputmethod.data.repository.AuthRepository
import com.nuc.omeletteinputmethod.data.repository.CloudSyncRepository
import com.nuc.omeletteinputmethod.data.repository.ThemeStoreRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 仓库层 DI 绑定
 *
 * AuthRepository / CloudSyncRepository / ThemeStoreRepository 均已标注 @Inject constructor，
 * Hilt 可自动发现它们。本模块提供显式绑定以确保无歧义。
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideAuthRepository(
        apiService: ApiService,
        tokenManager: TokenManager,
        userDao: UserDao,
    ): AuthRepository = AuthRepository(apiService, tokenManager, userDao)

    @Provides
    @Singleton
    fun provideCloudSyncRepository(
        apiService: ApiService,
        userDictionaryDao: UserDictionaryDao,
        noteDao: NoteDao,
        shortcutDao: ShortcutDao,
        clipboardDao: ClipboardDao,
        inputStatDao: InputStatDao,
    ): CloudSyncRepository = CloudSyncRepository(
        apiService, userDictionaryDao, noteDao, shortcutDao, clipboardDao, inputStatDao
    )

    @Provides
    @Singleton
    fun provideThemeStoreRepository(
        apiService: ApiService,
    ): ThemeStoreRepository = ThemeStoreRepository(apiService)
}
package com.verywords.csms_android.data.local.di

import com.verywords.csms_android.data.local.repository.MessageRepository
import com.verywords.csms_android.data.local.repository.impl.MessageRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LocalDataModule {
    @Singleton
    @Binds
    abstract fun provideMessageRepository(
        impl: MessageRepositoryImpl,
    ): MessageRepository
}
package com.verywords.csms_android.data.remote.di

import com.verywords.csms_android.data.remote.repository.LogRepository
import com.verywords.csms_android.data.remote.repository.impl.FakeLogRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
abstract class ServiceModule {

    @Singleton
    @Binds
    abstract fun provideLogRepository(impl: FakeLogRepositoryImpl): LogRepository
}
package com.verywords.csms_android.di

import com.verywords.csms_android.TestRepository
import com.verywords.csms_android.TestRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TestModule {
    @Singleton
    @Binds
    abstract fun provideTestRepository(impl: TestRepositoryImpl): TestRepository
}
package bruhcollective.itaysonlab.jetisteam.di

import bruhcollective.itaysonlab.jetisteam.service.MiniprofileService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.create
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {
    @Provides
    @Singleton
    fun provideMiniprofileService(retrofit: Retrofit) = retrofit.create<MiniprofileService>()
}
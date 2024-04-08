package net.treelzebub.podcasts.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import net.treelzebub.podcasts.util.DefaultDispatcher
import net.treelzebub.podcasts.util.IoDispatcher
import net.treelzebub.podcasts.util.MainDispatcher
import net.treelzebub.podcasts.util.MainImmediateDispatcher
import javax.inject.Qualifier
import javax.inject.Singleton


@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope

@Module
@InstallIn(SingletonComponent::class)
object CoroutinesDispatchersModule {
    @Provides
    @DefaultDispatcher
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @MainDispatcher
    fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main

    @Provides
    @MainImmediateDispatcher
    fun provideMainImmediateDispatcher(): CoroutineDispatcher = Dispatchers.Main.immediate
}

@InstallIn(SingletonComponent::class)
@Module
object CoroutinesScopesModule {

    @Singleton
    @Provides
    @ApplicationScope
    fun provideDefaultScope(
        @DefaultDispatcher defaultDispatcher: CoroutineDispatcher
    ): CoroutineScope = CoroutineScope(SupervisorJob() + defaultDispatcher)

    @Singleton
    @Provides
    @IoDispatcher
    fun provideIoScope(
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): CoroutineScope = CoroutineScope(ioDispatcher)

    @Singleton
    @Provides
    @MainDispatcher
    fun provideMainScope(
        @MainDispatcher mainDispatcher: CoroutineDispatcher
    ): CoroutineScope = CoroutineScope(mainDispatcher)

    @Singleton
    @Provides
    @MainImmediateDispatcher
    fun provideMainImmediateScope(
        @MainImmediateDispatcher mainImmediateDispatcher: CoroutineDispatcher
    ): CoroutineScope = CoroutineScope(mainImmediateDispatcher)
}

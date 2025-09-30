package org.banana.project.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.banana.project.services.Interfaces.ILlmService
import org.banana.project.services.Interfaces.ISpeechParser
import org.banana.project.services.LlmService
import org.banana.project.services.SpeechParserService
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideLlmService(
        @ApplicationContext context: Context
    ): ILlmService {
        return LlmService(context)
    }

    @Provides
    @Singleton
    fun provideSpeechParser(): ISpeechParser {
        return SpeechParserService()
    }
}
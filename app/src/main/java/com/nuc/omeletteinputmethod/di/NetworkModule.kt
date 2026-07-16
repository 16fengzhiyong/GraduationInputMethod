package com.nuc.omeletteinputmethod.di

import com.nuc.omeletteinputmethod.data.remote.api.ApiService
import com.nuc.omeletteinputmethod.data.remote.api.MockApiService
import com.nuc.omeletteinputmethod.data.remote.interceptor.AuthInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /** 基础 OkHttpClient（无认证拦截器） */
    @Provides
    @Singleton
    @Named("plainClient")
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient
            .Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            })
            .build()
    }

    /** 带认证拦截器的 OkHttpClient — 用于需要登录态的 API 请求 */
    @Provides
    @Singleton
    @Named("authClient")
    fun provideOkHttpClientWithAuth(
        authInterceptor: AuthInterceptor
    ): OkHttpClient {
        return OkHttpClient
            .Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            })
            .build()
    }

    /**
     * Retrofit 实例 — 使用带认证拦截器的 OkHttpClient
     */
    @Provides
    @Singleton
    fun provideRetrofit(
        @Named("authClient") okHttpClient: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * API 服务实现 — 当前通过 Retrofit 创建，指向 MockApiService
     *
     * 后续替换为真实后端时，直接修改 ApiService 接口的 Retrofit 注解即可。
     */
    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        // 开发阶段 — 仍使用 Mock 实现
        // 生产阶段改为: return retrofit.create(ApiService::class.java)
        return MockApiService()
    }

    private const val BASE_URL = "https://api.omeletteime.example.com/"
}

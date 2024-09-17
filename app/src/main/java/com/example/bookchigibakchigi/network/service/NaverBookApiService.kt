package com.example.bookchigibakchigi.network.service

import androidx.core.os.BuildCompat
import com.example.bookchigibakchigi.BuildConfig
import com.example.bookchigibakchigi.network.model.NaverBookResponse
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface NaverBookService {

    @GET("v1/search/book.json")
    suspend fun searchBooks(
        @Query("query") query: String,
        @Query("display") display: Int = 10,
        @Query("start") start: Int = 1,
        @Query("sort") sort: String = "sim"
    ): NaverBookResponse

    companion object {
        private const val BASE_URL = "https://openapi.naver.com/"

        fun create(): NaverBookService {
            // HTTP 로그를 출력하는 로거 설정
//            val logger = HttpLoggingInterceptor().apply {
//                level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
//            }

            // 네이버 API 호출을 위한 헤더 설정
            val headerInterceptor = Interceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("X-Naver-Client-Id", BuildConfig.NAVER_CLIENT_ID)
                    .addHeader("X-Naver-Client-Secret", BuildConfig.NAVER_CLIENT_SECRET)
                    .build()
                chain.proceed(request)
            }

            // OkHttpClient 설정
            val client = OkHttpClient.Builder()
                .addInterceptor(headerInterceptor)
                .build()

            // Retrofit 인스턴스 생성
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(NaverBookService::class.java)
        }
    }
}
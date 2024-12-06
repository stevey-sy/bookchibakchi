package com.example.bookchigibakchigi.network.service


import com.example.bookchigibakchigi.BuildConfig
import com.example.bookchigibakchigi.network.model.AladinBookResponse
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface AladinBookApiService {

    @GET("ItemSearch.aspx")
    fun searchBooks(
        @Query("ttbkey") ttbKey: String,
        @Query("Query") query: String,
        @Query("QueryType") queryType: String = "Keyword", // 기본값 설정
        @Query("SearchTarget") searchTarget: String = "Book", // 기본값 설정
        @Query("Start") start: Int = 1,
        @Query("MaxResults") maxResults: Int = 10,
        @Query("Sort") sort: String = "Accuracy",
        @Query("Cover") cover: String = "Mid",
        @Query("Output") output: String = "JS",
        @Query("Version") version: String = "20131101"
    ): Response<AladinBookResponse>

    companion object {
        private const val BASE_URL = "http://www.aladin.co.kr/ttb/api/"

        fun create(): AladinBookApiService {

            // 네이버 API 호출을 위한 헤더 설정
            val headerInterceptor = Interceptor { chain ->
                val request = chain.request().newBuilder()
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
                .create(AladinBookApiService::class.java)
        }
    }
}
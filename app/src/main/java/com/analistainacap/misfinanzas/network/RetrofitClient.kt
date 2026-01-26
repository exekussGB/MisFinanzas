package com.analistainacap.misfinanzas.network

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private var apiService: SupabaseApiService? = null

    /**
     * Retorna la instancia de la API con Interceptor de Seguridad y LOGS.
     */
    fun getApi(context: Context): SupabaseApiService {
        if (apiService == null) {
            // Agregamos un interceptor de logs para ver el Error 400 exacto en Logcat
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .addInterceptor { chain ->
                    val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
                    val token = prefs.getString("token", "")

                    val request = chain.request().newBuilder()
                        .addHeader("apikey", SupabaseConfig.ANON_KEY)
                        .apply {
                            if (!token.isNullOrEmpty()) addHeader("Authorization", "Bearer $token")
                        }
                        .build()
                    chain.proceed(request)
                }
                .build()

            apiService = Retrofit.Builder()
                .baseUrl(SupabaseConfig.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(SupabaseApiService::class.java)
        }
        return apiService!!
    }

    fun getClient(context: Context): Retrofit {
        return Retrofit.Builder()
            .baseUrl(SupabaseConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}

package com.analistainacap.misfinanzas.network

import android.content.Context
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private var apiService: SupabaseApiService? = null

    /**
     * Retorna la instancia de la API centralizada.
     * Inyecta automáticamente ANON_KEY y Token de sesión.
     */
    fun getApi(context: Context): SupabaseApiService {
        if (apiService == null) {
            val client = OkHttpClient.Builder().addInterceptor { chain ->
                val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
                val token = prefs.getString("token", "")

                val request = chain.request().newBuilder()
                    .addHeader("apikey", SupabaseConfig.ANON_KEY)
                    .apply {
                        if (!token.isNullOrEmpty()) addHeader("Authorization", "Bearer $token")
                    }
                    .build()
                chain.proceed(request)
            }.build()

            apiService = Retrofit.Builder()
                .baseUrl(SupabaseConfig.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(SupabaseApiService::class.java)
        }
        return apiService!!
    }

    /**
     * Método de compatibilidad para código que espera getClient()
     */
    fun getClient(context: Context): Retrofit {
        return Retrofit.Builder()
            .baseUrl(SupabaseConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}

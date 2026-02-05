package com.analistainacap.misfinanzas.network

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Cliente Retrofit Senior para Supabase.
 * Implementa inyección dinámica de Token JWT y Logging de red.
 */
object RetrofitClient {
    private var apiService: SupabaseApiService? = null

    fun getApi(context: Context): SupabaseApiService {
        if (apiService == null) {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .addInterceptor { chain ->
                    // Recuperar token actualizado en cada petición
                    val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
                    val token = prefs.getString("token", "")

                    val requestBuilder = chain.request().newBuilder()
                        .addHeader("apikey", SupabaseConfig.ANON_KEY)
                        .addHeader("Content-Type", "application/json")

                    if (!token.isNullOrEmpty()) {
                        requestBuilder.addHeader("Authorization", "Bearer $token")
                    }

                    chain.proceed(requestBuilder.build())
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

    /**
     * Limpia la instancia de la API al cerrar sesión.
     * Obliga a recrear el cliente con el nuevo contexto de seguridad.
     */
    fun clearInstance() {
        apiService = null
    }
}

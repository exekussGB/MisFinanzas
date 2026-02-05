package com.analistainacap.misfinanzas.network

import android.content.Context
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Cliente Retrofit Senior para Supabase.
 * PASO 3: Interceptor Correcto (La sesión se recupera dinámicamente).
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
                    // 1️⃣ Obtención de sesión oficial del cliente (Paso 3)
                    val session = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
                    val token = session.getString("token", "")
                    val userId = session.getString("user_id", "")

                    Log.e("DEBUG_SESSION_CLIENT", "USER_ID = $userId | TOKEN PRESENTE = ${!token.isNullOrEmpty()}")

                    val requestBuilder = chain.request().newBuilder()
                        .addHeader("apikey", SupabaseConfig.ANON_KEY)
                        .addHeader("Content-Type", "application/json")

                    // Solo inyectar si la sesión es válida
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

    fun clearInstance() {
        apiService = null
    }
}

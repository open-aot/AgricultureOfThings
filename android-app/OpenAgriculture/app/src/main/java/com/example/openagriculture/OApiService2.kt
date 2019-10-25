package com.example.openagriculture

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET


private const val BASE_URL = "<SERVER_LOCAL_IP>";

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()

interface OApiService2 {
    @GET("sensor1.json")
    fun getProperties():
            Call<List<OAData>>
}

object OApi2 {
    val retrofitService : OApiService2 by lazy {
        retrofit.create(OApiService2::class.java)
    }
}

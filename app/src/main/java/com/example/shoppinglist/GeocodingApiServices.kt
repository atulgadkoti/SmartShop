package com.example.shoppinglist

import retrofit2.http.GET
import retrofit2.http.Query

interface GeocodingApiServices {
    @GET("maps/api/geocode/json")
    suspend fun getAddressFromCoordinates(
        @Query("latlng")latlng : String,
        @Query("key")apiKey : String
    ): MyGeocodingResponse
}
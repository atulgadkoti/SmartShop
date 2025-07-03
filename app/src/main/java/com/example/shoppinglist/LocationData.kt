package com.example.shoppinglist

data class LocationData(
    val latitude : Double,
    val longitude : Double
)

data class MyGeocodingResponse(
    val results : List<MyGeoCodingResults> ,
    val status : String
)

data class MyGeoCodingResults(
    var formatted_address : String
)

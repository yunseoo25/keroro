package com.example.myapplication

import androidx.compose.runtime.*
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

data class CandidatePlace(
    val name: String,
    val lat: Double,
    val lng: Double
)

object MainActivityHolder {

    var resultAddress by mutableStateOf("")

    var midLat by mutableStateOf(0.0)

    var midLng by mutableStateOf(0.0)

    var originCoords =
        mutableStateListOf<Pair<Double, Double>>()

    var inputLocations =
        mutableStateListOf<String>()

    var transitResults =
        mutableStateListOf<String>()

    var candidatePlaces =
        mutableStateListOf<CandidatePlace>()

    var transitResult by mutableStateOf("")
}

fun searchLocation(
    query: String,
    totalCount: Int,
    latList: MutableList<Double>,
    lngList: MutableList<Double>
) {

    val client = OkHttpClient()

    val url =
        "https://dapi.kakao.com/v2/local/search/keyword.json?query=$query"

    val request = Request.Builder()
        .url(url)
        .addHeader(
            "Authorization",
            "KakaoAK 60adc96b7c111bfffbe014fabd8f6649"
        )
        .build()

    client.newCall(request).enqueue(object : Callback {

        override fun onFailure(
            call: Call,
            e: IOException
        ) {

            println("실패: ${e.message}")
        }

        override fun onResponse(
            call: Call,
            response: Response
        ) {

            val body = response.body?.string()

            val json = JSONObject(body ?: return)

            val documents =
                json.getJSONArray("documents")

            if (documents.length() > 0) {

                val first =
                    documents.getJSONObject(0)

                val x =
                    first.getString("x").toDouble()

                val y =
                    first.getString("y").toDouble()

                synchronized(latList) {

                    lngList.add(x)

                    latList.add(y)

                    if (latList.size == totalCount) {

                        val midLat =
                            latList.average()

                        val midLng =
                            lngList.average()

                        MainActivityHolder.originCoords.clear()

                        latList.zip(lngList)
                            .forEach { (lat, lng) ->

                                MainActivityHolder.originCoords.add(
                                    Pair(lat, lng)
                                )
                            }

                        MainActivityHolder.midLat = midLat

                        MainActivityHolder.midLng = midLng

                        getAddressFromCoord(
                            midLat,
                            midLng
                        ) { address ->

                            MainActivityHolder.resultAddress =
                                address

                            searchCandidatePlaces(
                                midLat,
                                midLng
                            )
                        }
                    }
                }
            }
        }
    })
}

fun getAddressFromCoord(
    lat: Double,
    lng: Double,
    onResult: (String) -> Unit
) {

    val client = OkHttpClient()

    val url =
        "https://dapi.kakao.com/v2/local/geo/coord2address.json?x=$lng&y=$lat"

    val request = Request.Builder()
        .url(url)
        .addHeader(
            "Authorization",
            "KakaoAK 60adc96b7c111bfffbe014fabd8f6649"
        )
        .build()

    client.newCall(request).enqueue(object : Callback {

        override fun onFailure(
            call: Call,
            e: IOException
        ) {

            onResult("주소 변환 실패")
        }

        override fun onResponse(
            call: Call,
            response: Response
        ) {

            val body =
                response.body?.string()

            val json =
                JSONObject(body ?: return)

            val documents =
                json.getJSONArray("documents")

            if (documents.length() > 0) {

                val address =
                    documents
                        .getJSONObject(0)
                        .getJSONObject("address")
                        .getString("address_name")

                onResult(address)
            }
        }
    })
}

fun searchCandidatePlaces(
    lat: Double,
    lng: Double
) {

    val client = OkHttpClient()

    val url =
        "https://dapi.kakao.com/v2/local/search/category.json" +
                "?category_group_code=SW8" +
                "&x=$lng&y=$lat&radius=5000"

    val request = Request.Builder()
        .url(url)
        .addHeader(
            "Authorization",
            "KakaoAK 60adc96b7c111bfffbe014fabd8f6649"
        )
        .build()

    client.newCall(request).enqueue(object : Callback {

        override fun onFailure(
            call: Call,
            e: IOException
        ) {

            println("후보 검색 실패")
        }

        override fun onResponse(
            call: Call,
            response: Response
        ) {

            val body =
                response.body?.string()

            val json =
                JSONObject(body ?: return)

            val documents =
                json.getJSONArray("documents")

            MainActivityHolder.candidatePlaces.clear()

            for (i in 0 until minOf(3, documents.length())) {

                val item =
                    documents.getJSONObject(i)

                val place =
                    item.getString("place_name")

                val x =
                    item.getString("x").toDouble()

                val y =
                    item.getString("y").toDouble()

                MainActivityHolder.candidatePlaces.add(

                    CandidatePlace(
                        name = place,
                        lat = y,
                        lng = x
                    )
                )
            }
        }
    })
}

fun searchTransitTime(
    startLat: Double,
    startLng: Double,
    endLat: Double,
    endLng: Double,
    onResult: (String) -> Unit
) {

    val apiKey = "여기에_ODSAY_API_KEY"

    val client = OkHttpClient()

    val url =
        "https://api.odsay.com/v1/api/searchPubTransPathT?" +
                "SX=$startLng&SY=$startLat" +
                "&EX=$endLng&EY=$endLat" +
                "&apiKey=$apiKey"

    val request = Request.Builder()
        .url(url)
        .build()

    client.newCall(request).enqueue(object : Callback {

        override fun onFailure(
            call: Call,
            e: IOException
        ) {

            onResult("계산 실패")
        }

        override fun onResponse(
            call: Call,
            response: Response
        ) {

            val body =
                response.body?.string()

            val json =
                JSONObject(body ?: return)

            val result =
                json.getJSONObject("result")

            val path =
                result.getJSONArray("path")

            if (path.length() > 0) {

                val info =
                    path
                        .getJSONObject(0)
                        .getJSONObject("info")

                val totalTime =
                    info.getInt("totalTime")

                onResult(
                    "대중교통 약 ${totalTime}분"
                )
            }
        }
    })
}
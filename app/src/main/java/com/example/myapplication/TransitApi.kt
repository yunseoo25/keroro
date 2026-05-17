// searchTransit.kt
package com.example.myapplication

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import android.util.Log

fun searchTransit(
    startLat: Double,
    startLng: Double,
    endLat: Double,
    endLng: Double,
    onSuccess: (TransitResult) -> Unit,
    onFailure: (String) -> Unit
) {
    val client = OkHttpClient()

    val url = "https://apis.openapi.sk.com/transit/routes"

    val body = JSONObject().apply {
        put("startX", startLng)
        put("startY", startLat)
        put("endX", endLng)
        put("endY", endLat)
        put("count", 1)
        put("lang", 0)
        put("format", "json")
    }

    val request = Request.Builder()
        .url(url)
        .addHeader("appKey", "vwvE0lnfBZ6eqg9BZvRoI8GPZYWWsiu01gD1lQsb")
        .addHeader("Content-Type", "application/json")
        .post(body.toString().toRequestBody("application/json".toMediaType()))
        .build()

    client.newCall(request).enqueue(object : Callback {

        override fun onFailure(call: Call, e: IOException) {
            onFailure("경로 조회 실패: ${e.message}")
        }

        override fun onResponse(call: Call, response: Response) {
            try {
                val bodyStr = response.body?.string() ?: run {
                    onFailure("응답 없음")
                    return
                }
                Log.d("TRANSIT", bodyStr) // ← 여기 추가

                val json = JSONObject(bodyStr)
                val itinerary = json
                    .getJSONObject("metaData")
                    .getJSONObject("plan")
                    .getJSONArray("itineraries")
                    .getJSONObject(0)

                // ✅ 총 소요시간 (초 → 분)
                val totalTime = itinerary.getInt("totalTime") / 60

                // ✅ 총 요금
                val totalFare = itinerary
                    .getJSONObject("fare")
                    .getJSONObject("regular")
                    .getInt("totalFare")

                // ✅ 환승 횟수
                val transferCount = itinerary.getInt("transferCount")

                // ✅ 구간별 경로 파싱
                val legsArray = itinerary.getJSONArray("legs")
                val legs = mutableListOf<LegInfo>()

                for (i in 0 until legsArray.length()) {
                    val leg = legsArray.getJSONObject(i)
                    val mode = leg.getString("mode")

                    val name = when (mode) {
                        "WALK" -> "도보"
                        "BUS" -> "버스 " + leg.optString("route", "")        // ← getJSONObject 대신 optString
                        "SUBWAY" -> "지하철 " + leg.optString("route", "")   // ← getJSONObject 대신 optString
                        else -> mode
                    }

                    legs.add(
                        LegInfo(
                            mode        = mode,
                            sectionTime = leg.getInt("sectionTime") / 60,
                            distance    = leg.getInt("distance"),
                            name        = name,
                            startName   = leg.getJSONObject("start").getString("name"),
                            endName     = leg.getJSONObject("end").getString("name")
                        )
                    )
                }

                onSuccess(
                    TransitResult(
                        totalTime     = totalTime,
                        totalFare     = totalFare,
                        transferCount = transferCount,
                        legs          = legs
                    )
                )

            } catch (e: Exception) {
                onFailure("파싱 오류: ${e.message}")
            }
        }
    })
}
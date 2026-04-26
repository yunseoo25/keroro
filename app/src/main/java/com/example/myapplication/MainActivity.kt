package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.kakao.vectormap.*
import com.kakao.vectormap.route.*
import com.kakao.vectormap.label.*
import com.kakao.vectormap.MapView
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

object MainActivityHolder {
    var resultAddress by mutableStateOf("")
    var midLat by mutableStateOf(0.0)
    var midLng by mutableStateOf(0.0)
    var originCoords = mutableStateListOf<Pair<Double, Double>>()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        KakaoMapSdk.init(this, "224d7660a967055bf1291d5fc07aca22")
        setContent {
            MyScreen()
        }
    }
}

@Composable
fun MyScreen() {
    val locations = remember { mutableStateListOf("", "", "") }
    val resultAddress = MainActivityHolder.resultAddress
    val showMap = MainActivityHolder.midLat != 0.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        locations.forEachIndexed { index, value ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextField(
                    value = value,
                    onValueChange = { locations[index] = it },
                    label = { Text("출발지 ${index + 1}") },
                    modifier = Modifier.weight(1f)
                )
                if (locations.size > 1) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { locations.removeAt(index) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.width(56.dp)
                    ) {
                        Text("X")
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = { if (locations.size < 10) locations.add("") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("출발지 추가")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                val validLocations = locations.filter { it.isNotBlank() }
                if (validLocations.isEmpty()) return@Button

                MainActivityHolder.resultAddress = ""
                MainActivityHolder.midLat = 0.0
                MainActivityHolder.midLng = 0.0
                MainActivityHolder.originCoords.clear()

                val latList = mutableListOf<Double>()
                val lngList = mutableListOf<Double>()

                validLocations.forEach { location ->
                    searchLocation(location, validLocations.size, latList, lngList)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("중간지점 찾기")
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (resultAddress.isNotEmpty()) {
            Text(text = "중간지점: $resultAddress")
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (showMap) {
            KakaoMapView(
                midLat = MainActivityHolder.midLat,
                midLng = MainActivityHolder.midLng,
                origins = MainActivityHolder.originCoords.toList()
            )
        }
    }
}

@Composable
fun KakaoMapView(
    midLat: Double,
    midLng: Double,
    origins: List<Pair<Double, Double>>
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    // MapView를 remember로 유지 → recomposition 때 재생성 방지
    val mapView = remember { mutableStateOf<MapView?>(null) }

    AndroidView(
        factory = { context ->
            MapView(context).also { view ->
                mapView.value = view
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
    )

    // 라이프사이클 연결 → MapView가 Activity 생명주기를 따르게 함
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.value?.resume()
                Lifecycle.Event.ON_PAUSE  -> mapView.value?.pause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        mapView.value?.start(
            object : MapLifeCycleCallback() {
                override fun onMapDestroy() {
                    android.util.Log.d("KakaoMap", "지도 종료")
                }
                override fun onMapError(e: Exception?) {
                    android.util.Log.e("KakaoMap", "지도 오류: ${e?.message}")
                }
            },
            object : KakaoMapReadyCallback() {
                override fun getPosition(): LatLng = LatLng.from(midLat, midLng)
                override fun getZoomLevel(): Int = 13

                override fun onMapReady(map: KakaoMap) {
                    val labelManager = map.labelManager
                    val routeLineManager = map.routeLineManager

                    // 중간지점 마커 (빨간색)
                    val midStyle = labelManager?.addLabelStyles(
                        LabelStyles.from(LabelStyle.from(R.drawable.ic_mid_marker))
                    )
                    labelManager?.layer?.addLabel(
                        LabelOptions.from("mid", LatLng.from(midLat, midLng))
                            .setStyles(midStyle)
                            .setTexts(LabelTextBuilder().setTexts("중간지점"))
                    )

                    // 출발지 마커 + 루트선
                    origins.forEachIndexed { index, (lat, lng) ->
                        val originStyle = labelManager?.addLabelStyles(
                            LabelStyles.from(LabelStyle.from(R.drawable.ic_origin_marker))
                        )
                        labelManager?.layer?.addLabel(
                            LabelOptions.from("origin_$index", LatLng.from(lat, lng))
                                .setStyles(originStyle)
                                .setTexts(LabelTextBuilder().setTexts("출발지 ${index + 1}"))
                        )

                        val segment = RouteLineSegment.from(
                            listOf(
                                LatLng.from(lat, lng),
                                LatLng.from(midLat, midLng)
                            )
                        ).setStyles(
                            RouteLineStyle.from(8f, routeColors[index % routeColors.size])
                        )

                        routeLineManager?.layer?.addRouteLine(
                            RouteLineOptions.from(listOf(segment))
                        )
                    }
                }
            }
        )

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.value?.pause()
        }
    }
}

val routeColors = listOf(
    0xFF3498DB.toInt(),
    0xFFE74C3C.toInt(),
    0xFF2ECC71.toInt(),
    0xFFF39C12.toInt(),
    0xFF9B59B6.toInt(),
)

fun searchLocation(
    query: String,
    totalCount: Int,
    latList: MutableList<Double>,
    lngList: MutableList<Double>
) {
    val client = OkHttpClient()
    val url = "https://dapi.kakao.com/v2/local/search/keyword.json?query=$query"

    val request = Request.Builder()
        .url(url)
        .addHeader("Authorization", "KakaoAK 60adc96b7c111bfffbe014fabd8f6649")
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            println("실패: ${e.message}")
        }

        override fun onResponse(call: Call, response: Response) {
            val body = response.body?.string()
            val json = JSONObject(body ?: return)
            val documents = json.getJSONArray("documents")

            if (documents.length() > 0) {
                val first = documents.getJSONObject(0)
                val x = first.getString("x").toDouble()
                val y = first.getString("y").toDouble()

                synchronized(latList) {
                    lngList.add(x)
                    latList.add(y)
                    MainActivityHolder.originCoords.add(Pair(y, x))

                    if (latList.size == totalCount) {
                        val midLat = latList.average()
                        val midLng = lngList.average()

                        MainActivityHolder.midLat = midLat
                        MainActivityHolder.midLng = midLng

                        getAddressFromCoord(midLat, midLng) { address ->
                            MainActivityHolder.resultAddress = address
                        }
                    }
                }
            }
        }
    })
}

fun getAddressFromCoord(lat: Double, lng: Double, onResult: (String) -> Unit) {
    val client = OkHttpClient()
    val url = "https://dapi.kakao.com/v2/local/geo/coord2address.json?x=$lng&y=$lat"

    val request = Request.Builder()
        .url(url)
        .addHeader("Authorization", "KakaoAK 60adc96b7c111bfffbe014fabd8f6649")
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            onResult("주소 변환 실패")
        }

        override fun onResponse(call: Call, response: Response) {
            val body = response.body?.string()
            val json = JSONObject(body ?: return)
            val documents = json.getJSONArray("documents")

            if (documents.length() > 0) {
                val address = documents.getJSONObject(0)
                    .getJSONObject("address")
                    .getString("address_name")

                onResult(address)
            }
        }
    })
}
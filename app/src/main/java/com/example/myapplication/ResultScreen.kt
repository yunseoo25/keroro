package com.example.myapplication

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import android.util.Log

@Composable
fun ResultScreen(
    transitResults: List<TransitResult?>,
    onBack: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F3FA))
    ) {

        // 상단바
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(vertical = 18.dp)
        ) {

            TextButton(
                onClick = {
                    onBack()
                },
                modifier = Modifier.align(Alignment.CenterStart)
            ) {

                Text(
                    text = "←",
                    fontSize = 28.sp,
                    color = Color(0xFF111111)
                )
            }

            Text(
                text = "중간지점 결과",
                modifier = Modifier.align(Alignment.Center),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111111)
            )

            Text(
                text = "↗",
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 20.dp),
                fontSize = 24.sp,
                color = Color(0xFF9B7BDB)
            )
        }

        // 실제 카카오맵
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {

            KakaoMapScreen(
                modifier = Modifier.fillMaxSize()
            )

            // A 위치
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 40.dp, top = 80.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = Color(0xFF9B7BDB)
                ) {

                    Text(
                        text = "A",
                        modifier = Modifier.padding(
                            horizontal = 22.dp,
                            vertical = 14.dp
                        ),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFF8F6FD2)
                ) {

                    Text(
                        text = MainActivityHolder.inputLocations.getOrNull(0)
                            ?: "",
                        modifier = Modifier.padding(
                            horizontal = 22.dp,
                            vertical = 10.dp
                        ),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }

            // B 위치
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 40.dp, top = 80.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = Color(0xFF9B7BDB)
                ) {

                    Text(
                        text = "B",
                        modifier = Modifier.padding(
                            horizontal = 22.dp,
                            vertical = 14.dp
                        ),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFF8F6FD2)
                ) {

                    Text(
                        text = MainActivityHolder.inputLocations.getOrNull(1)
                            ?: "",
                        modifier = Modifier.padding(
                            horizontal = 22.dp,
                            vertical = 10.dp
                        ),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }

            // 중간지점
            Column(
                modifier = Modifier
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = Color.White,
                    shadowElevation = 12.dp
                ) {

                    Image(
                        painter = painterResource(id = R.drawable.mapick_logo),
                        contentDescription = null,
                        modifier = Modifier
                            .size(110.dp)
                            .padding(20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFF8F6FD2)
                ) {

                    Text(
                        text = MainActivityHolder.resultAddress,
                        modifier = Modifier.padding(
                            horizontal = 28.dp,
                            vertical = 14.dp
                        ),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            }
        }

        // 하단 카드
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Color.White,
                    RoundedCornerShape(
                        topStart = 34.dp,
                        topEnd = 34.dp
                    )
                )
                .padding(28.dp)
        ) {

            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(6.dp)
                    .background(
                        Color(0xFFD0D3DA),
                        RoundedCornerShape(100.dp)
                    )
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Image(
                    painter = painterResource(id = R.drawable.ic_location),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(Color(0xFF9B7BDB)),
                    modifier = Modifier.size(28.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "중간지점 · ${MainActivityHolder.resultAddress}",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111111)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text =
                    if (MainActivityHolder.transitResults.isNotEmpty())
                        MainActivityHolder.transitResults[0]
                    else
                        "이동 시간 계산중...",
                fontSize = 15.sp,
                color = Color(0xFF666D80)
            )

            Spacer(modifier = Modifier.height(26.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                CategoryButton("맛집", true)

                CategoryButton("카페", false)

                CategoryButton("놀거리", false)
            }
        }
    }
}

@Composable
fun CategoryButton(
    title: String,
    selected: Boolean
) {

    Surface(
        shape = RoundedCornerShape(22.dp),
        color =
            if (selected)
                Color(0xFF9B7BDB)
            else
                Color(0xFFF3F3F5)
    ) {

        Text(
            text = title,
            modifier = Modifier.padding(
                horizontal = 24.dp,
                vertical = 14.dp
            ),
            color =
                if (selected)
                    Color.White
                else
                    Color(0xFF5F6678),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}

@Composable
fun KakaoMapScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val mapView = remember { MapView(context) }

    // ✅ 지도 시작 여부 추적
    var isMapStarted by remember { mutableStateOf(false) }

    AndroidView(
        factory = {
            mapView.start(
                object : MapLifeCycleCallback() {
                    override fun onMapDestroy() {
                        Log.d("KAKAO", "onMapDestroy 호출됨")
                    }
                    override fun onMapError(exception: Exception?) {
                        Log.d("KAKAO", "onMapError: ${exception?.message}")
                    }
                },
                object : KakaoMapReadyCallback() {
                    override fun getPosition(): LatLng {
                        return LatLng.from(
                            MainActivityHolder.midLat,
                            MainActivityHolder.midLng
                        )
                    }
                    override fun getZoomLevel(): Int = 13
                    override fun onMapReady(kakaoMap: KakaoMap) {
                        isMapStarted = true  // ✅ 준비 완료 표시
                    }
                }
            )
            mapView
        },
        modifier = modifier
    )

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    if (isMapStarted) mapView.resume()  // ✅ 준비된 후에만 resume
                }
                Lifecycle.Event.ON_PAUSE -> {
                    if (isMapStarted) mapView.pause()   // ✅ 준비된 후에만 pause
                }
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            if (isMapStarted) mapView.pause()
            mapView.finish()  // ✅ 항상 finish
        }
    }
}
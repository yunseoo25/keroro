package com.example.myapplication

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.kakao.vectormap.KakaoMapSdk
import java.security.MessageDigest

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 키 해시 확인용 (등록 후 삭제해도 됨)
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                val info = packageManager.getPackageInfo(
                    packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                )
                info.signingInfo?.apkContentsSigners?.forEach { signature ->
                    val md = MessageDigest.getInstance("SHA")
                    md.update(signature.toByteArray())
                    val keyHash = Base64.encodeToString(md.digest(), Base64.DEFAULT)
                    Log.d("KAKAO_KEY", keyHash)
                }
            } else {
                @Suppress("DEPRECATION")
                val info = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
                @Suppress("DEPRECATION")
                info.signatures?.forEach { signature ->
                    val md = MessageDigest.getInstance("SHA")
                    md.update(signature.toByteArray())
                    val keyHash = Base64.encodeToString(md.digest(), Base64.DEFAULT)
                    Log.d("KAKAO_KEY", keyHash)
                }
            }
        } catch (e: Exception) {
            Log.d("KAKAO_KEY", "오류: ${e.message}")
        }

        KakaoMapSdk.init(this, "224d7660a967055bf1291d5fc07aca22")

        setContent {

            var currentScreen by remember { mutableStateOf("splash") }

            // ✅ 여러 출발지 결과 리스트로 변경
            var transitResults by remember { mutableStateOf<List<TransitResult?>>(emptyList()) }

            when (currentScreen) {

                "splash" -> {
                    SplashScreen(
                        onFinish = {
                            currentScreen = "onboarding"
                        }
                    )
                }

                "onboarding" -> {
                    OnboardingScreen(
                        onStart = {
                            currentScreen = "main"
                        }
                    )
                }

                "main" -> {
                    MyScreen(
                        onSearchClick = {
                            currentScreen = "candidate"
                        }
                    )
                }

                "candidate" -> {
                    CandidateScreen(
                        onBack = {
                            currentScreen = "main"
                        },
                        onPlaceSearch = { endLat, endLng ->

                            val coords = MainActivityHolder.originCoords
                            Log.d("TRANSIT", "originCoords 개수: ${coords.size}")  // ← 추가
                            Log.d("TRANSIT", "endLat: $endLat, endLng: $endLng")



                            // ✅ 결과를 인덱스 순서대로 저장하기 위해 배열 사용
                            val results = arrayOfNulls<TransitResult?>(coords.size)
                            var completedCount = 0

                            coords.forEachIndexed { index, (startLat, startLng) ->
                                searchTransit(
                                    startLat = startLat,
                                    startLng = startLng,
                                    endLat = endLat,
                                    endLng = endLng,
                                    onSuccess = { result ->
                                        runOnUiThread {
                                            results[index] = result
                                            completedCount++
                                            if (completedCount == coords.size) {
                                                transitResults = results.toList()
                                                currentScreen = "result"
                                            }
                                        }
                                    },
                                    onFailure = { _ ->
                                        runOnUiThread {
                                            results[index] = null
                                            completedCount++
                                            if (completedCount == coords.size) {
                                                transitResults = results.toList()
                                                currentScreen = "result"
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    )
                }

                "result" -> {
                    ResultScreen(
                        transitResults = transitResults,
                        onBack = {
                            currentScreen = "candidate"
                        }
                    )
                }
            }
        }
    }
}
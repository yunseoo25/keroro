package com.example.myapplication

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CandidateScreen(
    onBack: () -> Unit,
    onPlaceSearch: (endLat: Double, endLng: Double) -> Unit
) {
    var selectedPlace by remember { mutableStateOf(0) }
    var selectedTransport by remember { mutableStateOf("대중교통") }
    var transitSummary by remember { mutableStateOf("이동 수단을 선택해주세요") }
    var isLoading by remember { mutableStateOf(false) }

    val candidates = MainActivityHolder.candidatePlaces

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
                onClick = { onBack() },
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Text(text = "←", fontSize = 28.sp, color = Color(0xFF111111))
            }

            Text(
                text = "중간지점 선택",
                modifier = Modifier.align(Alignment.Center),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111111)
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {

            item {
                Text(
                    text = "중간지점 후보",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111111)
                )
            }

            items(candidates.size) { index ->
                val item = candidates[index]
                CandidateCard(
                    title = item.name,
                    subtitle = "중간 후보 장소",
                    time = "",
                    selected = selectedPlace == index,
                    onClick = {
                        selectedPlace = index
                        MainActivityHolder.resultAddress = item.name
                        transitSummary = "이동 수단을 선택해주세요"
                    }
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {

                        Text(
                            text = "이동 수단",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {

                            TransportButton(
                                title = "대중교통",
                                selected = selectedTransport == "대중교통",
                                onClick = {
                                    selectedTransport = "대중교통"

                                    if (
                                        MainActivityHolder.originCoords.isNotEmpty()
                                        && candidates.isNotEmpty()
                                    ) {
                                        val selectedCandidate = candidates[selectedPlace]
                                        transitSummary = "계산중..."

                                        searchTransit(
                                            startLat = MainActivityHolder.originCoords[0].first,
                                            startLng = MainActivityHolder.originCoords[0].second,
                                            endLat = selectedCandidate.lat,
                                            endLng = selectedCandidate.lng,
                                            onSuccess = { result ->
                                                transitSummary = buildString {
                                                    appendLine("약 ${result.totalTime}분 소요")
                                                    appendLine("요금 ${result.totalFare}원")
                                                    appendLine("환승 ${result.transferCount}회")
                                                    appendLine()
                                                    appendLine("── 경로 ──")
                                                    result.legs.forEach { leg ->
                                                        when (leg.mode) {
                                                            "WALK" -> appendLine("🚶 도보 ${leg.sectionTime}분 (${leg.distance}m)")
                                                            "SUBWAY" -> appendLine("🚇 ${leg.name} 탑승")
                                                                .also { appendLine("   ${leg.startName} → ${leg.endName} (${leg.sectionTime}분)") }
                                                            "BUS" -> appendLine("🚌 ${leg.name} 탑승")
                                                                .also { appendLine("   ${leg.startName} → ${leg.endName} (${leg.sectionTime}분)") }
                                                        }
                                                    }
                                                }
                                            },
                                            onFailure = { error ->
                                                transitSummary = error
                                            }
                                        )
                                    }
                                }
                            )

                            TransportButton(
                                title = "자동차",
                                selected = selectedTransport == "자동차",
                                onClick = {
                                    selectedTransport = "자동차"
                                    transitSummary = "자동차 예상 25분"
                                }
                            )

                            TransportButton(
                                title = "도보",
                                selected = selectedTransport == "도보",
                                onClick = {
                                    selectedTransport = "도보"
                                    transitSummary = "도보 예상 1시간 12분"
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = transitSummary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF9B7BDB)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(120.dp))
            }
        }

        Button(
            onClick = {
                if (candidates.isNotEmpty() && !isLoading) {
                    isLoading=true
                    val selectedCandidate = candidates[selectedPlace]
                    onPlaceSearch(

                        selectedCandidate.lat,
                        selectedCandidate.lng
                    )
                }
            },
            enabled = !isLoading,  // ← 로딩 중 비활성화

            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .height(72.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF9B7BDB)
            )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "주변 장소 찾기",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

    }
} // ← CandidateScreen 끝

// ─────────────────────────────────────────

@Composable
fun CandidateCard(
    title: String,
    subtitle: String,
    time: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) Color(0xFF9B7BDB) else Color.White
        )
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .background(Color(0xFFF2EBFF), RoundedCornerShape(100.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_location),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(Color(0xFF9B7BDB)),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = title,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selected) Color.White else Color(0xFF111111)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        fontSize = 18.sp,
                        color = if (selected) Color.White else Color(0xFF777777)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────

@Composable
fun TransportButton(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .size(width = 100.dp, height = 120.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) Color(0xFF9B7BDB) else Color(0xFFF7F7F9)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = when (title) {
                    "대중교통" -> "🚌"
                    "자동차" -> "🚗"
                    else -> "🚶"
                },
                fontSize = 28.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (selected) Color.White else Color(0xFF555555)
            )
        }
    }
}
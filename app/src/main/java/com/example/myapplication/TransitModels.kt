package com.example.myapplication


data class TransitResult(
    val totalTime: Int,          // 총 소요시간 (분)
    val totalFare: Int,          // 총 요금 (원)
    val transferCount: Int,      // 환승 횟수
    val legs: List<LegInfo>      // 구간별 경로
)

data class LegInfo(
    val mode: String,            // 이동 수단 (WALK, BUS, SUBWAY 등)
    val sectionTime: Int,        // 구간 소요시간 (분)
    val distance: Int,           // 구간 거리 (m)
    val name: String,            // 노선명 또는 "도보"
    val startName: String,       // 출발지명
    val endName: String          // 도착지명
)
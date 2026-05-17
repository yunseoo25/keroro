package com.example.myapplication

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun MyScreen(
    onSearchClick: () -> Unit
) {

    val locations = remember {
        mutableStateListOf("", "")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F4F4))
    ) {

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {

            Spacer(modifier = Modifier.height(18.dp))

            // 상단 로고 영역
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White)
                    .padding(horizontal = 14.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Image(
                    painter = painterResource(id = R.drawable.mapick_logo),
                    contentDescription = null,
                    modifier = Modifier.size(52.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Mapick",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111111)
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            // 출발지 카드
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp),
                shape = RoundedCornerShape(30.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {

                Column(
                    modifier = Modifier.padding(24.dp)
                ) {

                    Text(
                        text = "출발지 설정",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111111)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    locations.forEachIndexed { index, value ->

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF0EAFE)),
                                contentAlignment = Alignment.Center
                            ) {

                                Image(
                                    painter = painterResource(id = R.drawable.ic_location),
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            TextField(
                                value = value,
                                onValueChange = {
                                    locations[index] = it
                                },
                                placeholder = {

                                    Text(
                                        text = "위치를 입력해주세요",
                                        color = Color(0xFFB8B8B8),
                                        fontSize = 16.sp
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(62.dp),
                                shape = RoundedCornerShape(18.dp),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFFF8F8F8),
                                    unfocusedContainerColor = Color(0xFFF8F8F8),
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    OutlinedButton(
                        onClick = {
                            if (locations.size < 10) {
                                locations.add("")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(62.dp),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(
                            1.dp,
                            Color(0xFFB999F2)
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF9B7BDB)
                        )
                    ) {

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {

                            Image(
                                painter = painterResource(id = R.drawable.ic_plus),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(Color(0xFF9B7BDB)),
                                modifier = Modifier.size(18.dp)
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            Text(
                                text = "위치 추가 (최대 10인)",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
        Button(
            onClick = {

                val validLocations = locations.filter {
                    it.isNotBlank()
                }

                if (validLocations.isEmpty()) return@Button

                MainActivityHolder.inputLocations.clear()

                MainActivityHolder.inputLocations.addAll(validLocations)

                MainActivityHolder.resultAddress = ""
                MainActivityHolder.midLat = 0.0
                MainActivityHolder.midLng = 0.0
                MainActivityHolder.originCoords.clear()

                val latList = mutableListOf<Double>()
                val lngList = mutableListOf<Double>()

                validLocations.forEach { location ->
                    searchLocation(
                        location,
                        validLocations.size,
                        latList,
                        lngList
                    )
                }

                onSearchClick()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height(72.dp),
            shape = RoundedCornerShape(22.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF9B7BDB)
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 10.dp
            )
        ) {

            Text(
                text = "중간지점 검색",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 하단 네비
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(
                    start = 30.dp,
                    end = 30.dp,
                    top = 10.dp,
                    bottom = 18.dp
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            BottomItem(R.drawable.ic_home, "홈", true)

            BottomItem(R.drawable.ic_history, "기록", false)

            BottomItem(R.drawable.ic_favorite, "즐겨찾기", false)

            BottomItem(R.drawable.ic_setting, "설정", false)
        }
    }
}
@Composable
fun BottomItem(
    iconRes: Int,
    title: String,
    selected: Boolean
) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Image(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            colorFilter = ColorFilter.tint(
                if (selected)
                    Color(0xFF9B7BDB)
                else
                    Color(0xFF9EA4B5)
            ),
            modifier = Modifier.size(30.dp)
        )

        Spacer(modifier = Modifier.height(1.dp))

        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = if (selected)
                Color(0xFF9B7BDB)
            else
                Color(0xFF9EA4B5)
        )
    }
}
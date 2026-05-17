package com.example.myapplication

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource

@Composable
fun OnboardingScreen(
    onStart: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(80.dp))

        Box(
            modifier = Modifier
                .size(180.dp)
                .clip(RoundedCornerShape(40.dp))
                .background(Color(0xFFEDE7F6)),
            contentAlignment = Alignment.Center
        ) {

            Image(
                painter = painterResource(id = R.drawable.mapick_logo),
                contentDescription = null,
                modifier = Modifier.size(90.dp)
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "모두의 중간 지점을\n한 번에 찾아요",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = Color(0xFF111111)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "각자 출발지를 입력하면\nMapick이 최적의 만남 장소를\n자동으로 계산해드려요",
            fontSize = 18.sp,
            lineHeight = 28.sp,
            textAlign = TextAlign.Center,
            color = Color(0xFF666666)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Box(
                modifier = Modifier
                    .width(28.dp)
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF9B7BDB))
            )

            repeat(2) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onStart,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF9B7BDB)
            )
        ) {

            Text(
                text = "다음",
                fontSize = 20.sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        TextButton(
            onClick = onStart
        ) {

            Text(
                text = "건너뛰기",
                color = Color.Gray,
                fontSize = 18.sp
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}
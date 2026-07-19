package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.ActiveCallState
import com.example.ui.viewmodel.ContactsViewModel
import java.util.Locale

@Composable
fun ActiveCallScreen(
    viewModel: ContactsViewModel,
    callState: ActiveCallState
) {
    // Pulse animation for avatar background to represent phone ringing/connecting
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // Call screens are traditionally dark, sleek, and high-contrast
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)) // Custom dark cosmic blue
            .padding(24.dp)
            .testTag("active_call_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top status
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 40.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (callState.isIncoming) Color(0xFFFFB300) else Color(0xFF4CAF50))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (callState.isIncoming) "مكالمة واردة آمنة" else "هوية ذكية مفعلة",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (callState.callEnded) "تم إنهاء المكالمة" else if (callState.isIncoming) "يرن..." else if (callState.durationSeconds == 0) "جاري الاتصال..." else "مكالمة نشطة",
                color = if (callState.callEnded) Color(0xFFEF5350) else Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            if (!callState.isIncoming && !callState.callEnded && callState.durationSeconds > 0) {
                val minutes = callState.durationSeconds / 60
                val seconds = callState.durationSeconds % 60
                Text(
                    text = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds),
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        // Midsection Avatar
        Box(
            modifier = Modifier.size(180.dp),
            contentAlignment = Alignment.Center
        ) {
            // Pulse rings
            if (!callState.callEnded && (callState.isIncoming || callState.durationSeconds == 0)) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(Color(callState.displayColor).copy(alpha = 0.2f))
                )
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .scale(pulseScale * 0.85f)
                        .clip(CircleShape)
                        .background(Color(callState.displayColor).copy(alpha = 0.1f))
                )
            }

            // Main Core Avatar Circle
            Box(
                modifier = Modifier
                    .size(104.dp)
                    .clip(CircleShape)
                    .background(Color(callState.displayColor)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = callState.displayEmoji,
                    fontSize = 52.sp
                )
            }
        }

        // Contact credentials (Display Name & Display Phone Number)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = callState.displayName,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = callState.displayNumber,
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )

            // Small watermark hint
            Text(
                text = "الرقم الحقيقي: " + callState.realNumber,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.25f),
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Call Control Buttons Actions
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (callState.isIncoming && !callState.callEnded) {
                // Incoming Actions: Answer (Green) or Reject (Red)
                Row(
                    modifier = Modifier.fillMaxWidth(0.8f),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Reject Button
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = { viewModel.endCall() },
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEF5350))
                                .testTag("reject_call_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CallEnd,
                                contentDescription = "رفض",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "رفض", color = Color.White, fontSize = 12.sp)
                    }

                    // Answer Button
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = { viewModel.answerIncomingCall() },
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4CAF50))
                                .testTag("answer_call_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "رد",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "رد", color = Color.White, fontSize = 12.sp)
                    }
                }
            } else {
                // Active Dialing Controls: Mute, Speaker, Keypad, End Call
                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(bottom = 32.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mute
                    IconButton(
                        onClick = { viewModel.toggleMute() },
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(
                                if (callState.isMuted) Color.White.copy(alpha = 0.25f)
                                else Color.White.copy(alpha = 0.08f)
                            )
                    ) {
                        Icon(
                            imageVector = if (callState.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "كتم الصوت",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Speaker
                    IconButton(
                        onClick = { viewModel.toggleSpeaker() },
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(
                                if (callState.isSpeakerOn) Color.White.copy(alpha = 0.25f)
                                else Color.White.copy(alpha = 0.08f)
                            )
                    ) {
                        Icon(
                            imageVector = if (callState.isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                            contentDescription = "مكبر الصوت",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Simulated Keypad placeholder
                    IconButton(
                        onClick = {},
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Dialpad,
                            contentDescription = "لوحة مفاتيح",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Main End Call Button (Big Red)
                IconButton(
                    onClick = { viewModel.endCall() },
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEF5350))
                        .testTag("end_call_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "إنهاء المكالمة",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

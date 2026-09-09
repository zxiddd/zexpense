package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.SyncStatus
import com.example.ui.theme.AmberLight
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoseError
import com.example.ui.theme.RoseLight

@Composable
fun SyncStatusIndicator(
    status: SyncStatus,
    modifier: Modifier = Modifier,
    showLabel: Boolean = true
) {
    val (bgColor, tintColor, text, icon) = when (status) {
        SyncStatus.SYNCED -> Quad(
            EmeraldLight,
            EmeraldSuccess,
            "Synced",
            Icons.Default.CheckCircle
        )
        SyncStatus.PENDING -> Quad(
            AmberLight,
            AmberWarning,
            "Pending",
            Icons.Default.Schedule
        )
        SyncStatus.FAILED -> Quad(
            RoseLight,
            RoseError,
            "Failed",
            Icons.Default.Error
        )
    }

    Row(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(8.dp))
            .padding(horizontal = if (showLabel) 7.dp else 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = tintColor,
            modifier = Modifier.size(11.dp)
        )
        if (showLabel) {
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = text.uppercase(),
                color = tintColor,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.4.sp
            )
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

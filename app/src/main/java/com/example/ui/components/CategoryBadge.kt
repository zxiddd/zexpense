package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberLight
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.TealBorder
import com.example.ui.theme.TealLight
import com.example.ui.theme.TealPrimary

fun getCategoryConfig(category: String): Triple<ImageVector, Color, Color> {
    return when (category.lowercase()) {
        "petrol", "fuel", "gas" -> Triple(
            Icons.Default.LocalGasStation,
            TealPrimary,
            TealLight
        )
        "parking" -> Triple(
            Icons.Default.LocalParking,
            EmeraldSuccess,
            EmeraldLight
        )
        "maintenance", "repairs" -> Triple(
            Icons.Default.Build,
            Color(0xFF7C3AED),
            Color(0xFFEDE9FE)
        )
        "food", "meals" -> Triple(
            Icons.Default.Fastfood,
            Color(0xFFE11D48),
            Color(0xFFFFE4E6)
        )
        "other", "site supplies", "supplies" -> Triple(
            Icons.Default.ShoppingBag,
            AmberWarning,
            AmberLight
        )
        else -> Triple(
            Icons.Default.Category,
            TealPrimary,
            TealLight
        )
    }
}

@Composable
fun CategoryIconBadge(
    category: String,
    modifier: Modifier = Modifier,
    size: Int = 40
) {
    val (icon, tintColor, bg) = getCategoryConfig(category)

    Box(
        modifier = modifier
            .size(size.dp)
            .background(bg, RoundedCornerShape(14.dp))
            .border(1.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = category,
            tint = tintColor,
            modifier = Modifier.size((size * 0.55).dp)
        )
    }
}

@Composable
fun CategoryPill(
    category: String,
    modifier: Modifier = Modifier
) {
    val (icon, tintColor, bg) = getCategoryConfig(category)

    Row(
        modifier = modifier
            .background(bg, RoundedCornerShape(10.dp))
            .border(1.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tintColor,
            modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = category,
            color = tintColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}


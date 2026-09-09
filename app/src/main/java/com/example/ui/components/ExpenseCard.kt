package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.local.ExpenseEntity
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GlassBorderLight
import com.example.ui.theme.GlassSurfaceLight
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate900
import com.example.ui.theme.TealLight
import com.example.ui.theme.TealPrimary
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ExpenseListItem(
    expense: ExpenseEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val hasImage = !expense.localImagePath.isNullOrBlank() && File(expense.localImagePath).exists()
    val dateFormatted = SimpleDateFormat("MMM dd", Locale.US).format(Date(expense.dateMillis))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = GlassSurfaceLight),
        border = BorderStroke(1.dp, GlassBorderLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Receipt Thumbnail or Category Icon
            if (hasImage) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, Color.White, RoundedCornerShape(14.dp))
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(File(expense.localImagePath!!))
                            .crossfade(true)
                            .build(),
                        contentDescription = "Receipt",
                        modifier = Modifier.size(48.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            } else {
                CategoryIconBadge(category = expense.category, size = 48)
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Vendor & Category + Date
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.vendor.ifBlank { expense.category },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$dateFormatted • ${expense.category}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = Slate500
                )
            }

            // Amount and Receipt attached badge
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = String.format(Locale.US, "%.2f", expense.amount),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = expense.currency,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Slate500
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))

                if (hasImage) {
                    Row(
                        modifier = Modifier
                            .background(TealLight, RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = TealPrimary,
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "RECEIPT",
                            color = TealPrimary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .background(Color(0xFFF1F5F9), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "MANUAL",
                            color = Slate500,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

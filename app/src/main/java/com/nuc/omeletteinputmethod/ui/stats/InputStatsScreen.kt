package com.nuc.omeletteinputmethod.ui.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputStatsScreen(
    onNavigateBack: () -> Unit,
    viewModel: InputStatsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("输入统计") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { innerPadding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("加载中...", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                StatsSummaryCards(state)

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "输入热力图",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                MonthHeatmap(state, viewModel::navigateMonth)

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "最近30天趋势",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                TrendLineChart(state.dailyStats)

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "最常用词 TOP 20",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                TopWordsGrid(state.topWords)

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun StatsSummaryCards(state: InputStatsUiState) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        StatCard("总输入字数", "${state.totalChars}", Modifier.weight(1f))
        StatCard("日均字数", "${state.dailyAvg}", Modifier.weight(1f))
        StatCard("活跃天数", "${state.activeDays}", Modifier.weight(1f))
    }
}

@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier =
            modifier
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun MonthHeatmap(
    state: InputStatsUiState,
    onNavigateMonth: (Int) -> Unit,
) {
    val cal = remember { Calendar.getInstance() }
    val parts = state.currentMonth.split("-")
    val year = parts.getOrNull(0)?.toIntOrNull() ?: cal.get(Calendar.YEAR)
    val month = (parts.getOrNull(1)?.toIntOrNull() ?: cal.get(Calendar.MONTH) + 1) - 1

    cal.set(Calendar.YEAR, year)
    cal.set(Calendar.MONTH, month)
    cal.set(Calendar.DAY_OF_MONTH, 1)
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = { onNavigateMonth(-1) }) {
            Icon(Icons.Filled.ChevronLeft, "上月")
        }
        Text(state.currentMonth, fontWeight = FontWeight.Medium)
        IconButton(onClick = { onNavigateMonth(1) }) {
            Icon(Icons.Filled.ChevronRight, "下月")
        }
    }

    val dayHeaders = listOf("日", "一", "二", "三", "四", "五", "六")
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        dayHeaders.forEach { d ->
            Text(d, modifier = Modifier.width(36.dp), textAlign = TextAlign.Center, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            val cells = mutableListOf<String>()
            for (i in 1..daysInMonth) {
                val padded = if (i < 10) "0$i" else "$i"
                cells.add("${state.currentMonth}-$padded")
            }
            val offset = (firstDayOfWeek - 1) % 7
            val totalCells = cells.size + offset
            val rows = (totalCells + 6) / 7

            val maxChars = state.heatmapData.values.maxOrNull()?.coerceAtLeast(1) ?: 1

            for (r in 0 until rows) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (c in 0 until 7) {
                        val cellIdx = r * 7 + c - offset
                        Box(
                            modifier =
                                Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                        ) {
                            if (cellIdx in cells.indices) {
                                val dateKey = cells[cellIdx]
                                val chars = state.heatmapData[dateKey] ?: 0
                                val intensity = if (maxChars > 0) chars.toFloat() / maxChars else 0f
                                val cellColor =
                                    if (chars == 0) {
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                    } else {
                                        Color(0xFF4CAF50).copy(alpha = 0.1f + intensity * 0.9f)
                                    }
                                Box(
                                    modifier =
                                        Modifier
                                            .fillMaxSize()
                                            .background(cellColor),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        "${cellIdx + 1}",
                                        fontSize = 10.sp,
                                        color = if (chars > 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    )
                                }
                            } else {
                                Box(modifier = Modifier.fillMaxSize())
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TrendLineChart(dailyStats: List<DailyStatItem>) {
    if (dailyStats.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.TrendingUp, "无数据", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f), modifier = Modifier.size(48.dp))
        }
        return
    }

    val maxChars = dailyStats.maxOf { it.totalChars }.coerceAtLeast(1)
    val lineColor = MaterialTheme.colorScheme.primary

    Canvas(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                .padding(8.dp),
    ) {
        val width = size.width
        val height = size.height
        val paddingH = 40f
        val paddingV = 30f
        val chartWidth = width - paddingH * 2
        val chartHeight = height - paddingV * 2

        val path = Path()
        val n = dailyStats.size
        val stepX = if (n > 1) chartWidth / (n - 1) else chartWidth

        for (i in dailyStats.indices) {
            val x = paddingH + i * stepX
            val y = paddingV + chartHeight * (1f - dailyStats[i].totalChars.toFloat() / maxChars)
            if (i == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 3f),
        )
    }
}

@Composable
fun TopWordsGrid(words: List<String>) {
    val columns = 4
    val chunked = words.chunked(columns)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        for (row in chunked) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                for (word in row) {
                    Box(
                        modifier =
                            Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            word,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
                repeat(columns - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
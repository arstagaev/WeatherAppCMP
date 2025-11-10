package com.tagaev.ui.main_weather_screen
import androidx.compose.foundation.clickable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.foundation.layout.offset


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tagaev.data.models.Resource
import com.tagaev.data.models.forecast_domain.CurrentForecast
import com.tagaev.data.models.forecast_domain.MainForecast
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.math.pow
import kotlin.math.roundToInt

import kotlinx.coroutines.delay
import com.tagaev.data.utils.nowEpochMs

/**
 * Weather main screen bound to [IMainWeatherComponent].
 *
 * Top: shimmering loading stripe.
 * Plates (from top to bottom):
 *  1) Current conditions (temp big, wind, coordinates, sunrise/sunset)
 *  2) Day 0 temperature chart (horizontal scroll)
 *  3) Day 1 temperature chart (horizontal scroll)
 *  4) Day 2 temperature chart (horizontal scroll)
 */
@Composable
fun MainWeatherScreen(component: IMainWeatherComponent) {
    val resource by component.resource.collectAsState()
    val isRefreshing = resource is Resource.Loading
    val pullState = rememberPullToRefreshState()
    val alertMessage by component.alertMessage.collectAsState(null)

    var shimmerUntil by remember { mutableStateOf(0L) }
    var tick by remember { mutableStateOf(0) }

    // Whenever we enter Loading (initial or programmatic), guarantee a minimum shimmer window
    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            val now = nowEpochMs()
            val target = now + 2000L // 2 seconds minimum shimmer
            if (target > shimmerUntil) shimmerUntil = target
        }
    }

    // Wake the UI when the shimmer window elapses so it can hide the stripe
    LaunchedEffect(shimmerUntil) {
        if (shimmerUntil > 0L) {
            val now = nowEpochMs()
            val remaining = shimmerUntil - now
            if (remaining > 0) delay(remaining)
            tick++
        }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            val now = nowEpochMs()
            val target = now + 2000L
            if (target > shimmerUntil) shimmerUntil = target
            component.onRefreshForecast()
        },
        state = pullState
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Full‑bleed rainbow stripe at the very top (thicker) — draw FIRST so it stays behind the top card
            @Suppress("UNUSED_VARIABLE")
            val __tick = tick // observe to recompose after delay
            val showStripe = isRefreshing || nowEpochMs() < shimmerUntil
            ShimmerStripe(
                visible = showStripe,
                height = 48.dp,           // total stripe + glow height
                bandHeight = 12.dp,       // visible rainbow band height at the top
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopStart)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(12.dp)
            ) {
                Spacer(Modifier.height(12.dp))
                when (val res = resource) {
                    is Resource.Success -> WeatherContent(
                        data = res.data,
                        onChangeCoordinates = { lat, lon -> component.onChangeCoordinates(lat, lon) }
                    )
                    is Resource.Error -> ErrorPlate(message = res.causes ?: res.exception?.message ?: "Не удалось загрузить")
                    Resource.Loading -> PlaceholderPlates()
                }
            }
            if (alertMessage != null) {
                AlertDialog(
                    onDismissRequest = { component.dismissAlert() },
                    title = { Text("Проблема с сетью") },
                    text = { Text(alertMessage!!) },
                    confirmButton = {
                        TextButton(onClick = {
                            component.dismissAlert()
                            component.onRefreshForecast()
                        }) { Text("Повторить") }
                    },
                    dismissButton = {
                        TextButton(onClick = { component.dismissAlert() }) { Text("ОК") }
                    }
                )
            }
        }
    }
}

// ------------------------- CONTENT -------------------------

@Composable
private fun WeatherContent(data: MainForecast, onChangeCoordinates: (Float, Float) -> Unit) {
    println(">>> WeatherContent ${data.toString()}")
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        CurrentPlate(data, onChangeCoordinates)

        // Build per-day buckets (today + next two days)
        val buckets: Map<LocalDate, List<CurrentForecast>> = remember(data.listOfDaysForecast) {
            data.listOfDaysForecast.groupByDay()
        }
        println(">>> 1WeatherContent ${buckets.toString()}")
        println(">>> 2WeatherContent ${buckets.size}")
        println(">>> 3WeatherContent ${buckets.keys.size} ${buckets.values.size}")
        println(">>> 4WeatherContent ${buckets.keys.joinToString()}")
        DayChartPlate(dayIndex = 0, buckets = buckets, title = "Температура • День 0")
        DayChartPlate(dayIndex = 1, buckets = buckets, title = "Температура • День 1")
        DayChartPlate(dayIndex = 2, buckets = buckets, title = "Температура • День 2")
    }
}

@Composable
private fun CurrentPlate(data: MainForecast, onChangeCoordinates: (Float, Float) -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = data.city,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "${data.currentForecast.currentTemp}°",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Column(Modifier.weight(1f)) {
                    Text(text = "Ветер: ${data.currentForecast.currentWindSpeed} м/с")
                    Text(text = "Влажность: ${data.currentForecast.currentHumidity}%")
                    Text(text = "Давление: ${data.currentForecast.currentPressure} гПа")
                }
            }

            Spacer(Modifier.height(6.dp))

            var showDialog by remember { mutableStateOf(false) }
            var latText by remember { mutableStateOf(TextFieldValue(data.coordinates.lat.toString())) }
            var lonText by remember { mutableStateOf(TextFieldValue(data.coordinates.lon.toString())) }

            Text(
                text = formatCoords(data.coordinates.lat, data.coordinates.lon),
                fontSize = 12.sp,
                modifier = Modifier.clickable { showDialog = true }
            )

            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    confirmButton = {
                        Button(onClick = {
                            val lat = latText.text.toFloatOrNull()
                            val lon = lonText.text.toFloatOrNull()
                            if (lat != null && lon != null) {
                                onChangeCoordinates(lat, lon)
                                showDialog = false
                            }
                        }) { Text("Применить") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDialog = false }) { Text("Отмена") }
                    },
                    title = { Text("Изменить координаты") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = latText,
                                onValueChange = { latText = it },
                                label = { Text("Широта") }
                            )
                            OutlinedTextField(
                                value = lonText,
                                onValueChange = { lonText = it },
                                label = { Text("Долгота") }
                            )
                            Text("Нажмите «Применить», чтобы обновить прогноз", fontSize = 12.sp)
                        }
                    }
                )
            }

            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(text = "Восход: ${data.sunrise.formatHHmm()}")
                Text(text = "Закат: ${data.sunset.formatHHmm()}")
            }
        }
    }
}

@Composable
private fun DayChartPlate(dayIndex: Int, buckets: Map<LocalDate, List<CurrentForecast>>, title: String) {
    val dayKey = buckets.keys.sorted().getOrNull(dayIndex)
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            val header = if (dayKey != null) "$title • ${dayKey}" else title
            Text(text = header, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))

            if (dayKey == null) {
                Text("Нет данных")
                return@Column
            }
            val points = buckets[dayKey]!!.sortedBy { it.dateTime }
            val entries = points.map { ChartEntry(label = it.dateTime.time.formatHHmm(), value = it.currentTemp.toFloat()) }

            LineChart(entries = entries, height = 160.dp)
        }
    }
}

// ------------------------- WIDGETS -------------------------

private data class ChartEntry(val label: String, val value: Float)

@Composable
private fun LineChart(
    entries: List<ChartEntry>,
    height: Dp,
) {
    if (entries.isEmpty()) { Text("Нет данных"); return }

    // Visual tuning
    val strokeWidth = 2.dp
    val dotRadius = 2.5.dp
    val axisColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
    val lineColor = MaterialTheme.colorScheme.primary
    val fillBrush = Brush.verticalGradient(listOf(lineColor.copy(alpha = 0.28f), Color.Transparent))

    // ----- Axis range (target ±10°, i.e., total 20°) -----
    val values = entries.map { it.value }
    val minRaw = values.minOrNull() ?: 0f
    val maxRaw = values.maxOrNull() ?: 1f
    val rawRange = maxRaw - minRaw
    val half = 10f

    val (axisMin, axisMax) =
        if (rawRange <= half * 2f) {
            // Center the band around the middle of the data
            val center = (minRaw + maxRaw) / 2f
            (center - half) to (center + half)
        } else {
            // Data varies by more than 20° — keep everything visible with a small pad
            (minRaw - 1f) to (maxRaw + 1f)
        }
    val dy = (axisMax - axisMin).let { if (it == 0f) 1f else it }

    val avg = values.average().toFloat()

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
        ) {
            Canvas(Modifier.fillMaxSize()) {
                // Smaller paddings around the plotting area
                val paddingLeft = 28.dp.toPx()
                val paddingRight = 8.dp.toPx()
                val paddingTop = 8.dp.toPx()
                val paddingBottom = 16.dp.toPx()

                val w = size.width - paddingLeft - paddingRight
                val h = size.height - paddingTop - paddingBottom

                fun x(i: Int): Float {
                    val n = entries.size
                    return paddingLeft + if (n <= 1) w / 2f else w * (i.toFloat() / (n - 1).toFloat())
                }
                fun y(v: Float): Float = paddingTop + (axisMax - v) / dy * h

                // Axes
                drawLine(
                    color = axisColor,
                    start = Offset(paddingLeft, paddingTop),
                    end = Offset(paddingLeft, paddingTop + h),
                    strokeWidth = 1.dp.toPx()
                )
                drawLine(
                    color = axisColor,
                    start = Offset(paddingLeft, paddingTop + h),
                    end = Offset(paddingLeft + w, paddingTop + h),
                    strokeWidth = 1.dp.toPx()
                )

                // Ticks (Y: 4 segments, shorter length)
                for (t in 1..3) {
                    val yy = paddingTop + h * (t / 4f)
                    drawLine(
                        color = axisColor,
                        start = Offset(paddingLeft - 4.dp.toPx(), yy),
                        end = Offset(paddingLeft, yy),
                        strokeWidth = 1.dp.toPx()
                    )
                }
                // Ticks (X: 4 segments, shorter length)
                for (t in 1..3) {
                    val xx = paddingLeft + w * (t / 4f)
                    drawLine(
                        color = axisColor,
                        start = Offset(xx, paddingTop + h),
                        end = Offset(xx, paddingTop + h + 4.dp.toPx()),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Average line (dashed)
                val yAvg = paddingTop + (axisMax - avg) / dy * h
                drawLine(
                    color = axisColor.copy(alpha = 0.55f),
                    start = Offset(paddingLeft, yAvg),
                    end = Offset(paddingLeft + w, yAvg),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                )

                // Smoothed polyline using quadratic midpoints
                val line = Path().apply {
                    moveTo(x(0), y(values[0]))
                    for (i in 1 until entries.size) {
                        val prevX = x(i - 1)
                        val prevY = y(values[i - 1])
                        val currX = x(i)
                        val currY = y(values[i])
                        val midX = (prevX + currX) / 2f
                        val midY = (prevY + currY) / 2f
                        quadraticBezierTo(prevX, prevY, midX, midY)
                    }
                    lineTo(x(entries.lastIndex), y(values.last()))
                }

                // Gradient fill under the curve
                val fill = Path().apply {
                    addPath(line)
                    lineTo(x(entries.lastIndex), paddingTop + h)
                    lineTo(x(0), paddingTop + h)
                    close()
                }
                drawPath(fill, brush = fillBrush)

                // Main line with rounded corners
                drawPath(
                    path = line,
                    color = lineColor,
                    style = Stroke(
                        width = strokeWidth.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round,
                        pathEffect = PathEffect.cornerPathEffect(6.dp.toPx())
                    )
                )

                // Dots
                for (i in entries.indices) {
                    drawCircle(
                        color = lineColor,
                        radius = dotRadius.toPx(),
                        center = Offset(x(i), y(values[i]))
                    )
                }
            }

            // Y labels (use axis range, not data extremes)
            Text(
                text = "${axisMax.roundToInt()}°",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 2.dp)
            )
            Text(
                text = "${axisMin.roundToInt()}°",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 2.dp, bottom = 2.dp)
            )

            val density = LocalDensity.current
            Box(Modifier.fillMaxSize()) {
                val avgLabel = "${avg.roundToInt()}°"
                // Position label roughly at the average line
                with(density) {
                    val yAvgDp = ((8.dp.toPx() + (axisMax - avg) / dy * (height.toPx() - 8.dp.toPx() - 16.dp.toPx()))).toDp()
                    Text(
                        text = avgLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(start = 2.dp)
                            .offset(y = yAvgDp - 8.dp)
                    )
                }
            }
        }

        // X labels: first | middle | last
        val firstLabel = entries.first().label
        val midLabel = entries[entries.size / 2].label
        val lastLabel = entries.last().label
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, start = 24.dp, end = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(firstLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(midLabel,   style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(lastLabel,  style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ErrorPlate(message: String) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = message,
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            color = MaterialTheme.colorScheme.onErrorContainer,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PlaceholderPlates() {
    // Simple placeholders while first load happens
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PlatePlaceholder(height = 120.dp)
        PlatePlaceholder(height = 220.dp)
        PlatePlaceholder(height = 220.dp)
        PlatePlaceholder(height = 220.dp)
    }
}

@Composable
private fun PlatePlaceholder(height: Dp) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth().height(height)
    ) {}
}

// ------------------------- SHIMMER STRIPE -------------------------

@Composable
private fun ShimmerStripe(
    visible: Boolean,
    height: Dp,
    modifier: Modifier = Modifier,
    bandHeight: Dp = height, // height of the moving rainbow band; remainder becomes bottom glow
) {
    if (!visible) return

    val transition = rememberInfiniteTransition(label = "rainbow")
    // 0f..1f loop for horizontal sweep
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rainbow-phase"
    )

    // Bright, visible rainbow (alpha high to be seen at 6dp)
    val rainbow = listOf(
        Color(0xFFFF5252), // red
        Color(0xFFFFA726), // orange
        Color(0xFFFFEE58), // yellow
        Color(0xFF66BB6A), // green
        Color(0xFF42A5F5), // blue
        Color(0xFF7E57C2), // indigo
        Color(0xFFEC407A)  // violet
    )

    val bg = MaterialTheme.colorScheme.background

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .drawBehind {
                val w = size.width
                val h = size.height
                val bandH = bandHeight.toPx().coerceIn(0f, h)
                val glowH = (h - bandH).coerceAtLeast(0f)

                // Sweep a full-width rainbow band from left->right; repeat the gradient to avoid gaps
                val bandWidth = w // one screen width
                val startX = -bandWidth + (bandWidth * 2f * phase)
                val endX = startX + bandWidth
                // Top moving band
                drawRect(
                    brush = Brush.linearGradient(
                        colors = rainbow,
                        start = Offset(startX, 0f),
                        end = Offset(endX, 0f),
                        tileMode = TileMode.Repeated
                    ),
                    topLeft = Offset(0f, 0f),
                    size = Size(w, bandH),
                    alpha = 0.95f
                )

                if (glowH > 0f) {
                    val topY = bandH
                    // 1) Draw horizontal rainbow (same phase) across the glow area
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = rainbow,//.map { it.copy(alpha = 0.75f) }, if need make some frame
                            start = Offset(startX, topY),
                            end = Offset(endX, topY),
                            tileMode = TileMode.Repeated
                        ),
                        topLeft = Offset(0f, topY),
                        size = Size(w, glowH),
                        alpha = 0.95f
                    )

                    // 2) Vertical overlay to gently blend into the screen background (no white tail)
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                bg // MaterialTheme.colorScheme.background
                            ),
                            startY = topY,
                            endY = topY + glowH
                        ),
                        topLeft = Offset(0f, topY),
                        size = Size(w, glowH)
                    )
                }
            }
    )
}

// ------------------------- HELPERS -------------------------

private fun List<CurrentForecast>.groupByDay(): Map<LocalDate, List<CurrentForecast>> =
    this.groupBy { it.dateTime.date }

private fun LocalTime.formatHHmm(): String =
    "${this.hour.toString().padStart(2, '0')}:${this.minute.toString().padStart(2, '0')}"

private fun formatCoords(lat: Float, lon: Float): String =
    "Координаты: ${lat.fixed(4)}, ${lon.fixed(4)}"

private fun Float.fixed(decimals: Int): String {
    val pow = 10.0.pow(decimals).toFloat()
    val rounded = (this * pow).let { kotlin.math.round(it) } / pow
    val sign = if (rounded < 0f) "-" else ""
    val absVal = kotlin.math.abs(rounded)
    val intPart = absVal.toInt()
    val fracRaw = ((absVal - intPart) * pow + 0.5f).toInt() // guard against 0.9999 -> 1.0000
    val fracStr = fracRaw.toString().padStart(decimals, '0')
    return if (decimals <= 0) "$sign$intPart" else "$sign$intPart.${fracStr.take(decimals)}"
}

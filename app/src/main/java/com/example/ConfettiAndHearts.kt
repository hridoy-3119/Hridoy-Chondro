package com.example

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.sin
import kotlin.random.Random

data class AmbientHeart(
    var xRatio: Float,
    var yRatio: Float,
    val size: Float,
    val speed: Float,
    val swaySpeed: Float,
    val swayAmplitude: Float,
    val alpha: Float,
    val color: Color
)

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val size: Float,
    val color: Color,
    val rotationSpeed: Float,
    var rotation: Float = 0f,
    val isHeart: Boolean,
    var alpha: Float = 1f
)

@Composable
fun AmbientFloatingHearts(
    modifier: Modifier = Modifier,
    count: Int = 18
) {
    val heartColors = listOf(
        Color(0xFFFF4D6D),
        Color(0xFFFF758F),
        Color(0xFFFFB3C1),
        Color(0xFFC9184A),
        Color(0xFFFF85A1)
    )

    val hearts = remember {
        List(count) {
            AmbientHeart(
                xRatio = Random.nextFloat(),
                yRatio = Random.nextFloat(),
                size = Random.nextFloat() * 18f + 12f,
                speed = Random.nextFloat() * 0.0018f + 0.0009f,
                swaySpeed = Random.nextFloat() * 2f + 1.5f,
                swayAmplitude = Random.nextFloat() * 25f + 15f,
                alpha = Random.nextFloat() * 0.45f + 0.25f,
                color = heartColors.random()
            )
        }
    }

    var frameTime by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(16)
            frameTime += 0.016f
            hearts.forEach { heart ->
                heart.yRatio -= heart.speed
                if (heart.yRatio < -0.05f) {
                    heart.yRatio = 1.05f
                    heart.xRatio = Random.nextFloat()
                }
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        hearts.forEach { heart ->
            val sway = sin(frameTime * heart.swaySpeed + heart.size) * heart.swayAmplitude
            val posX = (heart.xRatio * width) + sway
            val posY = heart.yRatio * height

            drawHeart(
                center = Offset(posX, posY),
                size = heart.size,
                color = heart.color.copy(alpha = heart.alpha)
            )
        }
    }
}

@Composable
fun CelebrationConfettiBurst(
    modifier: Modifier = Modifier,
    trigger: Long
) {
    if (trigger == 0L) return

    val confettiColors = listOf(
        Color(0xFFFF4D6D),
        Color(0xFFFF758F),
        Color(0xFFFFCCD5),
        Color(0xFFFFD166),
        Color(0xFF06D6A0),
        Color(0xFF118AB2),
        Color(0xFFC9184A),
        Color(0xFFF72585),
        Color(0xFF7209B7)
    )

    var particles by remember(trigger) {
        mutableStateOf(
            List(120) {
                val angle = Random.nextDouble(0.0, Math.PI * 2)
                val speed = Random.nextFloat() * 18f + 6f
                Particle(
                    x = 0.5f,
                    y = 0.45f,
                    vx = (kotlin.math.cos(angle) * speed).toFloat(),
                    vy = (sin(angle) * speed - 12f).toFloat(),
                    size = Random.nextFloat() * 16f + 10f,
                    color = confettiColors.random(),
                    rotationSpeed = Random.nextFloat() * 15f - 7.5f,
                    rotation = Random.nextFloat() * 360f,
                    isHeart = Random.nextBoolean()
                )
            }
        )
    }

    val progress = remember(trigger) { Animatable(0f) }

    LaunchedEffect(trigger) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 3500, easing = LinearEasing)
        )
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val gravity = 0.45f

        val currentProgress = progress.value
        val fade = (1f - currentProgress).coerceIn(0f, 1f)

        particles.forEach { p ->
            // Update
            p.x += (p.vx / width)
            p.y += (p.vy / height)
            p.vy += gravity
            p.rotation += p.rotationSpeed
            p.alpha = fade

            val screenX = p.x * width
            val screenY = p.y * height

            if (p.isHeart) {
                drawHeart(
                    center = Offset(screenX, screenY),
                    size = p.size,
                    color = p.color.copy(alpha = p.alpha)
                )
            } else {
                rotate(degrees = p.rotation, pivot = Offset(screenX, screenY)) {
                    drawRect(
                        color = p.color.copy(alpha = p.alpha),
                        topLeft = Offset(screenX - p.size / 2, screenY - p.size / 4),
                        size = androidx.compose.ui.geometry.Size(p.size, p.size / 2)
                    )
                }
            }
        }
    }
}

fun DrawScope.drawHeart(center: Offset, size: Float, color: Color) {
    val path = Path()
    val width = size
    val height = size

    val left = center.x - width / 2
    val top = center.y - height / 2

    path.moveTo(center.x, top + height / 4)
    path.cubicTo(
        center.x, top,
        left, top,
        left, top + height / 4
    )
    path.cubicTo(
        left, top + height / 2,
        center.x, top + height * 0.75f,
        center.x, top + height
    )
    path.cubicTo(
        center.x, top + height * 0.75f,
        left + width, top + height / 2,
        left + width, top + height / 4
    )
    path.cubicTo(
        left + width, top,
        center.x, top,
        center.x, top + height / 4
    )
    path.close()

    drawPath(path = path, color = color)
}

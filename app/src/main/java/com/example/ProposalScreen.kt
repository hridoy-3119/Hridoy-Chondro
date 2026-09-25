package com.example

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.RomanticBlushBg
import com.example.ui.theme.RomanticCardBg
import com.example.ui.theme.RomanticPastelBlush
import com.example.ui.theme.RomanticRosePrimary
import com.example.ui.theme.RomanticRubySecondary
import com.example.ui.theme.RomanticSlateNoBtn
import com.example.ui.theme.RomanticSoftPink
import com.example.ui.theme.RomanticWineText
import kotlin.math.roundToInt

@Composable
fun ProposalApp(
    viewModel: ProposalViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        RomanticBlushBg,
                        Color(0xFFFFE5EC),
                        RomanticBlushBg
                    )
                )
            )
    ) {
        // Ambient background drifting hearts
        AmbientFloatingHearts()

        // Celebration confetti burst
        CelebrationConfettiBurst(trigger = uiState.confettiTrigger)

        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar with Actions
            TopRomanticBar(
                isSoundEnabled = uiState.isSoundEnabled,
                onToggleSound = { viewModel.toggleSound() },
                onCustomizeClick = { viewModel.openCustomizeDialog() },
                onResetClick = {
                    VibrationHelper.vibrateTick(context)
                    viewModel.onReset()
                }
            )

            // Content based on step
            when (uiState.step) {
                ProposalStep.QUESTION -> {
                    ProposalQuestionView(
                        uiState = uiState,
                        onYesClicked = {
                            VibrationHelper.vibrateHeartbeat(context)
                            viewModel.onYesClicked()
                        },
                        onNoDodged = { w, h, d ->
                            VibrationHelper.vibrateTick(context)
                            viewModel.onNoButtonDodged(w, h, d)
                        }
                    )
                }
                ProposalStep.CELEBRATION, ProposalStep.RECIPROCATED -> {
                    ProposalCelebrationView(
                        uiState = uiState,
                        onReciprocateClick = {
                            VibrationHelper.vibrateHeartbeat(context)
                            viewModel.onReciprocateClicked()
                        },
                        onShareClick = {
                            shareProposalResult(context, uiState.partnerName, uiState.nickname)
                        },
                        onResetClick = {
                            VibrationHelper.vibrateTick(context)
                            viewModel.onReset()
                        }
                    )
                }
            }
        }

        // Customization Dialog
        if (uiState.showCustomizeDialog) {
            CustomizeNamesDialog(
                currentName = uiState.partnerName,
                currentNickname = uiState.nickname,
                onDismiss = { viewModel.closeCustomizeDialog() },
                onSave = { name, nick -> viewModel.saveCustomNames(name, nick) }
            )
        }
    }
}

@Composable
fun TopRomanticBar(
    isSoundEnabled: Boolean,
    onToggleSound: () -> Unit,
    onCustomizeClick: () -> Unit,
    onResetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = RomanticRosePrimary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "A Special Question",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = RomanticWineText
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onToggleSound,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("sound_toggle_button")
            ) {
                Icon(
                    imageVector = if (isSoundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                    contentDescription = if (isSoundEnabled) "Mute Sound" else "Enable Sound",
                    tint = RomanticRosePrimary
                )
            }

            IconButton(
                onClick = onCustomizeClick,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("customize_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Customize Name",
                    tint = RomanticRosePrimary
                )
            }

            IconButton(
                onClick = onResetClick,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("reset_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset Proposal",
                    tint = RomanticRosePrimary
                )
            }
        }
    }
}

@Composable
fun ProposalQuestionView(
    uiState: ProposalUiState,
    onYesClicked: () -> Unit,
    onNoDodged: (Float, Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Gentle breathing pulse animation for the romantic card
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
    ) {
        val screenWidthPx = constraints.maxWidth.toFloat()
        val screenHeightPx = constraints.maxHeight.toFloat()
        val density = LocalDensity.current.density

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 520.dp)
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(28.dp),
                        ambientColor = RomanticPastelBlush,
                        spotColor = RomanticRosePrimary.copy(alpha = 0.4f)
                    ),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = RomanticCardBg),
                border = BorderStroke(1.5.dp, RomanticSoftPink)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Adorable Teddy / Love Illustration
                    Box(
                        modifier = Modifier
                            .size(170.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(RomanticSoftPink),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_proposal_cute),
                            contentDescription = "Romantic illustration",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Heartfelt opening question (from user prompt)
                    Text(
                        text = "${uiState.partnerName}, আমার জীবনের সবচেয়ে প্রিয় মানুষটা কে জানো? তোমাকে আমার মন থেকে সরাতে পারছি না 🥺!",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = RomanticRosePrimary,
                        textAlign = TextAlign.Center,
                        lineHeight = 27.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Deep romantic sentiment (from user prompt)
                    Text(
                        text = "সারাদিনে তোমায় একবার দেখার জন্য আমার চোখ দুটি ছটফট করতে থাকে। আমি তোমাকে সারাজীবন আমার বউ বানিয়ে আগলে রাখতে চাই...",
                        fontSize = 15.sp,
                        color = RomanticWineText,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // The Big Proposal
                    Surface(
                        color = RomanticSoftPink.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "তুমি কি আমার গার্লফ্রেন্ড হবে? 💍❤️",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = RomanticRubySecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                        )
                    }

                    // Evasive / Dodging Feedback Bubble
                    AnimatedVisibility(
                        visible = uiState.currentDodgeMessage != null,
                        enter = fadeIn() + scaleIn()
                    ) {
                        uiState.currentDodgeMessage?.let { msg ->
                            Surface(
                                modifier = Modifier
                                    .padding(top = 14.dp)
                                    .testTag("dodge_message_bubble"),
                                color = RomanticRosePrimary,
                                shape = RoundedCornerShape(14.dp),
                                shadowElevation = 4.dp
                            ) {
                                Text(
                                    text = msg,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Button Area with Interactive Playful Runaway "No" Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // "YES" Button - Pulses and glows
                            Button(
                                onClick = onYesClicked,
                                modifier = Modifier
                                    .scale(pulseScale * uiState.yesButtonScale)
                                    .shadow(8.dp, RoundedCornerShape(30.dp), spotColor = RomanticRosePrimary)
                                    .testTag("yes_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = RomanticRosePrimary,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(30.dp),
                                contentPadding = ButtonDefaults.ContentPadding
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Favorite,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Yes ❤️",
                                        fontSize = 19.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(36.dp))

                            // "NO" Button - Runs away playfully!
                            val animatedOffsetX by animateFloatAsState(
                                targetValue = uiState.noButtonOffsetX,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                ),
                                label = "no_offset_x"
                            )
                            val animatedOffsetY by animateFloatAsState(
                                targetValue = uiState.noButtonOffsetY,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                ),
                                label = "no_offset_y"
                            )

                            Button(
                                onClick = {
                                    // If somehow clicked, it still escapes!
                                    onNoDodged(screenWidthPx, screenHeightPx, density)
                                },
                                modifier = Modifier
                                    .offset {
                                        IntOffset(
                                            animatedOffsetX.roundToInt(),
                                            animatedOffsetY.roundToInt()
                                        )
                                    }
                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                            onPress = {
                                                onNoDodged(screenWidthPx, screenHeightPx, density)
                                            },
                                            onTap = {
                                                onNoDodged(screenWidthPx, screenHeightPx, density)
                                            }
                                        )
                                    }
                                    .shadow(4.dp, RoundedCornerShape(30.dp))
                                    .testTag("no_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = RomanticSlateNoBtn,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(30.dp)
                            ) {
                                Text(
                                    text = "No 🙈",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    if (uiState.dodgeCount > 0) {
                        Text(
                            text = "না বলার চেষ্টা: ${uiState.dodgeCount} বার (পেরেছ কি? 😜)",
                            fontSize = 12.sp,
                            color = RomanticRubySecondary,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProposalCelebrationView(
    uiState: ProposalUiState,
    onReciprocateClick: () -> Unit,
    onShareClick: () -> Unit,
    onResetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val infiniteTransition = rememberInfiniteTransition(label = "celebration_heart")
    val heartScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heart_pulse"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 520.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(28.dp),
                    ambientColor = RomanticRosePrimary,
                    spotColor = RomanticRosePrimary.copy(alpha = 0.5f)
                ),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = RomanticCardBg),
            border = BorderStroke(2.dp, RomanticRosePrimary.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Joyful Kittens / Celebration Illustration
                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(RomanticSoftPink),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_celebration_love),
                        contentDescription = "Happy love celebration",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Pulsing Heart Badge
                Box(
                    modifier = Modifier
                        .scale(heartScale)
                        .size(54.dp)
                        .background(RomanticRosePrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Beating Heart",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Headline from user request
                Text(
                    text = "তুমি কি আমাকে একবার বলবে",
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                    color = RomanticRosePrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Love quote from user request
                Surface(
                    color = RomanticSoftPink.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, RomanticPastelBlush)
                ) {
                    Text(
                        text = "I love you! ${uiState.nickname} 😘💝",
                        fontSize = 23.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = RomanticRubySecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 22.dp, vertical = 12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sweet honest note from user request
                Surface(
                    color = Color(0xFFFFF7F9),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, RomanticSoftPink.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "আসলে আমি প্রথম বার কোনো মেয়েকে এই কথাটা জানিয়েছি।\nতুমি আগ্রহী না হলে প্লীজ ভাই মনে করে মাফ করে দিও 😁",
                        fontSize = 15.sp,
                        color = RomanticWineText,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                // If she replied "I Love You Too"
                AnimatedVisibility(
                    visible = uiState.step == ProposalStep.RECIPROCATED,
                    enter = fadeIn() + scaleIn()
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                            .testTag("reciprocate_confirmation_box"),
                        color = RomanticRubySecondary,
                        shape = RoundedCornerShape(16.dp),
                        shadowElevation = 6.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "তুমি আমার জীবন ধন্য করে দিলে! 🥹💍🥰",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "আমাদের সুন্দর এই ভালোবাসার গল্প চিরকাল অম্লান থাকুক! ❤️✨",
                                fontSize = 13.sp,
                                color = Color(0xFFFFD166),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Interactive Buttons
                if (uiState.step != ProposalStep.RECIPROCATED) {
                    Button(
                        onClick = onReciprocateClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(8.dp, RoundedCornerShape(24.dp), spotColor = RomanticRosePrimary)
                            .testTag("reciprocate_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RomanticRosePrimary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "I Love You Too! ❤️",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onShareClick,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("share_button"),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.5.dp, RomanticRosePrimary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = RomanticRosePrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Share",
                            color = RomanticRosePrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = onResetClick,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("play_again_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RomanticSoftPink,
                            contentColor = RomanticWineText
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = RomanticWineText,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "আবার খেলুন",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CustomizeNamesDialog(
    currentName: String,
    currentNickname: String,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var nickname by remember { mutableStateOf(currentNickname) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = RomanticRosePrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "নাম ও ডাকনাম পরিবর্তন",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = RomanticWineText
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "প্রস্তাবের জন্য প্রিয় মানুষটির নাম ও ডাকনাম কাস্টমাইজ করুন:",
                    fontSize = 14.sp,
                    color = RomanticWineText
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("প্রিয়তমার নাম (যেমন: রিদি)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RomanticRosePrimary,
                        unfocusedBorderColor = RomanticSoftPink,
                        focusedLabelColor = RomanticRosePrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("name_input_field")
                )

                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    label = { Text("আদরের ডাকনাম (যেমন: jaan)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RomanticRosePrimary,
                        unfocusedBorderColor = RomanticSoftPink,
                        focusedLabelColor = RomanticRosePrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("nickname_input_field")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, nickname) },
                colors = ButtonDefaults.buttonColors(containerColor = RomanticRosePrimary),
                modifier = Modifier.testTag("save_custom_names_button")
            ) {
                Text("সংরক্ষণ করুন")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_custom_names_button")
            ) {
                Text("বাতিল", color = RomanticSlateNoBtn)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp)
    )
}

fun shareProposalResult(context: Context, partnerName: String, nickname: String) {
    val shareText = """
        💖 A Special Question 💖
        $partnerName, আমার জীবনের সবচেয়ে প্রিয় মানুষটা কে জানো? তোমাকে আমার মন থেকে সরাতে পারছি না 🥺!
        
        তুমি কি আমার গার্লফ্রেন্ড হবে?
        👉 SHE SAID YES! 🥰💍🎉
        
        "I love you! $nickname 😘💝"
        
        Sent with love ❤️
    """.trimIndent()

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "A Special Question for $partnerName ❤️")
        putExtra(Intent.EXTRA_TEXT, shareText)
    }
    context.startActivity(Intent.createChooser(intent, "Share with $partnerName"))
}

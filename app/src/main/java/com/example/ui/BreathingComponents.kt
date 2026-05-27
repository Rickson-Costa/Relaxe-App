package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.SessionEntity
import com.example.data.SessionViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

// --- ZEN DEEP OCEAN COLOR PALETTE ---
val BgDeep = Color(0xFF0F172A)     // Dark solid navy space
val BgLight = Color(0xFF1E293B)    // Smooth slate navy
val AccentColor = Color(0xFF2DD4BF) // Serene cyan beach teal
val AccentGlow = Color(0xFF5EEAD4).copy(alpha = 0.35f)
val CoralAlert = Color(0xFFFF6B6B)  // Delicate red/coral for crisis alert

@Composable
fun DecorativeBackground() {
    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        // Upper-left cyan glowing pool
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF0D9488).copy(alpha = 0.22f), Color.Transparent),
                center = Offset(size.width * 0.2f, size.height * 0.25f),
                radius = 500f
            ),
            radius = 500f,
            center = Offset(size.width * 0.2f, size.height * 0.25f)
        )
        // Lower-right indigo oceanic depth pool
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF312E81).copy(alpha = 0.24f), Color.Transparent),
                center = Offset(size.width * 0.85f, size.height * 0.8f),
                radius = 600f
            ),
            radius = 600f,
            center = Offset(size.width * 0.85f, size.height * 0.8f)
        )
        // Auxiliary center glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF115E59).copy(alpha = 0.12f), Color.Transparent),
                center = Offset(size.width * 0.5f, size.height * 0.5f),
                radius = 400f
            ),
            radius = 400f,
            center = Offset(size.width * 0.5f, size.height * 0.5f)
        )
    }
}

@Composable
fun ZenAppNavigation(viewModel: SessionViewModel) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            PremiumBottomBar(navController)
        },
        containerColor = Color.Transparent,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "breathing",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(
                route = "breathing",
                enterTransition = { fadeIn(tween(500)) + scaleIn(initialScale = 0.95f, animationSpec = tween(500)) },
                exitTransition = { fadeOut(tween(400)) }
            ) {
                PremiumBreathingScreen(viewModel = viewModel)
            }

            composable(
                route = "history",
                enterTransition = { fadeIn(tween(500)) + slideInVertically(initialOffsetY = { it / 3 }, animationSpec = tween(500)) },
                exitTransition = { fadeOut(tween(400)) + slideOutVertically(targetOffsetY = { it / 3 }, animationSpec = tween(400)) }
            ) {
                val sessions by viewModel.allSessions.collectAsStateWithLifecycle()
                HistoryScreen(
                    sessions = sessions,
                    onClearHistory = { viewModel.clearHistory() }
                )
            }
        }
    }
}

@Composable
fun PremiumBottomBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 48.dp, vertical = 24.dp)
            .height(64.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(32.dp))
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                icon = Icons.Default.Home,
                label = "Praticar",
                isSelected = currentRoute == "breathing",
                tag = "breathing_tab",
                onClick = {
                    if (currentRoute != "breathing") {
                        navController.navigate("breathing") {
                            popUpTo("breathing") { inclusive = true }
                        }
                    }
                }
            )
            BottomNavItem(
                icon = Icons.Default.DateRange,
                label = "Acompanhar",
                isSelected = currentRoute == "history",
                tag = "history_tab",
                onClick = {
                    if (currentRoute != "history") {
                        navController.navigate("history") {
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun BottomNavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    tag: String,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val alpha by animateFloatAsState(if (isSelected) 1f else 0.44f, label = "TabAlpha")
    val scale by animateFloatAsState(if (isSelected) 1.05f else 0.95f, label = "TabScale")

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
            .padding(vertical = 4.dp, horizontal = 16.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .testTag(tag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) AccentColor else Color.White,
            modifier = Modifier
                .size(26.dp)
                .graphicsLayer(alpha = alpha)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = if (isSelected) AccentColor else Color.White,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
            modifier = Modifier.graphicsLayer(alpha = alpha)
        )
    }
}

@Composable
fun PremiumBreathingScreen(viewModel: SessionViewModel) {
    val haptic = LocalHapticFeedback.current
    
    // Audio engine initializer
    val soundSynth = remember { SoothingSoundSynth() }
    var isSoundEnabled by remember { mutableStateOf(true) }

    // Session cycle states
    var phaseIndex by remember { mutableIntStateOf(0) } // 0 = Inhale (4s), 1 = Hold (7s), 2 = Exhale (8s)
    var isRunning by remember { mutableStateOf(false) }
    var cycleCount by remember { mutableIntStateOf(0) }
    var showFeedback by remember { mutableStateOf(false) }
    var secondsRemaining by remember { mutableIntStateOf(4) }

    val phases = listOf("Inspire", "Segure", "Expire")
    val phaseDescriptions = listOf(
        "Expanda os pulmões de forma calma",
        "Mantenha o ar e sinta o silêncio",
        "Solte devagar esvaziando o corpo"
    )

    // Master sound synth lifecycle management with system event observers
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START, Lifecycle.Event.ON_RESUME -> {
                    soundSynth.start()
                    // Instantly restore active parameters upon foreground return
                    if (!isSoundEnabled || !isRunning) {
                        soundSynth.setParams(volume = 0.0f, frequency = 120.0f)
                    } else {
                        when (phaseIndex) {
                            0 -> soundSynth.setParams(volume = 0.35f, frequency = 170.0f)
                            1 -> soundSynth.setParams(volume = 0.35f, frequency = 175.5f)
                            2 -> soundSynth.setParams(volume = 0.12f, frequency = 110.0f)
                        }
                    }
                }
                Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> {
                    soundSynth.stop()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            soundSynth.stop()
        }
    }

    // Direct, live sound trigger when parameters shift or toggle is clicked
    LaunchedEffect(isSoundEnabled, isRunning, phaseIndex) {
        if (!isSoundEnabled || !isRunning) {
            soundSynth.setParams(volume = 0.0f, frequency = 120.0f)
        } else {
            when (phaseIndex) {
                0 -> soundSynth.setParams(volume = 0.35f, frequency = 170.0f)
                1 -> soundSynth.setParams(volume = 0.35f, frequency = 175.5f)
                2 -> soundSynth.setParams(volume = 0.12f, frequency = 110.0f)
            }
        }
    }

    // Precise core breathing timer coroutine with ticking countdown support
    LaunchedEffect(isRunning, phaseIndex) {
        if (isRunning) {
            val durations = listOf(4, 7, 8)
            secondsRemaining = durations[phaseIndex]
            
            while (secondsRemaining > 0) {
                delay(1000L)
                secondsRemaining--
            }
            
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            
            val nextPhase = (phaseIndex + 1) % 3
            if (nextPhase == 0) {
                cycleCount++
                if (cycleCount >= 4) {
                    isRunning = false
                    soundSynth.setParams(0.0f, 110.0f)
                    showFeedback = true
                }
            }
            phaseIndex = nextPhase
        } else {
            secondsRemaining = 4
        }
    }

    // High performance scale animator
    val targetScale = if (!isRunning) {
        1.0f
    } else {
        when (phaseIndex) {
            0 -> 1.45f  // Expansion
            1 -> 1.45f  // steady Hold
            else -> 0.8f // contraction
        }
    }

    val animationDuration = if (!isRunning) {
        1000
    } else {
        when (phaseIndex) {
            0 -> 4000
            1 -> 7000
            else -> 8000
        }
    }

    val orbScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = tween(
            durationMillis = animationDuration,
            easing = if (phaseIndex == 1 && isRunning) LinearEasing else EaseInOutQuad
        ),
        label = "BreathingOrbScale"
    )

    // Infinite ambient light halo animation
    val infiniteTransition = rememberInfiniteTransition(label = "HaloPulsate")
    val pulseFactor by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "HaloPulseValue"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // APP AUDIO ENABLER BUTTON (Glassmorphism top right alignment)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                        .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                        .clickable { isSoundEnabled = !isSoundEnabled }
                        .testTag("sound_toggle_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isSoundEnabled) "🔊" else "🔇",
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.weight(0.4f))

            // REVELATION LABELS
            Text(
                text = if (isRunning) phases[phaseIndex].uppercase() else "RELAXE",
                fontSize = 32.sp,
                fontWeight = FontWeight.Light,
                color = Color.White,
                letterSpacing = 8.sp,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = if (isRunning) phaseDescriptions[phaseIndex] else "Toque no botão abaixo para iniciar o ciclo de respiração",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            Spacer(modifier = Modifier.weight(0.6f))

            // THE BREATHING CORE ORB - GORGEOUS MODERN GLASSMORPHIC RING DESIGN
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(240.dp)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (isRunning) {
                            isRunning = false
                            phaseIndex = 0
                            cycleCount = 0
                        } else {
                            isRunning = true
                            phaseIndex = 0
                            cycleCount = 0
                        }
                    }
            ) {
                // 1. External Delicate Halo Ripple (Responsive & clean scaling)
                Box(
                    modifier = Modifier
                        .size(210.dp)
                        .graphicsLayer {
                            scaleX = orbScale * 1.15f * pulseFactor
                            scaleY = orbScale * 1.15f * pulseFactor
                        }
                        .border(1.dp, AccentColor.copy(alpha = 0.25f), CircleShape)
                )

                // 2. Middle Floating Ring (Fine outline orbit)
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .graphicsLayer {
                            scaleX = orbScale * pulseFactor
                            scaleY = orbScale * pulseFactor
                        }
                        .border(1.5.dp, Brush.radialGradient(listOf(Color.White.copy(alpha = 0.35f), AccentColor.copy(alpha = 0.15f))), CircleShape)
                )

                // 3. Central Glassmorphic Translucent Core Disk
                Box(
                    modifier = Modifier
                        .size(145.dp)
                        .graphicsLayer {
                            scaleX = orbScale
                            scaleY = orbScale
                        }
                        .clip(CircleShape)
                        .background(BgLight.copy(alpha = 0.75f))
                        .border(2.dp, Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.15f), AccentColor)), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isRunning) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$secondsRemaining",
                                color = Color.White,
                                fontSize = 54.sp,
                                fontWeight = FontWeight.Light,
                                letterSpacing = (-1).sp
                            )
                            Text(
                                text = phases[phaseIndex].uppercase(),
                                color = AccentColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "✨",
                                fontSize = 32.sp,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            Text(
                                text = "COMEÇAR",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(0.6f))

            // CURRENT STEP PILLS INDICATOR
            if (isRunning) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    phases.forEachIndexed { index, name ->
                        val isActive = index == phaseIndex
                        val widthFactor by animateDpAsState(if (isActive) 32.dp else 12.dp, label = "PillWidth")
                        
                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .width(widthFactor)
                                .clip(CircleShape)
                                .background(
                                    if (isActive) AccentColor else Color.White.copy(alpha = 0.2f)
                                )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Ciclo ${cycleCount + 1} de 4",
                    fontSize = 12.sp,
                    color = AccentColor,
                    fontWeight = FontWeight.SemiBold
                )
            } else {
                Text(
                    text = "Prática suspensa ou concluída",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.35f)
                )
            }

            Spacer(modifier = Modifier.weight(0.4f))

            // START/STOP TRIGGER CONTROLLER
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (isRunning) {
                        isRunning = false
                        phaseIndex = 0
                        cycleCount = 0
                    } else {
                        isRunning = true
                        phaseIndex = 0
                        cycleCount = 0
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRunning) Color.White.copy(alpha = 0.12f) else AccentColor,
                    contentColor = if (isRunning) Color.White else BgDeep
                ),
                shape = RoundedCornerShape(24.dp),
                border = if (isRunning) BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)) else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 24.dp)
                    .testTag("start_stop_button")
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isRunning) "PARAR PRÁTICA" else "INICIAR RESPIRAÇÃO",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.weight(0.2f))

            // Footer attribution
            Text(
                text = "Desenvolvido por Rickson Henrique e AI Studio",
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.25f),
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
        }

        // FEEDBACK EVALUATION CARD modal overlay popup
        if (showFeedback) {
            FeedbackOverlay(
                onDismiss = { wasCrisis, improved ->
                    showFeedback = false
                    // Save fully structured session log into database ViewModel
                    viewModel.saveSession(
                        wasCrisis = wasCrisis,
                        feltBetter = improved,
                        durationSeconds = 76 // 4 cycles * (4+7+8)s = 76 seconds length
                    )
                }
            )
        }
    }
}

@Composable
fun FeedbackOverlay(
    onDismiss: (wasCrisis: Boolean, improved: Boolean) -> Unit
) {
    var step by remember { mutableIntStateOf(1) } // 1: crisis check, 2: improvement feedback
    var wasCrisisInput by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f))
            .clickable(enabled = false) {}, // Intercept clicks to block dismissing by clicking background
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White.copy(alpha = 0.08f))
                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(28.dp))
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Feedback da Sessão",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = AccentColor,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (step == 1) {
                Text(
                    text = "Você estava enfrentando um início de crise de ansiedade?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            wasCrisisInput = true
                            step = 2
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CoralAlert.copy(alpha = 0.85f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("crisis_yes_button")
                    ) {
                        Text("Sim, estava em crise", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            wasCrisisInput = false
                            step = 2
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("crisis_no_button")
                    ) {
                        Text("Não, rotina preventiva", color = Color.White)
                    }
                }
            } else {
                Text(
                    text = "Como você se sente após realizar esse ciclo de respiração?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { onDismiss(wasCrisisInput, true) },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("feel_much_better_button")
                    ) {
                        Text("Muito melhor ✨", color = BgDeep, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { onDismiss(wasCrisisInput, true) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("feel_better_button")
                    ) {
                        Text("Sinto alguma melhora", color = Color.White)
                    }

                    Button(
                        onClick = { onDismiss(wasCrisisInput, false) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("feel_same_button")
                    ) {
                        Text("Sem alteração / Igual", color = Color.White.copy(alpha = 0.7f))
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryScreen(
    sessions: List<SessionEntity>,
    onClearHistory: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var showDialogConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // HEADER ROW (With clear trigger)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Sua Jornada",
                fontSize = 28.sp,
                fontWeight = FontWeight.Light,
                color = Color.White,
                letterSpacing = 1.sp
            )
            
            if (sessions.isNotEmpty()) {
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showDialogConfirm = true
                    },
                    modifier = Modifier.testTag("clear_history_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Limpar histórico",
                        tint = CoralAlert.copy(alpha = 0.8f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (sessions.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Text(
                        text = "🌱",
                        fontSize = 48.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Text(
                        text = "Sem registros de respiração",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Conclua uma prática para gerar estatísticas de alívio e controle de crise.",
                        color = Color.White.copy(alpha = 0.3f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // STATS TILES CARD (Glassmorphic)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Total sessões
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = sessions.size.toString(),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentColor
                        )
                        Text(
                            text = "Sessões",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }

                    // Efficacy
                    val totalImproved = sessions.count { it.feltBetter }
                    val efficacyPercent = (totalImproved * 100) / sessions.size
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${efficacyPercent}%",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF34D399) // Clean serene emerald green
                        )
                        Text(
                            text = "Eficácia",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }

                    // Crises alleviations
                    val crisesCount = sessions.count { it.wasCrisis }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = crisesCount.toString(),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = CoralAlert
                        )
                        Text(
                            text = "Crises Aliviadas",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // RECENT GRAPH VIEW
            Text(
                text = "ATIVIDADE DOS ÚLTIMOS 7 DIAS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.4f),
                letterSpacing = 1.5.sp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            MinimalistChart(sessions = sessions)

            Spacer(modifier = Modifier.height(28.dp))

            // RECENT SESSIONS LAZY LIST
            Text(
                text = "LOG DE PRÁTICA RECENTE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.4f),
                letterSpacing = 1.5.sp
            )

            LazyColumn(
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(
                    items = sessions,
                    key = { it.id }
                ) { session ->
                    HistoryItemCard(session = session)
                }
            }

            // Elegant Footer attribution
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Desenvolvido por Rickson Henrique e AI Studio",
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.25f),
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
        }
    }

    // CONFIRM DELETE DIALOG OVERLAY
    if (showDialogConfirm) {
        AlertDialog(
            onDismissRequest = { showDialogConfirm = false },
            title = { Text(text = "Limpar Histórico", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text(text = "Tem certeza de que deseja apagar permanentemente todas as suas sessões?", color = Color.White.copy(alpha = 0.8f)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearHistory()
                        showDialogConfirm = false
                    }
                ) {
                    Text(text = "APAGAR TUDO", color = CoralAlert, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialogConfirm = false }) {
                    Text(text = "CANCELAR", color = Color.White.copy(alpha = 0.6f))
                }
            },
            containerColor = BgLight,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
        )
    }
}

@Composable
fun HistoryItemCard(session: SessionEntity) {
    val formatter = remember { SimpleDateFormat("dd 'de' MMM ', ' HH:mm", Locale.forLanguageTag("pt-BR")) }
    val formattedDate = remember(session.timestamp) { formatter.format(Date(session.timestamp)) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.04f))
            .border(1.dp, Color.White.copy(alpha = 0.07f), RoundedCornerShape(16.dp))
            .padding(16.dp)
            .testTag("session_item_${session.id}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // Small Indicator Dot
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (session.wasCrisis) CoralAlert else AccentColor)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column {
                Text(
                    text = if (session.wasCrisis) "Alívio de Crise" else "Respiração Preventiva",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = formattedDate,
                        color = Color.White.copy(alpha = 0.44f),
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(3.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.3f))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "76s",
                        color = Color.White.copy(alpha = 0.44f),
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Status Indicator Badge
        Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (session.feltBetter) Color(0xFF0F766E).copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f)
            ),
            modifier = Modifier.border(
                width = 1.dp,
                color = if (session.feltBetter) Color(0xFF0D9488).copy(alpha = 0.3f) else Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            )
        ) {
            Text(
                text = if (session.feltBetter) "Melhorou ✨" else "Estável",
                color = if (session.feltBetter) AccentColor else Color.White.copy(alpha = 0.5f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun MinimalistChart(sessions: List<SessionEntity>) {
    val counts = IntArray(7) { 0 }
    val now = System.currentTimeMillis()
    
    for (session in sessions) {
        val diffMs = now - session.timestamp
        val diffDays = (diffMs / (1000 * 60 * 60 * 24)).toInt()
        if (diffDays in 0..6) {
            counts[diffDays]++
        }
    }
    
    val dailyCounts = counts.reversedArray()
    val maxCount = (dailyCounts.maxOrNull() ?: 1).coerceAtLeast(3)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.03f))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                dailyCounts.forEachIndexed { index, count ->
                    val barHeightFraction = count.toFloat() / maxCount.toFloat()
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Canvas(
                            modifier = Modifier
                                .width(12.dp)
                                .height(60.dp)
                        ) {
                            val w = size.width
                            val h = size.height
                            val actualBarHeight = h * barHeightFraction.coerceIn(0.08f, 1.0f)
                            
                            // Background track
                            drawRoundRect(
                                color = Color.White.copy(alpha = 0.05f),
                                topLeft = Offset(0f, 0f),
                                size = Size(w, h),
                                cornerRadius = CornerRadius(w / 2, w / 2)
                            )
                            
                            // Bar fill
                            drawRoundRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color(0xFF5EEAD4), Color(0xFF0D9488))
                                ),
                                topLeft = Offset(0f, h - actualBarHeight),
                                size = Size(w, actualBarHeight),
                                cornerRadius = CornerRadius(w / 2, w / 2)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val format = remember { SimpleDateFormat("EEE", Locale.forLanguageTag("pt-BR")) }
                val labels = remember {
                    val list = mutableListOf<String>()
                    val calendarDay = Calendar.getInstance()
                    calendarDay.add(Calendar.DAY_OF_YEAR, -6)
                    for (i in 0..6) {
                        list.add(format.format(calendarDay.time).uppercase())
                        calendarDay.add(Calendar.DAY_OF_YEAR, 1)
                    }
                    list
                }
                
                labels.forEach { label ->
                    Text(
                        text = label.take(3),
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.4f),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}


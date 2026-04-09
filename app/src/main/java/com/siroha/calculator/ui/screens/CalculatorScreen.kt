package com.siroha.calculator.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.siroha.calculator.ui.components.ButtonType
import com.siroha.calculator.ui.components.CalculatorButton
import com.siroha.calculator.ui.components.ScientificButton
import com.siroha.calculator.viewmodel.CalculatorAction
import com.siroha.calculator.viewmodel.CalculatorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel = viewModel()
) {
    val state = viewModel.state
    var showHistory by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    LaunchedEffect(state.display) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Calculator",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.SemiBold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Memory indicator
                    if (state.hasMemory) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "M",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    IconButton(onClick = { showHistory = !showHistory }) {
                        Icon(
                            imageVector = if (showHistory) Icons.Filled.History else Icons.Outlined.History,
                            contentDescription = "History",
                            tint = if (showHistory) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onBackground
                        )
                    }
                    IconButton(onClick = { viewModel.onAction(CalculatorAction.ToggleScientific) }) {
                        Icon(
                            imageVector = if (state.isScientificMode) Icons.Filled.Functions else Icons.Outlined.Functions,
                            contentDescription = "Scientific",
                            tint = if (state.isScientificMode) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }

            // History panel
            AnimatedVisibility(
                visible = showHistory,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "History",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (state.history.isNotEmpty()) {
                            TextButton(onClick = { viewModel.onAction(CalculatorAction.ClearHistory) }) {
                                Text("Clear", fontSize = 12.sp)
                            }
                        }
                    }
                    if (state.history.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No history yet",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    } else {
                        LazyColumn(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                            items(state.history) { entry ->
                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Text(
                                        text = entry.expression,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "= ${entry.result}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Divider(
                                        modifier = Modifier.padding(top = 4.dp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Display area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.End
            ) {
                // Expression
                AnimatedContent(
                    targetState = state.expression,
                    transitionSpec = { fadeIn() togetherWith fadeOut() }
                ) { expr ->
                    Text(
                        text = expr,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Main display
                AnimatedContent(
                    targetState = state.display,
                    transitionSpec = {
                        slideInVertically { it / 2 } + fadeIn() togetherWith
                        slideOutVertically { -it / 2 } + fadeOut()
                    }
                ) { display ->
                    val textSize = when {
                        display.length > 12 -> 36.sp
                        display.length > 9 -> 48.sp
                        else -> 60.sp
                    }
                    Text(
                        text = display,
                        fontSize = textSize,
                        fontWeight = FontWeight.Light,
                        color = if (state.isError)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )
                }
            }

            // Scientific panel
            AnimatedVisibility(
                visible = state.isScientificMode,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                ScientificPanel(
                    isDegreeMode = state.isDegreeMode,
                    onAction = viewModel::onAction
                )
            }

            // Memory row
            MemoryRow(
                hasMemory = state.hasMemory,
                onAction = viewModel::onAction
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Main keypad
            MainKeypad(
                modifier = Modifier.padding(horizontal = 12.dp),
                onAction = viewModel::onAction
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ScientificPanel(
    isDegreeMode: Boolean,
    onAction: (CalculatorAction) -> Unit
) {
    val scientificButtons = listOf(
        listOf("sin", "cos", "tan", "π", "e"),
        listOf("asin", "acos", "atan", "log", "ln"),
        listOf("x²", "x³", "√", "∛", "1/x"),
        listOf("eˣ", "10ˣ", "2ˣ", "x!", "|x|")
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Degree/Radian toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            FilterChip(
                selected = isDegreeMode,
                onClick = { onAction(CalculatorAction.ToggleDegreeRadian) },
                label = { Text(if (isDegreeMode) "DEG" else "RAD", fontSize = 11.sp) },
                modifier = Modifier.height(28.dp)
            )
        }

        scientificButtons.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                row.forEach { label ->
                    val functionKey = when (label) {
                        "x²" -> "square"
                        "x³" -> "cube"
                        "√" -> "sqrt"
                        "∛" -> "cbrt"
                        "1/x" -> "inv"
                        "eˣ" -> "exp"
                        "10ˣ" -> "10x"
                        "2ˣ" -> "2x"
                        "x!" -> "factorial"
                        "|x|" -> "abs"
                        "π" -> "pi"
                        "e" -> "e"
                        else -> label
                    }
                    ScientificButton(
                        symbol = label,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (label == "π" || label == "e") {
                                onAction(CalculatorAction.ScientificFunction(functionKey))
                            } else {
                                onAction(CalculatorAction.ScientificFunction(functionKey))
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MemoryRow(
    hasMemory: Boolean,
    onAction: (CalculatorAction) -> Unit
) {
    val buttons = listOf("MC", "MR", "M+", "MS")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        buttons.forEach { label ->
            ScientificButton(
                symbol = label,
                modifier = Modifier.weight(1f),
                isActive = label == "MR" && hasMemory,
                onClick = {
                    when (label) {
                        "MC" -> onAction(CalculatorAction.MemoryClear)
                        "MR" -> onAction(CalculatorAction.MemoryRecall)
                        "M+" -> onAction(CalculatorAction.MemoryAdd)
                        "MS" -> onAction(CalculatorAction.MemoryStore)
                    }
                }
            )
        }
    }
}

@Composable
fun MainKeypad(
    modifier: Modifier = Modifier,
    onAction: (CalculatorAction) -> Unit
) {
    val buttonSpacing = 8.dp

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(buttonSpacing)
    ) {
        // Row 1: AC, +/-, %, ÷
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
        ) {
            CalculatorButton(
                symbol = "AC",
                modifier = Modifier.weight(1f),
                buttonType = ButtonType.SPECIAL,
                fontSize = 18.sp,
                onClick = { onAction(CalculatorAction.Clear) }
            )
            CalculatorButton(
                symbol = "+/-",
                modifier = Modifier.weight(1f),
                buttonType = ButtonType.SPECIAL,
                fontSize = 18.sp,
                onClick = { onAction(CalculatorAction.Negate) }
            )
            CalculatorButton(
                symbol = "%",
                modifier = Modifier.weight(1f),
                buttonType = ButtonType.SPECIAL,
                fontSize = 18.sp,
                onClick = { onAction(CalculatorAction.Percent) }
            )
            CalculatorButton(
                symbol = "÷",
                modifier = Modifier.weight(1f),
                buttonType = ButtonType.OPERATOR,
                fontSize = 24.sp,
                onClick = { onAction(CalculatorAction.Operator("÷")) }
            )
        }

        // Row 2: 7, 8, 9, ×
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
        ) {
            CalculatorButton("7", Modifier.weight(1f), ButtonType.NUMBER, onClick = { onAction(CalculatorAction.Number("7")) })
            CalculatorButton("8", Modifier.weight(1f), ButtonType.NUMBER, onClick = { onAction(CalculatorAction.Number("8")) })
            CalculatorButton("9", Modifier.weight(1f), ButtonType.NUMBER, onClick = { onAction(CalculatorAction.Number("9")) })
            CalculatorButton("×", Modifier.weight(1f), ButtonType.OPERATOR, fontSize = 24.sp, onClick = { onAction(CalculatorAction.Operator("×")) })
        }

        // Row 3: 4, 5, 6, -
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
        ) {
            CalculatorButton("4", Modifier.weight(1f), ButtonType.NUMBER, onClick = { onAction(CalculatorAction.Number("4")) })
            CalculatorButton("5", Modifier.weight(1f), ButtonType.NUMBER, onClick = { onAction(CalculatorAction.Number("5")) })
            CalculatorButton("6", Modifier.weight(1f), ButtonType.NUMBER, onClick = { onAction(CalculatorAction.Number("6")) })
            CalculatorButton("-", Modifier.weight(1f), ButtonType.OPERATOR, fontSize = 28.sp, onClick = { onAction(CalculatorAction.Operator("-")) })
        }

        // Row 4: 1, 2, 3, +
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
        ) {
            CalculatorButton("1", Modifier.weight(1f), ButtonType.NUMBER, onClick = { onAction(CalculatorAction.Number("1")) })
            CalculatorButton("2", Modifier.weight(1f), ButtonType.NUMBER, onClick = { onAction(CalculatorAction.Number("2")) })
            CalculatorButton("3", Modifier.weight(1f), ButtonType.NUMBER, onClick = { onAction(CalculatorAction.Number("3")) })
            CalculatorButton("+", Modifier.weight(1f), ButtonType.OPERATOR, fontSize = 28.sp, onClick = { onAction(CalculatorAction.Operator("+")) })
        }

        // Row 5: DEL, 0, ., =
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
        ) {
            CalculatorButton(
                symbol = "⌫",
                modifier = Modifier.weight(1f),
                buttonType = ButtonType.SPECIAL,
                fontSize = 22.sp,
                onClick = { onAction(CalculatorAction.Delete) }
            )
            CalculatorButton("0", Modifier.weight(1f), ButtonType.NUMBER, onClick = { onAction(CalculatorAction.Number("0")) })
            CalculatorButton(".", Modifier.weight(1f), ButtonType.NUMBER, fontSize = 28.sp, onClick = { onAction(CalculatorAction.Decimal) })
            CalculatorButton(
                symbol = "=",
                modifier = Modifier.weight(1f),
                buttonType = ButtonType.EQUALS,
                fontSize = 28.sp,
                onClick = { onAction(CalculatorAction.Calculate) }
            )
        }
    }
}

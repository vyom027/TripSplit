package com.ljku.tripsplit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ljku.tripsplit.domain.Expense
import com.ljku.tripsplit.domain.Member
import com.ljku.tripsplit.domain.PaidShare
import com.ljku.tripsplit.domain.SettlementEngine
import com.ljku.tripsplit.domain.SplitMode
import com.ljku.tripsplit.domain.SplitShare
import com.ljku.tripsplit.ui.theme.Typography
import com.ljku.tripsplit.ui.util.Formatters
import java.util.UUID

private data class QuickShortcut(val label: String, val text: String, val category: String)
private data class CategoryOption(val name: String, val icon: ImageVector, val color: Color)

private val quickShortcuts = listOf(
    QuickShortcut("Water 💧", "Water", "Food"),
    QuickShortcut("Snacks 🍿", "Snacks", "Food"),
    QuickShortcut("Coffee ☕", "Coffee", "Food"),
    QuickShortcut("Taxi 🚕", "Taxi", "Transport"),
    QuickShortcut("Flight ✈️", "Flight", "Transport"),
    QuickShortcut("Hotel 🏨", "Hotel", "Stay"),
    QuickShortcut("Tickets 🎫", "Tickets", "Activities")
)

private val categoryOptions = listOf(
    CategoryOption("Food", Icons.Outlined.Restaurant, com.ljku.tripsplit.ui.theme.AuroraEmeraldLight),
    CategoryOption("Transport", Icons.Outlined.DirectionsCar, com.ljku.tripsplit.ui.theme.AuroraCyan),
    CategoryOption("Stay", Icons.Outlined.Hotel, com.ljku.tripsplit.ui.theme.AuroraVioletLight),
    CategoryOption("Activities", Icons.Outlined.SportsEsports, com.ljku.tripsplit.ui.theme.AuroraGoldLight),
    CategoryOption("Other", Icons.Outlined.Category, Color(0xFF94A3B8))
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ExpenseFormScreen(
    tripId: String,
    members: List<Member>,
    existingExpense: Expense? = null,
    preselectedMemberId: String? = null,
    defaultPayerId: String? = null,
    currentMemberId: String? = null,
    viewerName: String? = null,
    asRequest: Boolean = false,
    onSaveExpense: (Expense) -> Unit,
    onBack: () -> Unit
) {
    val isEditing = existingExpense != null
    var step by remember { mutableIntStateOf(0) } // 0 = What, 1 = Who

    // Step 1 state
    var amountText by remember { mutableStateOf(
        existingExpense?.let {
            val amt = it.totalAmount / 100.0
            if (amt % 1.0 == 0.0) amt.toLong().toString() else amt.toString()
        } ?: ""
    ) }
    var description by remember { mutableStateOf(existingExpense?.description ?: "") }
    var category by remember { mutableStateOf(existingExpense?.category ?: "Food") }
    var expenseDate by remember { mutableStateOf(existingExpense?.date ?: System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var isPersonal by remember { mutableStateOf(existingExpense?.isPersonal ?: (preselectedMemberId != null)) }
    var personalMemberId by remember { mutableStateOf(existingExpense?.personalMemberId ?: preselectedMemberId ?: members.firstOrNull()?.id) }
    var notes by remember { mutableStateOf(existingExpense?.notes ?: "") }

    // Step 2 state
    val payerAmounts = remember {
        androidx.compose.runtime.mutableStateMapOf<String, String>().apply {
            putAll(
                existingExpense?.paidBy?.associate {
                    val amt = it.amountPaid / 100.0
                    it.memberId to (if (amt % 1.0 == 0.0) amt.toLong().toString() else amt.toString())
                } ?: emptyMap()
            )
        }
    }
    var splitMode by remember { mutableStateOf(existingExpense?.splitMode ?: SplitMode.EQUAL) }
    var includedMembers by remember {
        mutableStateOf(
            existingExpense?.splitAmong?.map { it.memberId }?.toSet()
                ?: members.map { it.id }.toSet()
        )
    }

    // Default select ALL members when member list is loaded from database for a new expense
    LaunchedEffect(members) {
        if (existingExpense == null && (includedMembers.isEmpty() || (splitMode == SplitMode.EQUAL && includedMembers.size < members.size && includedMembers.isEmpty()))) {
            includedMembers = members.map { it.id }.toSet()
        }
    }
    val splitValues = remember {
        androidx.compose.runtime.mutableStateMapOf<String, String>().apply {
            putAll(
                existingExpense?.splitAmong?.associate { share ->
                    when (existingExpense.splitMode) {
                        SplitMode.PERCENTAGE -> {
                            val pct = (share.amountOwed * 100.0) / existingExpense.totalAmount
                            share.memberId to (if (pct % 1.0 == 0.0) pct.toLong().toString() else pct.toString())
                        }
                        else -> {
                            val amt = share.amountOwed / 100.0
                            share.memberId to (if (amt % 1.0 == 0.0) amt.toLong().toString() else amt.toString())
                        }
                    }
                } ?: emptyMap()
            )
        }
    }

    // Derived values
    val amountCents = (amountText.toDoubleOrNull() ?: 0.0).let { (it * 100).toLong() }
    val paidShares = members.mapNotNull { member ->
        val amount = payerAmounts[member.id]?.toDoubleOrNull() ?: 0.0
        if (amount > 0) PaidShare(member.id, (amount * 100).toLong()) else null
    }
    val totalCents = paidShares.sumOf { it.amountPaid }
    val selectedMemberIds = includedMembers.toList()
    val splitShares = when (splitMode) {
        SplitMode.EQUAL -> SettlementEngine.buildEqualSplits(amountCents, selectedMemberIds)
        SplitMode.EXACT -> selectedMemberIds.mapNotNull { id ->
            val amount = splitValues[id]?.toDoubleOrNull() ?: 0.0
            if (amount > 0) SplitShare(id, (amount * 100).toLong()) else null
        }
        SplitMode.PERCENTAGE -> {
            val percentages = selectedMemberIds.associateWith { id -> splitValues[id]?.toDoubleOrNull() ?: 0.0 }
            SettlementEngine.buildPercentageSplits(amountCents, percentages)
        }
        SplitMode.SHARES -> {
            val shares = selectedMemberIds.associateWith { id -> splitValues[id]?.toIntOrNull() ?: 1 }
            SettlementEngine.buildShareSplits(amountCents, shares)
        }
        SplitMode.PERSONAL -> emptyList()
    }
    val splitValid = when (splitMode) {
        SplitMode.EQUAL, SplitMode.SHARES -> selectedMemberIds.isNotEmpty()
        SplitMode.EXACT -> splitShares.sumOf { it.amountOwed } == amountCents && splitShares.isNotEmpty()
        SplitMode.PERCENTAGE -> {
            val pctSum = selectedMemberIds.sumOf { splitValues[it]?.toDoubleOrNull() ?: 0.0 }
            pctSum in 99.9..100.1 && splitShares.isNotEmpty()
        }
        SplitMode.PERSONAL -> true
    }
    val step1Valid = description.isNotBlank() && amountText.toDoubleOrNull()?.let { it > 0 } == true && (!isPersonal || personalMemberId != null)
    val paidMatchesTotal = totalCents == amountCents
    val canSave = if (isPersonal) step1Valid else step1Valid && paidShares.isNotEmpty() && paidMatchesTotal && splitValid
    val saveLabel = when {
        asRequest && isEditing -> "Update request"
        asRequest -> "Send request"
        isEditing -> "Update Expense"
        else -> "Save Expense"
    }


    if (showDatePicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = expenseDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { expenseDate = it }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = state) }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when {
                            asRequest && isEditing -> "Edit request"
                            asRequest -> "Request expense"
                            isEditing -> "Edit Expense"
                            else -> "Add Expense"
                        },
                        style = Typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (step == 1) step = 0 else onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .imePadding()
            ) {
                if (step == 0) {
                    if (isPersonal) {
                        Button(
                            onClick = {
                                val finalPaidShares = listOf(PaidShare(personalMemberId!!, amountCents))
                                val finalSplitShares = listOf(SplitShare(personalMemberId!!, amountCents))
                                onSaveExpense(
                                    Expense(
                                        id = existingExpense?.id ?: UUID.randomUUID().toString(),
                                        tripId = tripId,
                                        description = description.trim(),
                                        totalAmount = amountCents,
                                        date = expenseDate,
                                        paidBy = finalPaidShares,
                                        splitAmong = finalSplitShares,
                                        splitMode = SplitMode.PERSONAL,
                                        category = category,
                                        notes = notes.trim().takeIf { it.isNotBlank() },
                                        isPersonal = true,
                                        personalMemberId = personalMemberId
                                    )
                                )
                            },
                            enabled = step1Valid,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
                            )
                        ) {
                            Text(saveLabel, style = Typography.labelLarge)
                        }
                    } else {
                        Button(
                            onClick = { step = 1 },
                            enabled = step1Valid,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
                            )
                        ) {
                            Text("Next: Who Paid?", style = Typography.labelLarge)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = null)
                        }
                    }
                } else {
                    Column {
                        if (!canSave) {
                            val hint = when {
                                paidShares.isEmpty() -> "Enter who paid"
                                !paidMatchesTotal -> {
                                    val diff = amountCents - totalCents
                                    if (diff > 0) "Paid amounts must equal ${Formatters.money(amountCents)} (${Formatters.money(diff)} remaining)"
                                    else "Paid amounts exceed ${Formatters.money(amountCents)} by ${Formatters.money(-diff)}"
                                }
                                !splitValid && splitMode == SplitMode.EXACT -> "Split amounts must equal ${Formatters.money(amountCents)}"
                                !splitValid && splitMode == SplitMode.PERCENTAGE -> "Percentages must add up to 100%"
                                !splitValid && (splitMode == SplitMode.EQUAL || splitMode == SplitMode.SHARES) -> "Include at least one person"
                                else -> null
                            }
                            hint?.let {
                                Text(it, style = Typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 8.dp))
                            }
                        }
                        Button(
                            onClick = {
                                onSaveExpense(
                                    Expense(
                                        id = existingExpense?.id ?: UUID.randomUUID().toString(),
                                        tripId = tripId,
                                        description = description.trim(),
                                        totalAmount = amountCents,
                                        date = expenseDate,
                                        paidBy = paidShares,
                                        splitAmong = splitShares,
                                        splitMode = splitMode,
                                        category = category,
                                        notes = notes.trim().takeIf { it.isNotBlank() },
                                        isPersonal = false,
                                        personalMemberId = null
                                    )
                                )
                            },
                            enabled = canSave,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
                            )
                        ) {
                            Text(saveLabel, style = Typography.labelLarge)
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Step indicator
            StepIndicator(currentStep = step, totalSteps = 2, labels = listOf("Details", "Split"))

            if (step == 0) {
                Step1Content(
                    amountText = amountText,
                    onAmountChange = { amountText = it },
                    description = description,
                    onDescriptionChange = { description = it },
                    category = category,
                    onCategoryChange = { category = it },
                    expenseDate = expenseDate,
                    onDateClick = { showDatePicker = true },
                    isPersonal = isPersonal,
                    onPersonalChange = { isPersonal = it },
                    members = members,
                    currentMemberId = currentMemberId,
                    viewerName = viewerName,
                    personalMemberId = personalMemberId,
                    onPersonalMemberChange = { personalMemberId = it },
                    notes = notes,
                    onNotesChange = { notes = it }
                )
            } else {
                Step2Content(
                    members = members,
                    currentMemberId = currentMemberId,
                    viewerName = viewerName,
                    payerAmounts = payerAmounts,
                    expectedTotalCents = amountCents,
                    totalCents = totalCents,
                    splitMode = splitMode,
                    onSplitModeChange = { splitMode = it },
                    includedMembers = includedMembers,
                    onIncludedMembersChange = { includedMembers = it },
                    splitValues = splitValues,
                    splitShares = splitShares
                )
            }
        }
    }
}

@Composable
private fun StepIndicator(currentStep: Int, totalSteps: Int, labels: List<String>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        labels.forEachIndexed { index, label ->
            val isActive = index == currentStep
            val isDone = index < currentStep
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isDone -> MaterialTheme.colorScheme.primary
                                isActive -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isDone) {
                        Icon(Icons.Outlined.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(16.dp))
                    } else {
                        Text(
                            "${index + 1}",
                            style = Typography.labelLarge,
                            color = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(label, style = Typography.labelSmall, color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (index < totalSteps - 1) {
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(2.dp)
                        .padding(horizontal = 8.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(
                            if (currentStep > index) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Step1Content(
    amountText: String,
    onAmountChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    category: String,
    onCategoryChange: (String) -> Unit,
    expenseDate: Long,
    onDateClick: () -> Unit,
    isPersonal: Boolean,
    onPersonalChange: (Boolean) -> Unit,
    members: List<Member>,
    currentMemberId: String? = null,
    viewerName: String? = null,
    personalMemberId: String?,
    onPersonalMemberChange: (String) -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        // Big amount display
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "₹",
                    style = Typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = onAmountChange,
                    textStyle = MaterialTheme.typography.displayMedium.copy(
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    ),
                    placeholder = {
                        Text(
                            "0",
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Description
        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = { Text("What was it for?") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Quick shortcuts
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(quickShortcuts) { shortcut ->
                AssistChip(
                    onClick = { onDescriptionChange(shortcut.text); onCategoryChange(shortcut.category) },
                    label = { Text(shortcut.label) },
                    colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    border = null
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Category selector
        Text("Category", style = Typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categoryOptions.forEach { opt ->
                val isSelected = category == opt.name
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) opt.color.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .clickable { onCategoryChange(opt.name) }
                        .padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        opt.icon,
                        contentDescription = opt.name,
                        tint = if (isSelected) opt.color else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        opt.name,
                        style = Typography.labelSmall,
                        color = if (isSelected) opt.color else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Personal Expense Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .clickable { onPersonalChange(!isPersonal) }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Personal Expense", style = Typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
                Text("Exclude from group balances", style = Typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
                checked = isPersonal,
                onCheckedChange = { onPersonalChange(it) }
            )
        }

        if (isPersonal) {
            Spacer(modifier = Modifier.height(12.dp))
            Text("Whose expense is this?", style = Typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                members.forEach { member ->
                    FilterChip(
                        selected = personalMemberId == member.id,
                        onClick = { onPersonalMemberChange(member.id) },
                        label = { Text(member.displayName(currentMemberId, viewerName), maxLines = 1, softWrap = false) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Notes
        OutlinedTextField(
            value = notes,
            onValueChange = onNotesChange,
            label = { Text("Notes (Optional)") },
            modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Date picker button
        OutlinedButton(
            onClick = onDateClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Outlined.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Text(Formatters.date(expenseDate), color = MaterialTheme.colorScheme.onBackground)
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Step2Content(
    members: List<Member>,
    currentMemberId: String? = null,
    viewerName: String? = null,
    payerAmounts: androidx.compose.runtime.snapshots.SnapshotStateMap<String, String>,
    expectedTotalCents: Long,
    totalCents: Long,
    splitMode: SplitMode,
    onSplitModeChange: (SplitMode) -> Unit,
    includedMembers: Set<String>,
    onIncludedMembersChange: (Set<String>) -> Unit,
    splitValues: androidx.compose.runtime.snapshots.SnapshotStateMap<String, String>,
    splitShares: List<SplitShare>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("WHO PAID", style = Typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            val match = totalCents == expectedTotalCents
            Text(
                text = if (match) "Paid ${Formatters.money(totalCents)} of ${Formatters.money(expectedTotalCents)} ✓"
                       else "Paid ${Formatters.money(totalCents)} / ${Formatters.money(expectedTotalCents)}",
                style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = if (match) com.ljku.tripsplit.ui.theme.AuroraEmeraldLight else MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (totalCents == expectedTotalCents) "All assigned! Choose how to split below."
                   else {
                       val remaining = expectedTotalCents - totalCents
                       if (remaining > 0) "${Formatters.money(remaining)} remaining to be assigned"
                       else "Paid amounts exceed total by ${Formatters.money(-remaining)}"
                   },
            style = Typography.bodySmall,
            color = if (totalCents == expectedTotalCents) com.ljku.tripsplit.ui.theme.AuroraEmeraldLight else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))

        members.forEach { member ->
            val memberVal = payerAmounts[member.id] ?: ""
            val memberCents = (memberVal.toDoubleOrNull() ?: 0.0).let { (it * 100).toLong() }
            val remainingForMember = (expectedTotalCents - (totalCents - memberCents)).coerceAtLeast(0L)
            val remainingRupees = (remainingForMember / 100.0).let {
                if (it % 1.0 == 0.0) it.toLong().toString() else it.toString()
            }
            MemberAmountRow(
                name = member.displayName(currentMemberId, viewerName),
                value = memberVal,
                onValueChange = { payerAmounts[member.id] = it },
                onSetFull = if (remainingForMember > 0) {
                    { payerAmounts[member.id] = remainingRupees }
                } else null
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text("HOW TO SPLIT", style = Typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))

        // Luxury Segmented Split Mode Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf(
                SplitMode.EQUAL to "Equal",
                SplitMode.SHARES to "Shares",
                SplitMode.EXACT to "Exact",
                SplitMode.PERCENTAGE to "%"
            ).forEach { (mode, label) ->
                val isSelected = splitMode == mode
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else Color.Transparent
                        )
                        .clickable {
                            onSplitModeChange(mode)
                            if (mode == SplitMode.EQUAL) {
                                // Default select all members when Equal is selected
                                onIncludedMembersChange(members.map { it.id }.toSet())
                            }
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = Typography.labelLarge,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                               else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        val modeDescription = when (splitMode) {
            SplitMode.EQUAL -> "Divided equally among selected members"
            SplitMode.SHARES -> "Split proportionally by shares (e.g. 2 for couples, 1 for singles)"
            SplitMode.EXACT -> "Specify exact rupee amounts for each participant"
            SplitMode.PERCENTAGE -> "Allocate percentages totaling 100%"
            SplitMode.PERSONAL -> "Personal expense"
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = modeDescription,
            style = Typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Include members (${includedMembers.size}/${members.size})",
                style = Typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (members.isNotEmpty()) {
                Text(
                    text = if (includedMembers.size == members.size) "Deselect All" else "Select All",
                    style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable {
                            if (includedMembers.size == members.size) {
                                onIncludedMembersChange(emptySet())
                            } else {
                                onIncludedMembersChange(members.map { it.id }.toSet())
                            }
                        }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            members.forEach { member ->
                val selected = includedMembers.contains(member.id)
                FilterChip(
                    selected = selected,
                    onClick = {
                        onIncludedMembersChange(
                            if (selected) includedMembers - member.id else includedMembers + member.id
                        )
                    },
                    label = {
                        Text(
                            text = member.displayName(currentMemberId, viewerName),
                            maxLines = 1,
                            softWrap = false
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        selectedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        val selectedMemberIds = includedMembers.toList()
        when (splitMode) {
            SplitMode.EQUAL -> {
                Text("Split equally among ${selectedMemberIds.size} members", style = Typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            SplitMode.SHARES -> {
                Text("Shares (e.g. 2:1:1)", style = Typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                selectedMemberIds.forEach { memberId ->
                    val member = members.first { it.id == memberId }
                    MemberAmountRow(
                        name = member.displayName(currentMemberId, viewerName),
                        value = splitValues[memberId] ?: "1",
                        onValueChange = { splitValues[memberId] = it },
                        suffix = "shares"
                    )
                }
            }
            SplitMode.EXACT -> {
                selectedMemberIds.forEach { memberId ->
                    val member = members.first { it.id == memberId }
                    MemberAmountRow(name = member.displayName(currentMemberId, viewerName), value = splitValues[memberId] ?: "", onValueChange = { splitValues[memberId] = it })
                }
            }
            SplitMode.PERCENTAGE -> {
                selectedMemberIds.forEach { memberId ->
                    val member = members.first { it.id == memberId }
                    MemberAmountRow(name = member.displayName(currentMemberId, viewerName), value = splitValues[memberId] ?: "", onValueChange = { splitValues[memberId] = it }, suffix = "%")
                }
            }
            SplitMode.PERSONAL -> { }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Summary card
        com.ljku.tripsplit.ui.components.GlassyCard(modifier = Modifier.fillMaxWidth(), elevation = 6.dp) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Expense Total", style = Typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(Formatters.money(expectedTotalCents), style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onBackground)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Paid", style = Typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    val match = totalCents == expectedTotalCents
                    Text(
                        Formatters.money(totalCents),
                        style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (match) com.ljku.tripsplit.ui.theme.AuroraEmeraldLight else com.ljku.tripsplit.ui.theme.AuroraCoralLight
                    )
                }
                if (totalCents != expectedTotalCents) {
                    val remaining = expectedTotalCents - totalCents
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (remaining > 0) "${Formatters.money(remaining)} remaining to be assigned"
                               else "Paid amounts exceed total by ${Formatters.money(-remaining)}",
                        style = Typography.labelSmall,
                        color = com.ljku.tripsplit.ui.theme.AuroraCoralLight
                    )
                }
                if (splitShares.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Split total", style = Typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(Formatters.money(splitShares.sumOf { it.amountOwed }), style = Typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onBackground)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun MemberAmountRow(
    name: String,
    value: String,
    onValueChange: (String) -> Unit,
    suffix: String? = null,
    onSetFull: (() -> Unit)? = null
) {
    com.ljku.tripsplit.ui.components.GlassyCard(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), elevation = 4.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = name,
                style = Typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.width(8.dp))
            if (onSetFull != null && (value.isBlank() || value == "0")) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onSetFull() }
                ) {
                    Text(
                        text = "Fill",
                        style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { Text("0") },
                prefix = if (suffix == null) ({ Text("₹") }) else null,
                suffix = if (suffix != null) ({ Text(suffix) }) else null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.width(130.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    }
}

@Composable
fun AddExpenseScreen(
    tripId: String,
    members: List<Member>,
    defaultPayerId: String? = null,
    currentMemberId: String? = null,
    viewerName: String? = null,
    asRequest: Boolean = false,
    onSaveExpense: (Expense) -> Unit,
    onBack: () -> Unit
) = ExpenseFormScreen(
    tripId = tripId,
    members = members,
    existingExpense = null,
    preselectedMemberId = null,
    defaultPayerId = defaultPayerId,
    currentMemberId = currentMemberId,
    viewerName = viewerName,
    asRequest = asRequest,
    onSaveExpense = onSaveExpense,
    onBack = onBack
)

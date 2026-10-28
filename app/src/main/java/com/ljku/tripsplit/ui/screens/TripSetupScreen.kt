package com.ljku.tripsplit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Luggage
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.ljku.tripsplit.domain.Member
import com.ljku.tripsplit.domain.Trip
import com.ljku.tripsplit.ui.components.GlassyCard
import com.ljku.tripsplit.ui.theme.Typography
import com.ljku.tripsplit.ui.util.Formatters
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripSetupScreen(
    creatorName: String? = null,
    onSaveTrip: (Trip, List<Member>) -> Unit,
    onBack: () -> Unit
) {
    var tripName by remember { mutableStateOf("") }
    var currentMemberName by remember { mutableStateOf("") }
    val members = remember { mutableStateListOf<String>() }
    var startDate by remember { mutableStateOf<Long?>(System.currentTimeMillis()) }
    var endDate by remember { mutableStateOf<Long?>(null) }
    var pickingStart by remember { mutableStateOf(false) }
    var pickingEnd by remember { mutableStateOf(false) }
    var budgetText by remember { mutableStateOf("") }
    var accentColor by remember { mutableStateOf<Int?>(null) }
    
    val colorOptions = listOf(
        android.graphics.Color.parseColor("#5B3FE8") to "Violet",
        android.graphics.Color.parseColor("#10B981") to "Emerald",
        android.graphics.Color.parseColor("#06B6D4") to "Cyan",
        android.graphics.Color.parseColor("#F59E0B") to "Amber",
        android.graphics.Color.parseColor("#EC4899") to "Rose",
        android.graphics.Color.parseColor("#EF4444") to "Coral"
    )

    if (pickingStart) {
        val state = rememberDatePickerState(initialSelectedDateMillis = startDate ?: System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { pickingStart = false },
            confirmButton = {
                TextButton(onClick = {
                    startDate = state.selectedDateMillis
                    pickingStart = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { pickingStart = false }) { Text("Cancel") } }
        ) { DatePicker(state) }
    }

    if (pickingEnd) {
        val state = rememberDatePickerState(initialSelectedDateMillis = endDate ?: System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { pickingEnd = false },
            confirmButton = {
                TextButton(onClick = {
                    endDate = state.selectedDateMillis
                    pickingEnd = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { pickingEnd = false }) { Text("Cancel") } }
        ) { DatePicker(state) }
    }

    val canSave = tripName.isNotBlank() && members.isNotEmpty()

    fun addMember() {
        val trimmed = currentMemberName.trim()
        if (trimmed.isNotBlank() && !members.contains(trimmed)) {
            members.add(trimmed)
            currentMemberName = ""
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "New Trip",
                            style = Typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Set up your group",
                            style = Typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
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
                Button(
                    onClick = {
                        val tripId = UUID.randomUUID().toString()
                        val trip = Trip(
                            id = tripId,
                            name = tripName.trim(),
                            createdAt = System.currentTimeMillis(),
                            memberIds = emptyList(),
                            startDate = startDate,
                            endDate = endDate,
                            budget = budgetText.toLongOrNull()?.let { it * 100 }, // store as cents
                            accentColor = accentColor
                        )
                        val myName = creatorName?.trim()?.ifBlank { null }
                        val hasMatchingMember = myName != null && members.any { it.trim().equals(myName, ignoreCase = true) }
                        val memberList = members.mapIndexed { index, name ->
                            val isManager = if (hasMatchingMember) {
                                name.trim().equals(myName, ignoreCase = true)
                            } else {
                                index == 0
                            }
                            Member(
                                id = UUID.randomUUID().toString(),
                                tripId = tripId,
                                name = name,
                                role = if (isManager) com.ljku.tripsplit.domain.MemberRole.MANAGER
                                else com.ljku.tripsplit.domain.MemberRole.MEMBER
                            )
                        }
                        onSaveTrip(trip, memberList)
                    },
                    enabled = canSave,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
                    )
                ) {
                    Text(
                        text = "Create Trip",
                        style = Typography.labelLarge
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            com.ljku.tripsplit.ui.components.GlassyCard(modifier = Modifier.fillMaxWidth(), elevation = 6.dp) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Luggage,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.size(12.dp))
                        Column {
                            Text(
                                text = "TRIP DETAILS",
                                style = Typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Give it a name",
                                style = Typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    OutlinedTextField(
                        value = tripName,
                        onValueChange = { tripName = it },
                        placeholder = { Text("e.g. Goa Weekend", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        ),
                        shape = CircleShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            com.ljku.tripsplit.ui.components.GlassyCard(modifier = Modifier.fillMaxWidth(), elevation = 6.dp) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.AttachMoney, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.size(12.dp))
                        Column {
                            Text("BUDGET (OPTIONAL)", style = Typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Set a spending limit", style = Typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    OutlinedTextField(
                        value = budgetText,
                        onValueChange = { if (it.all { char -> char.isDigit() }) budgetText = it },
                        placeholder = { Text("e.g. 5000", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number, imeAction = ImeAction.Next),
                        shape = CircleShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            com.ljku.tripsplit.ui.components.GlassyCard(modifier = Modifier.fillMaxWidth(), elevation = 6.dp) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.size(12.dp))
                        Column {
                            Text("THEME (OPTIONAL)", style = Typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Pick a trip color", style = Typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(colorOptions) { (colorInt, name) ->
                            val color = Color(colorInt)
                            val isSelected = accentColor == colorInt
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { accentColor = if (isSelected) null else colorInt }
                                    .background(if (isSelected) color.copy(alpha = 0.2f) else Color.Transparent)
                                    .padding(8.dp)
                            ) {
                                Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(color))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(name, style = Typography.labelSmall, color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            com.ljku.tripsplit.ui.components.GlassyCard(modifier = Modifier.fillMaxWidth(), elevation = 6.dp) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("TRIP DATES", style = Typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(12.dp))
                    SetupDateRow("Start", startDate) { pickingStart = true }
                    Spacer(modifier = Modifier.height(8.dp))
                    SetupDateRow("End", endDate) { pickingEnd = true }
                }
            }

            com.ljku.tripsplit.ui.components.GlassyCard(modifier = Modifier.fillMaxWidth(), elevation = 6.dp) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Group,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.size(12.dp))
                        Column {
                            Text(
                                text = "MEMBERS",
                                style = Typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Who's splitting?",
                                style = Typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = currentMemberName,
                            onValueChange = { currentMemberName = it },
                            placeholder = { Text("Add a name", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = { addMember() }),
                            shape = CircleShape,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                cursorColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        IconButton(
                            onClick = { addMember() },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    if (currentMemberName.isNotBlank()) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Add,
                                contentDescription = "Add member",
                                tint = if (currentMemberName.isNotBlank()) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )
                        }
                    }

                    if (members.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            items(members) { name ->
                                InputChip(
                                    selected = false,
                                    onClick = { members.remove(name) },
                                    label = { Text(name) },
                                    avatar = {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = name.firstOrNull()?.uppercase() ?: "?",
                                                style = Typography.labelMedium,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    },
                                    trailingIcon = {
                                        Icon(
                                            Icons.Outlined.Close,
                                            contentDescription = "Remove",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    colors = InputChipDefaults.inputChipColors(
                                        containerColor = MaterialTheme.colorScheme.surface,
                                        labelColor = MaterialTheme.colorScheme.onBackground
                                    )
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Add at least one member to continue.",
                            style = Typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SetupDateRow(label: String, millis: Long?, onClick: () -> Unit) {
    com.ljku.tripsplit.ui.components.GlassyCard(modifier = Modifier.fillMaxWidth(), onClick = onClick, elevation = 2.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(label, style = Typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    millis?.let { Formatters.date(it) } ?: "Not set",
                    style = Typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Icon(Icons.Outlined.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}

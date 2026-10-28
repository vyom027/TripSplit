package com.ljku.tripsplit.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.draw.clip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.ljku.tripsplit.domain.Member
import com.ljku.tripsplit.domain.Trip
import com.ljku.tripsplit.ui.components.GlassyCard
import com.ljku.tripsplit.ui.theme.AccentTeal
import com.ljku.tripsplit.ui.theme.BackgroundDimWhite
import com.ljku.tripsplit.ui.theme.DividerGray
import com.ljku.tripsplit.ui.theme.SurfaceGlassy
import com.ljku.tripsplit.ui.theme.TextCharcoal
import com.ljku.tripsplit.ui.theme.TextGray
import com.ljku.tripsplit.ui.theme.Typography
import com.ljku.tripsplit.ui.util.Formatters
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTripScreen(
    trip: Trip,
    members: List<Member>,
    onSave: (Trip, List<Member>, List<String>) -> Unit,
    onDeleteTrip: () -> Unit = {},
    onBack: () -> Unit
) {
    var tripName by remember { mutableStateOf(trip.name) }
    var unlocked by remember { mutableStateOf(trip.unlocked) }
    var archived by remember { mutableStateOf(trip.archived) }
    var confirmDelete by remember { mutableStateOf(false) }
    var startDate by remember { mutableStateOf(trip.startDate) }
    var endDate by remember { mutableStateOf(trip.endDate) }
    val memberItems = remember { mutableStateListOf<Member>().apply { addAll(members) } }
    val removedIds = remember { mutableStateListOf<String>() }
    var newMemberName by remember { mutableStateOf("") }
    var pickingStart by remember { mutableStateOf(false) }
    var pickingEnd by remember { mutableStateOf(false) }
    var budgetText by remember { mutableStateOf(trip.budget?.let { (it / 100).toString() } ?: "") }
    var accentColor by remember { mutableStateOf<Int?>(trip.accentColor) }

    val colorOptions = listOf(
        android.graphics.Color.parseColor("#E07A5F") to "Terracotta",
        android.graphics.Color.parseColor("#81B29A") to "Sage",
        android.graphics.Color.parseColor("#3D405B") to "Navy",
        android.graphics.Color.parseColor("#F2CC8F") to "Yellow",
        android.graphics.Color.parseColor("#9C89B8") to "Purple"
    )

    fun openDatePicker(isStart: Boolean) {
        if (isStart) pickingStart = true else pickingEnd = true
    }

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

    Scaffold(
        containerColor = BackgroundDimWhite,
        topBar = {
            TopAppBar(
                title = { Text("Edit Trip", style = Typography.titleLarge, color = TextCharcoal) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back", tint = TextCharcoal)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundDimWhite)
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    onSave(
                        trip.copy(
                            name = tripName.trim(),
                            startDate = startDate,
                            endDate = endDate,
                            budget = budgetText.toLongOrNull()?.let { it * 100 },
                            accentColor = accentColor,
                            unlocked = unlocked,
                            archived = archived
                        ),
                        memberItems.toList(),
                        removedIds.toList()
                    )
                },
                enabled = tripName.isNotBlank() && memberItems.isNotEmpty(),
                modifier = Modifier.fillMaxWidth().padding(24.dp).height(56.dp).imePadding(),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = AccentTeal, contentColor = SurfaceGlassy)
            ) { Text("Save Changes") }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(24.dp)
        ) {
            OutlinedTextField(
                value = tripName,
                onValueChange = { tripName = it },
                label = { Text("Trip name") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentTeal, unfocusedBorderColor = DividerGray)
            )

            Spacer(modifier = Modifier.height(16.dp))
            DateButton("Start date", startDate) { openDatePicker(true) }
            Spacer(modifier = Modifier.height(8.dp))
            DateButton("End date", endDate) { openDatePicker(false) }
            
            Spacer(modifier = Modifier.height(24.dp))
            Text("BUDGET", style = Typography.labelMedium, color = TextGray)
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = budgetText,
                onValueChange = { if (it.all { char -> char.isDigit() }) budgetText = it },
                label = { Text("Budget (optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number, imeAction = ImeAction.Next),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentTeal, unfocusedBorderColor = DividerGray)
            )

            Spacer(modifier = Modifier.height(24.dp))
            Text("THEME", style = Typography.labelMedium, color = TextGray)
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(colorOptions) { (colorInt, name) ->
                    val color = androidx.compose.ui.graphics.Color(colorInt)
                    val isSelected = accentColor == colorInt
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                            .clickable { accentColor = if (isSelected) null else colorInt }
                            .background(if (isSelected) color.copy(alpha = 0.2f) else androidx.compose.ui.graphics.Color.Transparent)
                            .padding(8.dp)
                    ) {
                        Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(color))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(name, style = Typography.labelSmall, color = if (isSelected) color else TextGray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("MEMBERS", style = Typography.labelMedium, color = TextGray)
            Spacer(modifier = Modifier.height(12.dp))

            memberItems.forEach { member ->
                GlassyCard(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(member.name, modifier = Modifier.weight(1f), color = TextCharcoal)
                        IconButton(onClick = {
                            removedIds.add(member.id)
                            memberItems.remove(member)
                        }) {
                            Icon(Icons.Outlined.Close, contentDescription = "Remove", tint = TextGray)
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = newMemberName,
                    onValueChange = { newMemberName = it },
                    label = { Text("Add member") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        if (newMemberName.isNotBlank()) {
                            memberItems.add(Member(UUID.randomUUID().toString(), trip.id, newMemberName.trim()))
                            newMemberName = ""
                        }
                    }),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentTeal, unfocusedBorderColor = DividerGray)
                )
                IconButton(
                    onClick = {
                        if (newMemberName.isNotBlank()) {
                            memberItems.add(Member(UUID.randomUUID().toString(), trip.id, newMemberName.trim()))
                            newMemberName = ""
                        }
                    },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = "Add", tint = AccentTeal)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            TextButton(onClick = { unlocked = !unlocked }) {
                Text(if (unlocked) "Lock ended trip again" else "Unlock ended trip for edits")
            }
            TextButton(onClick = { archived = !archived }) {
                Text(if (archived) "Unarchive trip" else "Archive trip")
            }
            TextButton(onClick = { confirmDelete = true }) {
                Text("Delete trip", color = androidx.compose.material3.MaterialTheme.colorScheme.error)
            }
        }
    }

    if (confirmDelete) {
        ModalBottomSheet(
            onDismissRequest = { confirmDelete = false },
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .width(36.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.outline)
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
                    .padding(bottom = 32.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Delete \"${tripName.ifBlank { "this trip" }}\"?",
                            style = Typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Irreversible action",
                            style = Typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Are you sure you want to permanently delete this trip? All registered members, logged expenses, and settled ledger transactions will be permanently deleted from this device.",
                    style = Typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { confirmDelete = false },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
                    }

                    Button(
                        onClick = {
                            confirmDelete = false
                            onDeleteTrip()
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("Delete Trip")
                    }
                }
            }
        }
    }
}

@Composable
private fun DateButton(label: String, millis: Long?, onClick: () -> Unit) {
    GlassyCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(label, style = Typography.labelLarge, color = TextGray)
                Text(
                    millis?.let { Formatters.date(it) } ?: "Not set",
                    style = Typography.bodyLarge,
                    color = TextCharcoal
                )
            }
            Icon(Icons.Outlined.CalendarMonth, contentDescription = null, tint = AccentTeal)
        }
    }
}

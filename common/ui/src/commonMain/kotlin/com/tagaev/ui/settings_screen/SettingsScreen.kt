package com.tagaev.ui.settings_screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tagaev.core.ui.ThemeMode

private const val KEY_BW_THEME = "isBW_THEME"

/**
 * Minimal settings screen scaffold.
 *
 * Shows a title, a list of (future) parameters including a B/W theme toggle,
 * and a footer with version and actions (contact developer / logout).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(component: ISettingsComponent) {
    val mode by component.themeMode.collectAsState()
    var showContactDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройки") },
                navigationIcon = {

                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            Text("Оформление, стиль приложения", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))

            ThemeRow("Системная", mode == ThemeMode.System) { component.onSelectSystem() }
            ThemeRow("Светлая",   mode == ThemeMode.Light)  { component.onSelectLight() }
            ThemeRow("Тёмная",    mode == ThemeMode.Dark)   { component.onSelectDark() }

            Spacer(Modifier.height(24.dp))
            Text("Связаться с разработчиком", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Button(onClick = { showContactDialog = true }) {
                Text("Написать разработчику")
            }
        }
        if (showContactDialog) {
            AlertDialog(
                onDismissRequest = { showContactDialog = false },
                title = { Text("Связаться с разработчиком") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("✨ Это демонстрационный диалог.")
                        Text("У меня в резюме еще есть ссылка на портфолио с моими другими работами)")
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showContactDialog = false }) {
                        Text("ОК")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showContactDialog = false }) {
                        Text("Позже")
                    }
                }
            )
        }
    }
}

@Composable
private fun ThemeRow(title: String, selected: Boolean, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(title) },
        trailingContent = { RadioButton(selected = selected, onClick = onClick) },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
    )
}

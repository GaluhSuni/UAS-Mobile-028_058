package com.example.eventapp.main

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.eventapp.event.EventApi
import com.example.eventapp.event.Event
import com.example.eventapp.event.ApiResponse
import com.example.eventapp.event.Stats
import com.example.eventapp.MainActivity

class EventFormActivity : ComponentActivity() {

    private val eventApi = EventApi.create()
    private var eventId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        eventId = intent.getStringExtra("EVENT_ID")
        val initialTitle = intent.getStringExtra("TITLE") ?: ""
        val initialDate = intent.getStringExtra("DATE") ?: "2025-12-15"  // Update ke tanggal masa depan
        val initialTime = intent.getStringExtra("TIME") ?: "09:00:00"
        val initialLocation = intent.getStringExtra("LOCATION") ?: ""
        val initialDescription = intent.getStringExtra("DESCRIPTION") ?: ""
        val initialCapacity = intent.getStringExtra("CAPACITY") ?: ""
        val initialStatus = intent.getStringExtra("STATUS") ?: "upcoming"

        setContent {
            MaterialTheme {
                EventForm(
                    initialTitle = initialTitle,
                    initialDate = initialDate,
                    initialTime = initialTime,
                    initialLocation = initialLocation,
                    initialDescription = initialDescription,
                    initialCapacity = initialCapacity,
                    initialStatus = initialStatus,
                    onSave = { event ->
                        if (eventId == null) {
                            createEvent(event)
                        } else {
                            updateEvent(eventId!!, event)
                        }
                    }
                )
            }
        }
    }

    private fun createEvent(event: Event) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = eventApi.createEvent(event)
                withContext(Dispatchers.Main) {
                    if (response.status == 201) {
                        Toast.makeText(this@EventFormActivity, "Event dibuat", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@EventFormActivity, response.message, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EventFormActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateEvent(id: String, event: Event) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = eventApi.updateEvent(id, event)
                withContext(Dispatchers.Main) {
                    if (response.status == 200) {
                        Toast.makeText(this@EventFormActivity, "Event diupdate", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@EventFormActivity, response.message, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EventFormActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventForm(
    initialTitle: String,
    initialDate: String,
    initialTime: String,
    initialLocation: String,
    initialDescription: String,
    initialCapacity: String,
    initialStatus: String,
    onSave: (Event) -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    var date by remember { mutableStateOf(initialDate) }
    var time by remember { mutableStateOf(initialTime) }
    var location by remember { mutableStateOf(initialLocation) }
    var description by remember { mutableStateOf(initialDescription) }
    var capacity by remember { mutableStateOf(initialCapacity) }
    var status by remember { mutableStateOf(initialStatus) }
    var expanded by remember { mutableStateOf(false) }

    val statuses = listOf("upcoming", "ongoing", "completed", "cancelled")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        TextField(value = title, onValueChange = { title = it }, label = { Text("Title") })
        Spacer(modifier = Modifier.height(8.dp))
        TextField(value = date, onValueChange = { date = it }, label = { Text("Date (YYYY-MM-DD)") })
        Spacer(modifier = Modifier.height(8.dp))
        TextField(value = time, onValueChange = { time = it }, label = { Text("Time (HH:MM:SS)") })
        Spacer(modifier = Modifier.height(8.dp))
        TextField(value = location, onValueChange = { location = it }, label = { Text("Location") })
        Spacer(modifier = Modifier.height(8.dp))
        TextField(value = description, onValueChange = { description = it }, label = { Text("Description") })
        Spacer(modifier = Modifier.height(8.dp))
        TextField(value = capacity, onValueChange = { capacity = it }, label = { Text("Capacity") })
        Spacer(modifier = Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            TextField(
                readOnly = true,
                value = status,
                onValueChange = { },
                label = { Text("Status") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                statuses.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption) },
                        onClick = {
                            status = selectionOption
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            if (title.isBlank() || date.isBlank() || time.isBlank() || location.isBlank()) {
                // Validasi sederhana
                return@Button
            }
            val event = Event(
                title = title,
                date = date,
                time = time,
                location = location,
                description = description,
                capacity = capacity.toIntOrNull(),
                status = status
            )
            onSave(event)
        }) {
            Text("Simpan")
        }
    }
}
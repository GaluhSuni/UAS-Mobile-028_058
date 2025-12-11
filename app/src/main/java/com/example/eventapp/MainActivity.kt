package com.example.eventapp

import android.content.Intent
import android.widget.Toast
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.eventapp.ui.theme.EventAppTheme
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
//import androidx.compose.foundation.box
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.eventapp.event.EventApi
import com.example.eventapp.event.Event
import com.example.eventapp.event.ApiResponse
import com.example.eventapp.event.Stats
import com.example.eventapp.main.EventFormActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    private val eventApi = EventApi.create()
    private var events by mutableStateOf(listOf<Event>())
    private var isLoading by mutableStateOf(true)
    private var errorMessage by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Scaffold { padding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    ) {
                        Button(onClick = {
                            startActivity(Intent(this@MainActivity, EventFormActivity::class.java))
                        }) {
                            Text("Tambah Event")
                        }
                        when {
                            isLoading -> {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator()
                                }
                            }

                            errorMessage != null -> {
                                Text(
                                    text = errorMessage!!,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                            }

                            else -> {
                                EventList(events = events, onEdit = ::editEvent, onDelete = ::deleteEvent)
                            }
                        }
                    }
                }
            }
        }
        fetchEvents()
    }

    private fun fetchEvents() {
        isLoading = true
        errorMessage = null
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = eventApi.getAllEvents()
                withContext(Dispatchers.Main) {
                    isLoading = false
                    if (response.status == 200) {
                        events = response.data ?: emptyList()
                    } else {
                        errorMessage = response.message
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isLoading = false
                    errorMessage = "Koneksi gagal: ${e.localizedMessage ?: "Unknown error"}"
                }
            }
        }
    }

    private fun editEvent(event: Event) {
        val intent = Intent(this, EventFormActivity::class.java).apply {
            putExtra("EVENT_ID", event.id)
            putExtra("TITLE", event.title)
            putExtra("DATE", event.date)
            putExtra("TIME", event.time)
            putExtra("LOCATION", event.location)
            putExtra("DESCRIPTION", event.description ?: "")
            putExtra("CAPACITY", event.capacity?.toString() ?: "")
            putExtra("STATUS", event.status ?: "upcoming")
        }
        startActivity(intent)
    }

    private fun deleteEvent(event: Event) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = eventApi.deleteEvent(event.id!!)
                withContext(Dispatchers.Main) {
                    if (response.status == 200) {
                        fetchEvents()
                        Toast.makeText(this@MainActivity, "Event dihapus", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@MainActivity, response.message, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        fetchEvents()
    }
}

@Composable
fun EventList(events: List<Event>, onEdit: (Event) -> Unit, onDelete: (Event) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(events) { event ->
            EventItem(event = event, onEdit = onEdit, onDelete = onDelete)
        }
    }
}

@Composable
fun EventItem(event: Event, onEdit: (Event) -> Unit, onDelete: (Event) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = event.title, style = MaterialTheme.typography.titleMedium)
            Text(text = "${event.date} ${event.time}")
            Text(text = event.location)
            Text(text = event.status)
            Row {
                Button(onClick = { onEdit(event) }) {
                    Text("Edit")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { onDelete(event) }) {
                    Text("Hapus")
                }
            }
        }
    }
}
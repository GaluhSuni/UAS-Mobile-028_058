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
import com.example.eventapp.event.ApiResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.eventapp.event.EventApi
import com.example.eventapp.event.Event

class EventFormActivity : ComponentActivity() {

    private val eventApi = EventApi.create()
    private var eventId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ambil data dari Intent (jika edit)
        eventId = intent.getStringExtra("EVENT_ID")
        val initTitle = intent.getStringExtra("TITLE") ?: ""
        val initDate = intent.getStringExtra("DATE") ?: ""
        val initTime = intent.getStringExtra("TIME") ?: ""
        val initLocation = intent.getStringExtra("LOCATION") ?: ""
        val initDesc = intent.getStringExtra("DESCRIPTION") ?: ""
        val initCapacity = intent.getStringExtra("CAPACITY") ?: ""
        val initStatus = intent.getStringExtra("STATUS") ?: "upcoming"

        setContent {
            MaterialTheme {
                EventFormScreen(
                    eventId = eventId,
                    initialTitle = initTitle,
                    initialDate = initDate,
                    initialTime = initTime,
                    initialLocation = initLocation,
                    initialDescription = initDesc,
                    initialCapacity = initCapacity,
                    initialStatus = initStatus,
                    onSave = { title, date, time, loc, desc, cap, status ->
                        if (eventId == null) createEvent(title, date, time, loc, desc, cap, status)
                        else updateEvent(eventId!!, title, date, time, loc, desc, cap, status)
                    }
                )
            }
        }
    }

    private fun createEvent(
        title: String, date: String, time: String, location: String,
        description: String?, capacity: Int?, status: String
    ) {
        val event = Event(
            title = title,
            date = date,
            time = time,
            location = location,
            description = description,
            capacity = capacity,
            status = status
        )

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

    private fun updateEvent(
        id: String, title: String, date: String, time: String, location: String,
        description: String?, capacity: Int?, status: String
    ) {
        val event = Event(
            title = title,
            date = date,
            time = time,
            location = location,
            description = description,
            capacity = capacity,
            status = status
        )

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

    private suspend fun handleResult(response: ApiResponse<*>, successMsg: String) {
        withContext(Dispatchers.Main) {
            if (response.status in 200..299) {
                Toast.makeText(this@EventFormActivity, successMsg, Toast.LENGTH_SHORT).show()
                finish() // kembali ke MainActivity
            } else {
                Toast.makeText(this@EventFormActivity, "Gagal: ${response.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private suspend fun showError(e: Exception) {
        withContext(Dispatchers.Main) {
            Toast.makeText(this@EventFormActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventFormScreen(
    eventId: String?,
    initialTitle: String,
    initialDate: String,
    initialTime: String,
    initialLocation: String,
    initialDescription: String,
    initialCapacity: String,
    initialStatus: String,
    onSave: (String, String, String, String, String?, Int?, String) -> Unit
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
        Text(text = if (eventId == null) "Tambah Event" else "Edit Event", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Date (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = time, onValueChange = { time = it }, label = { Text("Time (HH:MM)") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Location") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description (optional)") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = capacity, onValueChange = { capacity = it }, label = { Text("Capacity (number)") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))

        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                readOnly = true,
                value = status,
                onValueChange = {},
                label = { Text("Status") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                statuses.forEach {
                    DropdownMenuItem(text = { Text(it) }, onClick = { status = it; expanded = false })
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                if (title.isBlank() || date.isBlank() || time.isBlank() || location.isBlank()) {
                    return@Button
                }
                onSave(title, date, time, location, description.ifBlank { null }, capacity.toIntOrNull(), status)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("SIMPAN")
        }
    }
}


//class EventFormActivity : ComponentActivity() {
//
//    private val eventApi = EventApi.create()
//    private var eventId: String? = null
//    private var isEditMode = false
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        // Ambil data dari Intent (untuk edit)
//        eventId = intent.getStringExtra("EVENT_ID")
//        isEditMode = !eventId.isNullOrBlank()
//
//        val initialTitle = intent.getStringExtra("TITLE") ?: ""
//        val initialDate = intent.getStringExtra("DATE") ?: "2025-12-15"
//        val initialTime = intent.getStringExtra("TIME") ?: "09:00:00"
//        val initialLocation = intent.getStringExtra("LOCATION") ?: ""
//        val initialDescription = intent.getStringExtra("DESCRIPTION") ?: ""
//        val initialCapacity = intent.getStringExtra("CAPACITY") ?: ""
//        val initialStatus = intent.getStringExtra("STATUS") ?: "upcoming"
//
//        setContent {
//            EventAppTheme {  // Gunakan theme yang sama
//                Surface(modifier = Modifier.fillMaxSize()) {
//                    EventForm(
//                        isEditMode = isEditMode,
//                        initialTitle = initialTitle,
//                        initialDate = initialDate,
//                        initialTime = initialTime,
//                        initialLocation = initialLocation,
//                        initialDescription = initialDescription,
//                        initialCapacity = initialCapacity,
//                        initialStatus = initialStatus,
//                        onSave = { event ->
//                            // Log dulu biar tahu sampai sini atau tidak
//                            android.util.Log.d("EventForm", "Event akan disimpan: $event")
//                            if (isEditMode && eventId != null) {
//                                updateEvent(eventId!!, event)
//                            } else {
//                                createEvent(event)
//                            }
//                        },
//                        onCancel = { finish() }
//                    )
//                }
//            }
//        }
//    }
//
//    private fun createEvent(event: Event) {
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                val response = eventApi.createEvent(event)
//                withContext(Dispatchers.Main) {
//                    if (response.status == 201) {
//                        Toast.makeText(this@EventFormActivity, "Event berhasil dibuat", Toast.LENGTH_SHORT).show()
//                        finish()
//                    } else {
//                        Toast.makeText(this@EventFormActivity, "Gagal: ${response.message}", Toast.LENGTH_SHORT).show()
//                    }
//                }
//            } catch (e: Exception) {
//                withContext(Dispatchers.Main) {
//                    Toast.makeText(this@EventFormActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
//                }
//            }
//        }
//    }
//
//    private fun updateEvent(id: String, event: Event) {
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                val response = eventApi.updateEvent(id, event)
//                withContext(Dispatchers.Main) {
//                    if (response.status == 200) {
//                        Toast.makeText(this@EventFormActivity, "Event berhasil diupdate", Toast.LENGTH_SHORT).show()
//                        finish()
//                    } else {
//                        Toast.makeText(this@EventFormActivity, "Gagal: ${response.message}", Toast.LENGTH_SHORT).show()
//                    }
//                }
//            } catch (e: Exception) {
//                withContext(Dispatchers.Main) {
//                    Toast.makeText(this@EventFormActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
//                }
//            }
//        }
//    }
//}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun EventForm(
//    isEditMode: Boolean,
//    initialTitle: String,
//    initialDate: String,
//    initialTime: String,
//    initialLocation: String,
//    initialDescription: String,
//    initialCapacity: String,
//    initialStatus: String,
//    onSave: (Event) -> Unit,
//    onCancel: () -> Unit
//) {
//    var title by remember { mutableStateOf(initialTitle) }
//    var date by remember { mutableStateOf(initialDate) }
//    var time by remember { mutableStateOf(initialTime) }
//    var location by remember { mutableStateOf(initialLocation) }
//    var description by remember { mutableStateOf(initialDescription) }
//    var capacityText by remember { mutableStateOf(initialCapacity) }
//    var status by remember { mutableStateOf(initialStatus) }
//    var expanded by remember { mutableStateOf(false) }
//
//    val context = LocalContext.current   // INI YANG HARUS DITAMBAHKAN
//
//    val statuses = listOf("upcoming", "ongoing", "completed", "cancelled")
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp)
//            .verticalScroll(rememberScrollState())
//    ) {
//        Text(
//            text = if (isEditMode) "Edit Event" else "Tambah Event",
//            style = MaterialTheme.typography.headlineSmall,
//            modifier = Modifier.padding(bottom = 16.dp)
//        )
//
//        OutlinedTextField(
//            value = title,
//            onValueChange = { title = it },
//            label = { Text("Judul Event *") },
//            modifier = Modifier.fillMaxWidth()
//        )
//        Spacer(Modifier.height(8.dp))
//
//        OutlinedTextField(
//            value = date,
//            onValueChange = { date = it },
//            label = { Text("Tanggal (YYYY-MM-DD) *") },
//            modifier = Modifier.fillMaxWidth()
//        )
//        Spacer(Modifier.height(8.dp))
//
//        OutlinedTextField(
//            value = time,
//            onValueChange = { time = it },
//            label = { Text("Waktu (HH:MM:SS) *") },
//            modifier = Modifier.fillMaxWidth()
//        )
//        Spacer(Modifier.height(8.dp))
//
//        OutlinedTextField(
//            value = location,
//            onValueChange = { location = it },
//            label = { Text("Lokasi *") },
//            modifier = Modifier.fillMaxWidth()
//        )
//        Spacer(Modifier.height(8.dp))
//
//        OutlinedTextField(
//            value = description,
//            onValueChange = { description = it },
//            label = { Text("Deskripsi") },
//            modifier = Modifier.fillMaxWidth(),
//            minLines = 3
//        )
//        Spacer(Modifier.height(8.dp))
//
//        OutlinedTextField(
//            value = capacityText,
//            onValueChange = { capacityText = it },
//            label = { Text("Kapasitas (opsional)") },
//            modifier = Modifier.fillMaxWidth()
//        )
//        Spacer(Modifier.height(8.dp))
//
//        ExposedDropdownMenuBox(
//            expanded = expanded,
//            onExpandedChange = { expanded = it }
//        ) {
//            OutlinedTextField(
//                readOnly = true,
//                value = status,
//                onValueChange = { },
//                label = { Text("Status") },
//                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
//                modifier = Modifier.menuAnchor().fillMaxWidth()
//            )
//            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
//                statuses.forEach { option ->
//                    DropdownMenuItem(
//                        text = { Text(option) },
//                        onClick = {
//                            status = option
//                            expanded = false
//                        }
//                    )
//                }
//            }
//        }
//
//        Spacer(Modifier.height(24.dp))
//
//        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
//            TextButton(onClick = onCancel) {
//                Text("Batal")
//            }
//            Spacer(Modifier.width(8.dp))
//            Button(onClick = {
//                if (title.isBlank() || date.isBlank() || time.isBlank() || location.isBlank()) {
//                    Toast.makeText(context, "Harap isi semua kolom wajib", Toast.LENGTH_SHORT).show()
//                    return@Button
//                }
//
//                val capacityInt = capacityText.toIntOrNull()
//
//                val event = Event(
//                    id = null,                                    // PASTI null, jangan pakai eventId di sini
//                    title = title.trim(),
//                    date = date.trim(),
//                    time = time.trim(),
//                    location = location.trim(),
//                    description = description.trim().ifBlank { null },
//                    capacity = capacityText.toIntOrNull(),
//                    status = status
//                )
//                onSave(event)
//            }) {
//                Text(if (isEditMode) "Update" else "Simpan")
//            }
//        }
//    }
//}
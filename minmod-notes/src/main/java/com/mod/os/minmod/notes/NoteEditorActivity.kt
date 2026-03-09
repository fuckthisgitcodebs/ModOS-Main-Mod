package com.mod.os.minmod.notes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mod.os.recents.data.ClipEntry
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NoteEditorActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val clip = intent.getParcelableExtra<ClipEntry>("clip")

        setContent {
            MaterialTheme {
                NoteEditorScreen(initialContent = clip?.fullContent ?: "")
            }
        }
    }
}

@Composable
fun NoteEditorScreen(
    initialContent: String,
    modifier: Modifier = Modifier
) {
    var noteText by remember { mutableStateOf(initialContent) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(24.dp)
    ) {
        Text(
            text = "Edit Note",
            style = MaterialTheme.typography.headlineMedium,
            color = Color(0xFF00D4FF)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = noteText,
            onValueChange = { noteText = it },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00D4FF),
                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                cursorColor = Color(0xFF00D4FF)
            ),
            placeholder = { Text("Paste or edit your note here...") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { /* Save to host storage / SharedPreferences */ },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00D4FF))
            ) {
                Text("Save Note")
            }

            Button(
                onClick = { /* Copy to clipboard via host */ },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0288D1))
            ) {
                Text("Copy")
            }
        }
    }
}

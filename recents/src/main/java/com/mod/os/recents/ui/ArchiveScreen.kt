package com.mod.os.recents.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.mod.os.recents.clipboard.ClipboardRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveScreen(
    repository: ClipboardRepository,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }

    val pagingFlow = remember(searchQuery) {
        if (searchQuery.isBlank()) repository.getArchivedPagingFlow()
        else repository.searchArchivedPagingFlow(searchQuery)
    }
    val pagingItems = pagingFlow.collectAsLazyPagingItems()

    Column(modifier = modifier) {
        SearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            onSearch = { },
            active = false,
            onActiveChange = { },
            placeholder = { Text("Search archived clips…") }
        ) { }

        LazyColumn {
            items(
                count = pagingItems.itemCount,
                key = pagingItems.itemKey { it.id }
            ) { index ->
                val clip = pagingItems[index] ?: return@items
                ListItem(
                    headlineContent = {
                        Text(clip.contentPreview.take(80) + if (clip.contentPreview.length > 80) "..." else "")
                    },
                    supportingContent = {
                        Text("${clip.sourcePackage} • ${clip.timestamp}")
                    },
                    leadingContent = {
                        Text(clip.clipType.name, style = MaterialTheme.typography.labelSmall)
                    }
                )
                HorizontalDivider()
            }
        }
    }
}

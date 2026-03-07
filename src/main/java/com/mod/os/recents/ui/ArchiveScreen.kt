package com.mod.os.recents.ui

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.paging.*
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.mod.os.recents.clipboard.ClipboardRepository
import com.mod.os.recents.data.ClipEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveScreen(
    repository: ClipboardRepository,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }

    val pagingItems = remember(searchQuery) {
        Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = {
                object : PagingSource<Int, ClipEntry>() {
                    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ClipEntry> {
                        val page = params.key ?: 0
                        val limit = params.loadSize
                        val offset = page * limit

                        val items = if (searchQuery.isBlank()) {
                            repository.clipDao.getArchivedClipsPaginated(limit, offset).first()
                        } else {
                            repository.clipDao.searchArchivedPaginated(searchQuery, limit, offset).first()
                        }

                        return LoadResult.Page(
                            data = items,
                            prevKey = if (page == 0) null else page - 1,
                            nextKey = if (items.size < limit) null else page + 1
                        )
                    }

                    override fun getRefreshKey(state: PagingState<Int, ClipEntry>): Int? = null
                }
            }
        ).flow.collectAsLazyPagingItems()
    }

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
                    headlineContent = { Text(clip.contentPreview.take(80) + if (clip.contentPreview.length > 80) "..." else "") },
                    supportingContent = {
                        Text("${clip.sourcePackage} • ${clip.timestamp}")
                    },
                    leadingContent = {
                        Text(clip.clipType.name, style = MaterialTheme.typography.labelSmall)
                    }
                )
                Divider()
            }
        }
    }
}

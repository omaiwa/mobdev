package io.github.mobdev.ui.messages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import io.github.mobdev.R
import io.github.mobdev.domain.ChatMessage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(
    uiState: MessagesUiState,
    onBackClick: (() -> Unit)?,
    onInputChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onLoadMore: () -> Unit,
    onImageClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val shouldLoadMore by remember {
        derivedStateOf {
            uiState.canLoadMore &&
                !uiState.isLoadingMore &&
                listState.firstVisibleItemIndex == 0 &&
                listState.firstVisibleItemScrollOffset == 0 &&
                listState.layoutInfo.totalItemsCount > 1
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            onLoadMore()
        }
    }

    LaunchedEffect(uiState.messages.lastOrNull()?.id) {
        val lastMessageIndex = uiState.messages.lastIndex
        if (lastMessageIndex < 0) return@LaunchedEffect
        val loadMoreHeader = if (uiState.canLoadMore || uiState.isLoadingMore) 1 else 0
        listState.animateScrollToItem(lastMessageIndex + loadMoreHeader)
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(uiState.channelName) },
                navigationIcon = {
                    if (onBackClick != null) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.messages_back),
                            )
                        }
                    }
                },
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = uiState.inputText,
                    onValueChange = onInputChange,
                    placeholder = { Text(stringResource(R.string.message_input_hint)) },
                    singleLine = true,
                )
                IconButton(
                    onClick = onSendClick,
                    enabled = uiState.inputText.isNotBlank(),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = stringResource(R.string.send),
                    )
                }
            }
        },
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.messages.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = stringResource(R.string.no_messages))
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (uiState.canLoadMore || uiState.isLoadingMore) {
                        item(key = "load_more") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (uiState.isLoadingMore) {
                                    Text(text = stringResource(R.string.load_more))
                                } else {
                                    TextButton(onClick = onLoadMore) {
                                        Text(text = stringResource(R.string.load_more_button))
                                    }
                                }
                            }
                        }
                    }
                    items(uiState.messages, key = { it.id }) { message ->
                        MessageItem(
                            message = message,
                            onImageClick = onImageClick,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageItem(
    message: ChatMessage,
    onImageClick: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
    ) {
        Text(text = message.from)
        when (message) {
            is ChatMessage.Text -> {
                Text(text = message.text)
            }
            is ChatMessage.Image -> {
                AsyncImage(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp)
                        .clickable { onImageClick(message.imagePath) },
                    model = message.thumbUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                )
            }
        }
    }
}

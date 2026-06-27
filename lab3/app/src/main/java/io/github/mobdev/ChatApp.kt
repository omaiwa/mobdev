package io.github.mobdev

import android.content.res.Configuration
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.mobdev.data.api.NetworkModule
import io.github.mobdev.ui.auth.AuthViewModel
import io.github.mobdev.ui.auth.LoginScreen
import io.github.mobdev.ui.chats.ChatListScreen
import io.github.mobdev.ui.chats.ChatListViewModel
import io.github.mobdev.ui.image.ImageScreen
import io.github.mobdev.ui.messages.MessagesScreen
import io.github.mobdev.ui.messages.MessagesViewModel

private enum class PortraitDestination {
    ChatList,
    Messages,
}

@Composable
fun ChatApp(
    onFinishApp: () -> Unit,
) {
    val repository = ChatDependencies.repository
    val savedStateOwner = LocalContext.current as ComponentActivity
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory(repository))
    val chatListViewModel: ChatListViewModel = viewModel(factory = ChatListViewModel.Factory(repository))
    val messagesViewModel: MessagesViewModel = viewModel(
        factory = MessagesViewModel.Factory(
            owner = savedStateOwner,
            repository = repository,
        ),
    )

    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
    val chatListState by chatListViewModel.uiState.collectAsStateWithLifecycle()
    val messagesState by messagesViewModel.uiState.collectAsStateWithLifecycle()

    var portraitDestination by rememberSaveable { mutableStateOf(PortraitDestination.ChatList) }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    LaunchedEffect(Unit) {
        authViewModel.tryAutoLogin()
    }

    LaunchedEffect(authState.isLoggedIn) {
        if (authState.isLoggedIn) {
            chatListViewModel.loadChannelsIfNeeded()
        } else {
            portraitDestination = PortraitDestination.ChatList
        }
    }

    LaunchedEffect(Unit) {
        NetworkModule.unauthorizedEventBus.events.collect {
            messagesViewModel.clear()
            chatListViewModel.selectChannel(null)
            authViewModel.onLoggedOut()
            portraitDestination = PortraitDestination.ChatList
        }
    }

    if (!authState.isLoggedIn) {
        LoginScreen(
            uiState = authState,
            onUsernameChange = authViewModel::onUsernameChange,
            onPasswordChange = authViewModel::onPasswordChange,
            onLoginClick = authViewModel::login,
            onDismissError = authViewModel::dismissError,
        )
        BackHandler {
            onFinishApp()
        }
        return
    }

    val selectedChannel = chatListState.selectedChannelName
    val openImagePath = messagesState.openImagePath

    fun openChannel(channelName: String) {
        chatListViewModel.selectChannel(channelName)
        messagesViewModel.bind(channelName, authState.username)
        messagesViewModel.loadInitialIfNeeded()
        if (!isLandscape) {
            portraitDestination = PortraitDestination.Messages
        }
    }

    fun exitChat() {
        chatListViewModel.selectChannel(null)
        portraitDestination = PortraitDestination.ChatList
    }

    LaunchedEffect(isLandscape, selectedChannel) {
        if (!isLandscape && selectedChannel != null && portraitDestination == PortraitDestination.ChatList) {
            portraitDestination = PortraitDestination.Messages
        }
    }

    BackHandler {
        when {
            openImagePath != null -> messagesViewModel.closeImage()
            isLandscape && selectedChannel != null -> chatListViewModel.selectChannel(null)
            !isLandscape && portraitDestination == PortraitDestination.Messages -> exitChat()
            else -> onFinishApp()
        }
    }

    if (isLandscape) {
        Row(modifier = Modifier.fillMaxSize()) {
            ChatListScreen(
                modifier = Modifier
                    .width(280.dp)
                    .fillMaxHeight(),
                uiState = chatListState,
                onChannelClick = { channel -> openChannel(channel.name) },
                onLogoutClick = {
                    chatListViewModel.logout {
                        messagesViewModel.clear()
                        authViewModel.onLoggedOut()
                    }
                },
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            ) {
                when {
                    openImagePath != null -> {
                        ImageScreen(
                            imageUrl = messagesViewModel.fullImageUrl().orEmpty(),
                            onClose = messagesViewModel::closeImage,
                        )
                    }
                    selectedChannel == null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = stringResource(R.string.select_chat),
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                    }
                    else -> {
                        MessagesScreen(
                            uiState = messagesState,
                            onBackClick = null,
                            onInputChange = messagesViewModel::onInputChange,
                            onSendClick = messagesViewModel::sendMessage,
                            onLoadMore = messagesViewModel::loadOlderMessages,
                            onImageClick = messagesViewModel::openImage,
                        )
                    }
                }
            }
        }
        return
    }

    if (openImagePath != null) {
        ImageScreen(
            imageUrl = messagesViewModel.fullImageUrl().orEmpty(),
            onClose = messagesViewModel::closeImage,
        )
        return
    }

    when (portraitDestination) {
        PortraitDestination.ChatList -> {
            ChatListScreen(
                uiState = chatListState,
                onChannelClick = { channel -> openChannel(channel.name) },
                onLogoutClick = {
                    chatListViewModel.logout {
                        messagesViewModel.clear()
                        authViewModel.onLoggedOut()
                    }
                },
            )
        }
        PortraitDestination.Messages -> {
            LaunchedEffect(selectedChannel, authState.username) {
                val channel = selectedChannel ?: return@LaunchedEffect
                messagesViewModel.bind(channel, authState.username)
                messagesViewModel.loadInitialIfNeeded()
            }
            MessagesScreen(
                uiState = messagesState,
                onBackClick = { exitChat() },
                onInputChange = messagesViewModel::onInputChange,
                onSendClick = messagesViewModel::sendMessage,
                onLoadMore = messagesViewModel::loadOlderMessages,
                onImageClick = messagesViewModel::openImage,
            )
        }
    }
}

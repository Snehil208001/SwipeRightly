package com.example.swiperightly.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.swiperightly.CommonDivider
import com.example.swiperightly.CommonImage
import com.example.swiperightly.CommonProgressSpinner
import com.example.swiperightly.SWViewModel
import com.example.swiperightly.data.Message

@Composable
fun SingleChatScreen(navController: NavController, vm: SWViewModel, chatId: String) {
    // 1. Setup listener when the screen is first composed
    LaunchedEffect(key1 = Unit) {
        vm.populateChat(chatId)
    }

    // 2. Handle the back press to correctly clean up the listener
    BackHandler {
        vm.depopulateChat()
        navController.popBackStack()
    }

    var reply by rememberSaveable { mutableStateOf("") }

    // 3. Use `firstOrNull` to prevent crashes if chat data is not yet available
    val currentChat = vm.chats.value.firstOrNull { it.chatId == chatId }
    val myId = vm.userData.value?.userId
    val chatMessages = vm.chatMessages.value

    if (currentChat == null || myId == null) {
        // Show a loading spinner while the chat data is being fetched
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CommonProgressSpinner()
        }
    } else {
        val chatUser = if (myId == currentChat.user1.userId) currentChat.user2 else currentChat.user1
        val onSendReply = {
            vm.onSendReply(chatId, reply)
            reply = ""
        }

        Column(modifier = Modifier.fillMaxSize()) {
            // Chat header
            ChatHeader(name = chatUser.name ?: "", imageUrl = chatUser.imageUrl ?: "") {
                navController.popBackStack()
                vm.depopulateChat() // Clean up listener on manual back navigation
            }

            // Messages
            Messages(
                modifier = Modifier.weight(1f),
                chatMessages = chatMessages,
                currentUserId = myId
            )

            // Reply box
            ReplyBox(reply = reply, onReplyChange = { reply = it }, onSendReply = onSendReply)
        }
    }
}

@Composable
fun ChatHeader(name: String, imageUrl: String, onBackClicked: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Rounded.ArrowBack,
            contentDescription = "Back",
            modifier = Modifier
                .clickable { onBackClicked.invoke() }
                .padding(8.dp)
        )
        CommonImage(
            data = imageUrl,
            modifier = Modifier
                .padding(8.dp)
                .size(50.dp)
                .clip(CircleShape)
        )
        Text(
            text = name,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
    CommonDivider()
}

@Composable
fun Messages(modifier: Modifier, chatMessages: List<Message>, currentUserId: String) {
    LazyColumn(modifier = modifier) {
        items(chatMessages) { msg ->
            msg.message?.let {
                val alignment = if (msg.sentBy == currentUserId) Alignment.End else Alignment.Start
                val color = if (msg.sentBy == currentUserId) Color(0xFF68C400) else Color(0xFFC0C0C0)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalAlignment = alignment
                ) {
                    Text(
                        text = it,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(color)
                            .padding(12.dp),
                        color = Color.White
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReplyBox(reply: String, onReplyChange: (String) -> Unit, onSendReply: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        CommonDivider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = reply,
                onValueChange = onReplyChange,
                maxLines = 3,
                modifier = Modifier.weight(1f)
            )
            Button(onClick = onSendReply, modifier = Modifier.padding(start = 8.dp)) {
                Text(text = "Send")
            }
        }
    }
}
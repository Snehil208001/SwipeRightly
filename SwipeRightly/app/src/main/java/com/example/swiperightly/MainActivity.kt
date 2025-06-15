package com.example.swiperightly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.swiperightly.swipecards.SwipeScreen
import com.example.swiperightly.ui.ChatListScreen
import com.example.swiperightly.ui.LoginScreen
import com.example.swiperightly.ui.ProfileScreen
import com.example.swiperightly.ui.SignUpScreen
import com.example.swiperightly.ui.SingleChatScreen
import com.example.swiperightly.ui.theme.SwipeRightlyTheme
import dagger.hilt.android.AndroidEntryPoint

sealed class DestinationScreen(val route: String){
    object SignUp : DestinationScreen("signup")
    object Login : DestinationScreen("login")
    object Profile : DestinationScreen("profile")
    object Swipe : DestinationScreen("swipe")
    object ChatList : DestinationScreen("chatList")
    object SingleChat : DestinationScreen("singleChat/{chatId}")

    fun createRoute(id: String) = "singleChat/$id"
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // This enables the edge-to-edge display, allowing your UI to draw behind the system bars
        enableEdgeToEdge()
        setContent {
            SwipeRightlyTheme {
                SwipeAppNavigation()
            }
        }
    }
}

@Composable
fun SwipeAppNavigation(){
    val navController = rememberNavController()

    val vm = hiltViewModel<SWViewModel>()

    NotificationMessage(vm = vm)

    NavHost(navController = navController, startDestination = DestinationScreen.SignUp.route){
        composable(DestinationScreen.SignUp.route){
            SignUpScreen(navController, vm)
        }
        composable(DestinationScreen.Login.route){
            LoginScreen(navController, vm)
        }
        composable(DestinationScreen.Profile.route){
            ProfileScreen(navController, vm)
        }
        composable(DestinationScreen.Swipe.route){
            SwipeScreen(navController,vm)
        }
        composable(DestinationScreen.ChatList.route){
            ChatListScreen(navController,vm)
        }
        composable(DestinationScreen.SingleChat.route){
            SingleChatScreen(navController, vm, it.arguments?.getString("chatId") ?: "")
        }
    }
}
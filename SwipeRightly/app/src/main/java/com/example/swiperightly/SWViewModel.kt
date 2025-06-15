package com.example.swiperightly

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.swiperightly.data.COLLECTION_CHAT
import com.example.swiperightly.data.COLLECTION_MESSAGES
import com.example.swiperightly.data.COLLECTION_USER
import com.example.swiperightly.data.ChatData
import com.example.swiperightly.data.ChatUser
import com.example.swiperightly.data.Event
import com.example.swiperightly.data.Message
import com.example.swiperightly.data.UserData
import com.example.swiperightly.ui.Gender
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class SWViewModel @Inject constructor(
    val auth: FirebaseAuth,
    val db: FirebaseFirestore,
    val storage: FirebaseStorage
) : ViewModel() {

    val inProgress = mutableStateOf(false)
    val popupNotification = mutableStateOf<Event<String>?>(null)
    val signedIn = mutableStateOf(false)
    val userData = mutableStateOf<UserData?>(null)

    val matchProfiles = mutableStateOf<List<UserData>>(listOf())
    val inProgressProfiles = mutableStateOf(false)

    val chats = mutableStateOf<List<ChatData>>(listOf())
    val inProgressChats = mutableStateOf(false)

    val inProgressChatMessages = mutableStateOf(false)
    val chatMessages = mutableStateOf<List<Message>>(listOf())
    var currentChatMessagesListener: ListenerRegistration? = null

    init {
        val currentUser = auth.currentUser
        signedIn.value = currentUser != null
        currentUser?.uid?.let { uid ->
            getUserData(uid)
        }
    }

    fun onSignup(username: String, email: String, password: String) {
        if (username.isBlank() || email.isBlank() || password.isBlank()) {
            handleException(customMessage = "Please fill in all fields")
            return
        }
        inProgress.value = true
        db.collection(COLLECTION_USER).whereEqualTo("username", username)
            .get()
            .addOnSuccessListener {
                if (it.isEmpty)
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                signedIn.value = true
                                createOrUpdateProfile(username = username)
                            } else {
                                handleException(task.exception, "Signup failed")
                            }
                        }
                else {
                    handleException(customMessage = "Username already exists")
                    inProgress.value = false
                }
            }
            .addOnFailureListener {
                handleException(it)
            }
    }

    fun onLogin(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            handleException(customMessage = "Please fill in all fields")
            return
        }
        inProgress.value = true
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    signedIn.value = true
                    inProgress.value = false
                    auth.currentUser?.uid?.let {
                        getUserData(it)
                    }
                } else {
                    handleException(task.exception, "Login failed")
                }
            }
            .addOnFailureListener {
                handleException(it, "Login failed")
            }
    }

    fun createOrUpdateProfile(
        name: String? = null,
        username: String? = null,
        bio: String? = null,
        imageUrl: String? = null,
        gender: Gender? = null,
        genderPreference: Gender? = null
    ) {
        val uid = auth.currentUser?.uid
        val updatedData = UserData(
            userId = uid,
            name = name ?: userData.value?.name,
            username = username ?: userData.value?.username,
            imageUrl = imageUrl ?: userData.value?.imageUrl,
            bio = bio ?: userData.value?.bio,
            gender = gender?.name ?: userData.value?.gender,
            genderPreference = genderPreference?.name ?: userData.value?.genderPreference
        )

        uid?.let { userUid ->
            inProgress.value = true
            db.collection(COLLECTION_USER).document(userUid)
                .get()
                .addOnSuccessListener {
                    if (it.exists()) {
                        it.reference.update(updatedData.toMap())
                            .addOnSuccessListener {
                                getUserData(userUid)
                            }
                            .addOnFailureListener { e ->
                                handleException(e, "Cannot update user")
                            }
                    } else {
                        db.collection(COLLECTION_USER).document(userUid).set(updatedData)
                            .addOnSuccessListener {
                                getUserData(userUid)
                            }
                            .addOnFailureListener { e ->
                                handleException(e, "Cannot create user")
                            }
                    }
                }
                .addOnFailureListener {
                    handleException(it, "Cannot create user profile")
                }
        }
    }

    private fun getUserData(uid: String) {
        inProgress.value = true
        db.collection(COLLECTION_USER).document(uid)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    handleException(error, "Cannot retrieve user data")
                    return@addSnapshotListener
                }
                if (value != null) {
                    userData.value = value.toObject<UserData>()
                    populateCards() // Re-populate cards whenever user data changes
                    populateChats()
                }
                inProgress.value = false
            }
    }

    fun onLogout() {
        auth.signOut()
        signedIn.value = false
        userData.value = null
        popupNotification.value = Event("Logged out")
    }

    fun uploadProfileImage(uri: Uri) {
        inProgress.value = true
        val uid = auth.currentUser?.uid
        if (uid != null) {
            val storageRef = storage.reference.child("images/$uid/${UUID.randomUUID()}")
            storageRef.putFile(uri)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { imageUrl ->
                        createOrUpdateProfile(imageUrl = imageUrl.toString())
                    }
                }
                .addOnFailureListener {
                    handleException(it, "Image upload failed")
                }
        } else {
            handleException(customMessage = "User not logged in.")
        }
    }

    private fun handleException(exception: Exception? = null, customMessage: String = "") {
        Log.e("SwipeRightly", "Tinder Exception", exception)
        exception?.printStackTrace()
        val errorMsg = exception?.localizedMessage ?: ""
        val message = if (customMessage.isEmpty()) errorMsg else "$customMessage: $errorMsg"
        popupNotification.value = Event(message)
        inProgress.value = false
        inProgressProfiles.value = false // Also turn off profiles loading on error
    }

    private fun populateCards() {
        inProgressProfiles.value = true

        val currentUserData = userData.value
        if (currentUserData?.userId == null) {
            inProgressProfiles.value = false
            return
        }

        val preferredGender = currentUserData.genderPreference?.takeIf { it.isNotEmpty() } ?: "ANY"

        val cardsQuery = if (preferredGender == "ANY") {
            db.collection(COLLECTION_USER)
        } else {
            db.collection(COLLECTION_USER).whereEqualTo("gender", preferredGender)
        }

        val seenUserIds = (currentUserData.swipeLeft + currentUserData.swipeRight + currentUserData.matches + currentUserData.userId).toSet()

        cardsQuery.get()
            .addOnSuccessListener { result ->
                val potentialMatches = result.documents.mapNotNull { it.toObject<UserData>() }
                val newProfiles = potentialMatches.filter { potential ->
                    potential.userId !in seenUserIds &&
                            (potential.genderPreference == "ANY" || potential.genderPreference == currentUserData.gender)
                }
                matchProfiles.value = newProfiles
                inProgressProfiles.value = false
            }
            .addOnFailureListener { e ->
                handleException(e, "Failed to populate cards")
            }
    }


    fun onDislike(selectedUser: UserData){
        db.collection(COLLECTION_USER).document(userData.value?.userId ?: "")
            .update("swipeLeft", FieldValue.arrayUnion(selectedUser.userId))
    }

    fun onLike(selectedUser: UserData){
        val reciprocalMatch = selectedUser.swipeRight.contains(userData.value?.userId)
        if (!reciprocalMatch){
            db.collection(COLLECTION_USER).document(userData.value?.userId ?: "")
                .update("swipeRight", FieldValue.arrayUnion(selectedUser.userId))
        }else{
            popupNotification.value = Event("Match!")

            db.collection(COLLECTION_USER).document(selectedUser.userId ?: "")
                .update("swipeRight", FieldValue.arrayUnion(userData.value?.userId))
            db.collection(COLLECTION_USER).document(userData.value?.userId ?: "")
                .update("matches", FieldValue.arrayUnion(selectedUser.userId))
            db.collection(COLLECTION_USER).document(selectedUser.userId ?: "")
                .update("matches", FieldValue.arrayUnion(userData.value?.userId))

            val chatKey = db.collection(COLLECTION_CHAT).document().id
            val chatData = ChatData(
                chatKey,
                ChatUser(
                    userData.value?.userId,
                    if (userData.value?.name.isNullOrEmpty()) userData.value?.username
                        else userData.value?.name,
                    userData.value?.imageUrl
                ),
                ChatUser(
                    selectedUser.userId,
                    if (selectedUser.name.isNullOrEmpty()) selectedUser.username
                         else selectedUser.name,
                    selectedUser.imageUrl
                )

                )
            db.collection(COLLECTION_CHAT).document(chatKey).set(chatData)




        }
    }

    private fun populateChats(){
        inProgressChats.value = true
        db.collection(COLLECTION_CHAT).where(
            Filter.or(
                Filter.equalTo("user1.userId", userData.value?.userId),
                Filter.equalTo("user2.userId", userData.value?.userId)
            )
        )
            .addSnapshotListener { value, error ->
                if (error != null)
                    handleException(error)
                if (value != null)
                    chats.value = value.documents.mapNotNull { it.toObject<ChatData>() }
                inProgressChats.value = false
            }
    }

    fun onSendReply(chatId: String, message: String){
        val time = Calendar.getInstance().time.toString()
        val message = Message(userData.value?.userId, message, time)
        db.collection(COLLECTION_CHAT).document(chatId)
            .collection(COLLECTION_MESSAGES).document().set(message)
    }

    fun populateChat(chatId: String){
        inProgressChatMessages.value = true
        currentChatMessagesListener = db.collection(COLLECTION_CHAT)
            .document(chatId)
            .collection(COLLECTION_MESSAGES)
            .addSnapshotListener { value, error ->
                if (error != null)
                    handleException(error)
                if (value != null)
                    chatMessages.value = value.documents.mapNotNull { it.toObject<Message>() }
                inProgressChatMessages.value = false
            }
    }

    fun depopulateChat(){
        currentChatMessagesListener = null
        chatMessages.value = listOf()
    }

}
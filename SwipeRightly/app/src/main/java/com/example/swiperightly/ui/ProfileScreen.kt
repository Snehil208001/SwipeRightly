package com.example.swiperightly.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.swiperightly.*

enum class Gender {
    MALE, FEMALE, ANY
}

@Composable
fun ProfileScreen(navController: NavController, vm: SWViewModel) {
    val inProgress = vm.inProgress.value

    // We show the spinner for any background operation
    if (inProgress) {
        CommonProgressSpinner()
    }

    val userData = vm.userData.value
    val g = if (userData?.gender.isNullOrEmpty())"MALE"
        else userData!!.gender!!.uppercase()
    val gPref = if (userData?.genderPreference.isNullOrEmpty()) "FEMALE"
        else userData!!.genderPreference!!.uppercase()
    var name by rememberSaveable { mutableStateOf(userData?.name ?: "") }
    var username by rememberSaveable { mutableStateOf(userData?.username ?: "") }
    var bio by rememberSaveable { mutableStateOf(userData?.bio ?: "") }
    var gender by rememberSaveable { mutableStateOf(Gender.valueOf(g)) }
    var genderPreference by rememberSaveable { mutableStateOf(Gender.valueOf(gPref)) }

    // --- *** THIS IS THE KEY FIX FOR THE UI *** ---
    // This LaunchedEffect will run whenever `userData` from the ViewModel changes.
    // It resets the local state, ensuring the UI reflects the latest data from Firestore.
    LaunchedEffect(userData) {
        name = userData?.name ?: ""
        username = userData?.username ?: ""
        bio = userData?.bio ?: ""
        gender = Gender.valueOf(userData?.gender?.uppercase() ?: "MALE")
        genderPreference = Gender.valueOf(userData?.genderPreference?.uppercase() ?: "FEMALE")
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // The content is always visible, with the spinner overlaying it if inProgress is true.
        ProfileContent(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(8.dp),
            vm = vm,
            name = name,
            username = username,
            bio = bio,
            gender = gender,
            genderPreference = genderPreference,
            onNameChanged = { name = it },
            onUsernameChanged = { username = it },
            onBioChanged = { bio = it },
            onGenderChanged = { gender = it },
            onGenderPreferenceChanged = { genderPreference = it },
            onSave = {
                vm.createOrUpdateProfile(
                    name = name,
                    username = username,
                    bio = bio,
                    gender = gender,
                    genderPreference = genderPreference
                )
            },
            onBack = { navigateTo(navController, DestinationScreen.Swipe.route) },
            onLogout = {
                vm.onLogout()
                navigateTo(navController, DestinationScreen.Login.route)
            }
        )

        BottomNavigationMenu(
            selectedItem = BottomNavigationItem.PROFILE,
            navController = navController
        )
    }
}


@Composable
fun ProfileContent(
    modifier: Modifier,
    vm: SWViewModel,
    name: String,
    username: String,
    bio: String,
    gender: Gender,
    genderPreference: Gender,
    onNameChanged: (String) -> Unit,
    onUsernameChanged: (String) -> Unit,
    onBioChanged: (String) -> Unit,
    onGenderChanged: (Gender) -> Unit,
    onGenderPreferenceChanged: (Gender) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val imageUrl = vm.userData.value?.imageUrl

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Back", modifier = Modifier.clickable { onBack.invoke() })
            Text(text = "Save", modifier = Modifier.clickable { onSave.invoke() })
        }

        CommonDivider()

        ProfileImage(imageUrl = imageUrl, vm = vm)

        CommonDivider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Name", modifier = Modifier.width(100.dp))
            TextField(
                value = name,
                onValueChange = onNameChanged,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                )
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Username", modifier = Modifier.width(100.dp))
            TextField(
                value = username,
                onValueChange = onUsernameChanged,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                )
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(text = "Bio", modifier = Modifier.width(100.dp))
            TextField(
                value = bio,
                onValueChange = onBioChanged,
                modifier = Modifier
                    .height(150.dp),
                singleLine = false,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                )
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "I am a:",
                modifier = Modifier
                    .width(100.dp)
                    .padding(8.dp)
            )
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = gender == Gender.MALE,
                        onClick = { onGenderChanged(Gender.MALE) }
                    )
                    Text(
                        text = "Man",
                        modifier = Modifier
                            .padding(4.dp)
                            .clickable { onGenderChanged(Gender.MALE) }
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = gender == Gender.FEMALE,
                        onClick = { onGenderChanged(Gender.FEMALE) }
                    )
                    Text(
                        text = "Woman",
                        modifier = Modifier
                            .padding(4.dp)
                            .clickable { onGenderChanged(Gender.FEMALE) }
                    )
                }
            }
        }
        CommonDivider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "Looking for:",
                modifier = Modifier
                    .width(100.dp)
                    .padding(8.dp)
            )
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = genderPreference == Gender.MALE,
                        onClick = { onGenderPreferenceChanged(Gender.MALE) }
                    )
                    Text(
                        text = "Men",
                        modifier = Modifier
                            .padding(4.dp)
                            .clickable { onGenderPreferenceChanged(Gender.MALE) }
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = genderPreference == Gender.FEMALE,
                        onClick = { onGenderPreferenceChanged(Gender.FEMALE) }
                    )
                    Text(
                        text = "Women",
                        modifier = Modifier
                            .padding(4.dp)
                            .clickable { onGenderPreferenceChanged(Gender.FEMALE) }
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = genderPreference == Gender.ANY,
                        onClick = { onGenderPreferenceChanged(Gender.ANY) }
                    )
                    Text(
                        text = "Any",
                        modifier = Modifier
                            .padding(4.dp)
                            .clickable { onGenderPreferenceChanged(Gender.ANY) }
                    )
                }
            }
        }
        CommonDivider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = "Logout", modifier = Modifier.clickable { onLogout.invoke() })
        }
    }
}

@Composable
fun ProfileImage(imageUrl: String?, vm: SWViewModel) {

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            vm.uploadProfileImage(it)
        }
    }

    Box(modifier = Modifier.height(IntrinsicSize.Min)) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .clickable {
                    launcher.launch("image/*")
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                shape = CircleShape,
                modifier = Modifier
                    .padding(8.dp)
                    .size(100.dp)
            ) {
                // CommonImage already handles loading/error states
                CommonImage(data = imageUrl)
            }
            Text(text = "Change profile picture")
        }

        // We don't need the isLoading check here anymore because the main
        // ProfileScreen composable now handles the progress spinner overlay
    }
}
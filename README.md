SwipeRightly Tinder Clone App 💖
Welcome to SwipeRightly, a modern dating app clone built with Jetpack Compose and Firebase. This application showcases a feature-rich, scalable, and modern Android development stack.

🎬 Screen Recording Demo
(https://drive.google.com/drive/folders/1F7GbHq0D2syjN9_HfdHwydgnSFLwjLqP?usp=sharing)

✨ Features
Authentication: Secure user sign-up and login using Firebase Authentication (Email & Password).
User Profiles: Users can create and update their profiles, including name, bio, gender, and profile picture.
Swiping Mechanism: A Tinder-like card-swiping interface to like or dislike other profiles.
Real-time Chat: Once two users match, they can chat in real-time.
Image Uploads: Users can upload their profile pictures to Firebase Storage.
Modern UI: A beautiful and responsive UI built entirely with Jetpack Compose.
Dependency Injection: Uses Hilt for managing dependencies.
📲 Download APK
You can try out the app by downloading the latest APK from the releases section.

⬇️ Download the latest APK(https://drive.google.com/drive/folders/1sZISTqCSjVeuxaR8ogxkBgKxYGhb3Nxr?usp=sharing)

Login Screen	Sign-Up Screen	Swipe Screen
Login UI	Sign-Up UI	Profile Cards for Swiping
Profile Screen	Chat List	Single Chat
User Profile Editing	List of Matches	Real-time Conversation

🛠 Tech Stack & Architecture
UI: 100% Kotlin with Jetpack Compose for the entire UI.
Architecture: Follows the MVVM (Model-View-ViewModel) architecture pattern.
Asynchronous Programming: Kotlin Coroutines for all asynchronous operations.
Dependency Injection: Hilt for dependency management.
Navigation: Jetpack Navigation Compose for navigating between screens.
Image Loading: Coil for efficient image loading and caching.
Backend: Firebase for a complete backend solution.
Firebase Integration
Firebase Authentication: For handling user registration and sign-in.
Cloud Firestore: As the primary database for storing user data, chats, and matches.
Firebase Storage: For storing user-uploaded profile pictures.
The Firestore database is structured with the following collections:

user: Stores all public user profile data.
chat: Contains documents representing a match between two users.
message: A sub-collection within each chat document to store all messages for that conversation.
🚀 How To Run
To get this project up and running on your local machine, follow these steps:

Clone the repository:
Bash

git clone https://(https://github.com/Snehil208001/SwipeRightly)
Firebase Setup:
Go to the Firebase Console and create a new project.
Add an Android app to your Firebase project with the package name com.example.swiperightly.
Follow the setup instructions to download the google-services.json file.
Place the downloaded google-services.json file in the SwipeRightly/app/ directory.
Enable Authentication (Email/Password), Firestore, and Storage in the Firebase console.
Open in Android Studio:
Open the cloned project in Android Studio.
Let Gradle sync and build the project.
Run the App:
Select an emulator or a physical device.
Click the 'Run' button.
🔮 Future Improvements
Here are some potential features and improvements for the future:

[ ] Social Login (Google, Facebook, etc.)
[ ] Push Notifications for new matches and messages
[ ] Advanced Profile Filters (age, location)
[ ] In-app purchases for premium features
[ ] Writing more unit and integration tests
🤝 Contribution
Contributions are welcome! If you'd like to contribute, please fork the repository and use a feature branch. Pull requests are warmly welcome.

Made with ❤️ and Kotlin.

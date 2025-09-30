# 🍌 Banana Project Mobile

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-blue.svg)](https://kotlinlang.org/)
[![Android](https://img.shields.io/badge/Android-API%2033+-green.svg)](https://developer.android.com/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

A cutting-edge Android application that transforms voice-inputted shopping lists into structured JSON data using advanced speech recognition and natural language processing. This project is developed for **academic and learning purposes** to sharpen skills in Android development, Kotlin programming, and AI-driven features.

## 📱 Features

- 🎤 **Voice Recognition**: Integrated Vosk library for accurate Spanish speech-to-text
- 🗣️ **Natural Language Parsing**: Custom NLP engine to extract products and quantities from spoken lists
- 📊 **JSON Output**: Clean, structured data output for easy integration
- 🎨 **Modern UI**: Sleek interface built with Jetpack Compose and Material 3
- 🏗️ **Robust Architecture**: MVVM pattern with Hilt dependency injection
- 🔒 **Privacy-Focused**: On-device processing with minimal permissions

## 🚀 Screenshots

![Final App Design](./screenshots/final_app_design.png)

## 🛠️ Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM with ViewModels
- **Dependency Injection**: Hilt
- **Speech Recognition**: Vosk (offline STT)
- **Serialization**: Kotlinx.serialization
- **Build Tool**: Gradle with Kotlin DSL

## 📋 Prerequisites

- Android Studio (Arctic Fox 2020.3.1 or later)
- Minimum SDK: API 21 (Android 5.0)
- Target SDK: API 33 (Android 13)
- Kotlin 1.9.0+

## 🏃‍♂️ Getting Started

1. **Clone the repository**:
   ```bash
   git clone https://github.com/yourusername/banana-project-mobile.git
   cd banana-project-mobile
   ```

2. **Open in Android Studio**:
   - Launch Android Studio
   - Select "Open an existing Android Studio project"
   - Navigate to the cloned directory

3. **Build and Run**:
   - Connect an Android device or start an emulator
   - Click "Run" (green play button) or use `Shift + F10`

## 📖 Usage

1. **Launch the App**: Open Banana Project Mobile on your device
2. **Grant Permissions**: Allow microphone access when prompted
3. **Speak Your List**: Tap the record button and say your shopping list in Spanish
   - Example: "Veinte manzanas, cinco litros de leche y treinta galletas de chocolate"
4. **View Results**: See the parsed JSON with products and quantities
5. **Test Features**: Use the built-in test screens to experiment with different inputs

## 🎓 Learning Objectives

This academic project provides hands-on experience with:

- **Android Development**: Modern app architecture, permissions, and lifecycle management
- **Kotlin Programming**: Advanced language features, coroutines, and DSLs
- **Speech Processing**: Integrating offline STT and building custom NLP parsers
- **UI/UX Design**: Creating intuitive interfaces with Compose
- **Dependency Injection**: Implementing clean architecture with Hilt
- **Version Control**: Best practices for Android projects with Git

## 🤝 Contributing

Contributions are welcome! This is a learning project, so feel free to:

- Fork the repository
- Create feature branches
- Submit pull requests with improvements
- Open issues for bugs or suggestions

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- [Vosk](https://alphacephei.com/vosk/) for the speech recognition library
- [Jetpack Compose](https://developer.android.com/jetpack/compose) for modern UI toolkit
- [Hilt](https://dagger.dev/hilt/) for dependency injection

---

*Built with ❤️ for learning and exploration in Android development*

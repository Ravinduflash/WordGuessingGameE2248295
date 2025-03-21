# Word Guessing Game

The **WordGuessingGameE2248295** repository contains the source code for a mobile word guessing game developed for the **ITE 2152 - Introduction to Mobile Application Development** course. This Android app, built with Kotlin, challenges users to guess a randomly fetched secret word with limited attempts and points.

## Table of Contents
- [Features](#features)
- [Installation](#installation)
- [Usage](#usage)
- [Contributing](#contributing)
- [License](#license)

## Features
- **Gameplay Mechanics**:
  - Users start with 100 points, deducting 10 points for each wrong guess.
  - A maximum of 10 attempts to guess the secret word.
  - Correct guesses display a success message, while wrong guesses show score deductions.
  
- **Onboarding**:
  - Name input screen with a welcome message personalized with the user's name.
  - Utilizes SharedPreferences to save the user's name.

- **Clue System**:
  - Letter count clue available for 5 points.
  - Letter occurrence clue available for 5 points.
  - Rhyme tip available after five incorrect attempts for 5 points.

- **Timer**:
  - Tracks the time taken for each guess.

- **Leaderboard**:
  - Integration with the Dreamlo API to display player scores.
  - Scores are added to the leaderboard after correct guesses.

- **User  Interface**:
  - Designed using ConstraintLayout for efficient layout.
  - Clear color scheme and intuitive button placement for usability.

- **Code Quality**:
  - Modular code structure with separate functions for API calls and UI updates.
  - Error handling for API failures.

- **Testing and Debugging**:
  - Manual testing across multiple devices.
  - Performance optimizations and use of Logcat for debugging.

## Installation
To get started with the Word Guessing Game, follow these steps:

1. Download the project zip file: [WordGuessingGameE2248295_Updated.zip](./WordGuessingGameE2248295_Updated.zip)
2. Extract the contents of the zip file.
3. Open the project in Android Studio or your preferred IDE.
4. Ensure you have the necessary SDK and dependencies installed.

## Usage
To play the game:

1. Run the app on an Android device or emulator.
2. Follow the onboarding instructions to input your name.
3. Start guessing the secret word by entering letters.
4. Use the clues wisely and try to guess the word before running out of attempts!

## Contributing
We welcome contributions! If you would like to contribute to the project, please follow these steps:

1. Fork the repository.
2. Create a new branch (`git checkout -b feature/YourFeature`).
3. Make your changes and commit them (`git commit -m 'Add some feature'`).
4. Push to the branch (`git push origin feature/YourFeature`).
5. Open a pull request.

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments
- Thank you to all the contributors and players who make this game enjoyable!

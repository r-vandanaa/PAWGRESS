# Digital Pet Evolution

A JavaFX application that creates an emotionally engaging experience where a virtual companion evolves based on the user's real-life habits.

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   ├── com/digitalpet/
│   │   │   ├── DigitalPetApplication.java    # Main application class
│   │   │   ├── model/                        # Data models and enums
│   │   │   ├── view/                         # UI components and FXML controllers
│   │   │   ├── viewmodel/                    # Presentation logic and data binding
│   │   │   └── service/                      # Business logic and services
│   │   └── module-info.java                 # Java module configuration
│   └── resources/
│       └── com/digitalpet/
│           ├── view/                         # FXML files
│           └── assets/                       # Images, sounds, etc.
└── test/
    └── java/
        └── com/digitalpet/                   # Unit and property-based tests
```

## Core Enums

- **EvolutionStage**: Defines the five pet evolution stages (Egg, Baby, Teen, Adult, Legendary)
- **PetMood**: Represents pet emotional states (Happy, Neutral, Sleepy, Worried, Celebrating)
- **AchievementCategory**: Categories for achievements (Consistency, Milestone, Evolution)

## Build and Run

### Prerequisites
- Java 17 or higher
- Maven 3.6 or higher

### Commands
```bash
# Compile the project
mvn clean compile

# Run tests (including property-based tests with jqwik)
mvn test

# Run the application
mvn javafx:run
```

## Testing Framework

The project uses **jqwik** for property-based testing alongside JUnit 5 for unit tests. Property-based tests validate universal correctness properties across all possible inputs.

## Architecture

The application follows the MVVM (Model-View-ViewModel) pattern with:
- **Models**: Core data structures and business entities
- **Views**: JavaFX UI components and FXML files
- **ViewModels**: Presentation logic with property binding
- **Services**: Business logic and system services

## Next Steps

This is the foundational setup. The next tasks will implement:
1. Core data models with JavaFX properties
2. XP and evolution systems
3. Mood and environment systems
4. UI components and animations
5. Achievement and statistics systems
# Design Document: Digital Pet Evolution

## Overview

The Digital Pet Evolution application is a JavaFX-based virtual companion that creates an emotionally engaging experience where users track daily habits through interaction with a responsive digital pet. The system employs a calm, motivational design philosophy that encourages healthy lifestyle choices without judgment, making it suitable for college students and young professionals.

The application follows a Model-View-ViewModel (MVVM) architecture pattern, leveraging JavaFX's property binding system for reactive UI updates and Timeline-based animations for smooth visual feedback. The core design emphasizes emotional connection through consistent visual evolution, mood-responsive environments, and progressive reward systems.

## Architecture

### System Architecture Pattern

The application implements a layered MVVM architecture with clear separation of concerns:

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                       │
│  ┌─────────────────┐  ┌─────────────────┐  ┌──────────────┐ │
│  │   Main Pet      │  │  Habit Input    │  │  Statistics  │ │
│  │     View        │  │     View        │  │     View     │ │
│  └─────────────────┘  └─────────────────┘  └──────────────┘ │
└─────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────┐
│                   ViewModel Layer                           │
│  ┌─────────────────┐  ┌─────────────────┐  ┌──────────────┐ │
│  │   Pet View      │  │  Habit View     │  │  Stats View  │ │
│  │    Model        │  │    Model        │  │    Model     │ │
│  └─────────────────┘  └─────────────────┘  └──────────────┘ │
└─────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────┐
│                     Service Layer                           │
│  ┌─────────────────┐  ┌─────────────────┐  ┌──────────────┐ │
│  │  Animation      │  │   Mood System   │  │  Achievement │ │
│  │   Engine        │  │    Service      │  │   Service    │ │
│  └─────────────────┘  └─────────────────┘  └──────────────┘ │
└─────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────┐
│                      Data Layer                             │
│  ┌─────────────────┐  ┌─────────────────┐  ┌──────────────┐ │
│  │   Digital Pet   │  │  Habit Tracker  │  │  Local Data  │ │
│  │     Model       │  │     Model       │  │   Storage    │ │
│  └─────────────────┘  └─────────────────┘  └──────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

### Core Design Principles

1. **Reactive Data Flow**: JavaFX properties enable automatic UI synchronization when model data changes
2. **Emotional Responsiveness**: All visual elements respond to user habits through mood-based state changes
3. **Progressive Disclosure**: Features unlock gradually as the pet evolves, maintaining long-term engagement
4. **Accessibility First**: WCAG 2.1 AA compliance ensures inclusive design for all users
5. **Performance Optimization**: 60fps animations with efficient memory usage through shadow field patterns

## Components and Interfaces

### Digital Pet Component

**Core Responsibilities:**
- Visual representation and animation state management
- Evolution stage progression and visual consistency
- Mood-based reaction system
- Interactive response handling

**Key Properties:**
```java
public class DigitalPet {
    private StringProperty name;
    private ObjectProperty<EvolutionStage> currentStage;
    private ObjectProperty<PetMood> currentMood;
    private IntegerProperty experiencePoints;
    private IntegerProperty energyLevel;
    private ObjectProperty<LocalDateTime> lastInteraction;
}
```

**Evolution Stage Definitions:**
- **Egg Stage**: Simple oval shape, soft pulsing animation, minimal features
- **Baby Stage**: Small rounded body, large eyes, basic accessories (small bow, pacifier)
- **Teen Stage**: Medium size, school accessories (glasses, books), more expressive animations
- **Adult Stage**: Full size, professional accessories (briefcase, organized appearance)
- **Legendary Stage**: Magical aura effects, floating particles, enhanced visual presence

### Habit Tracking System

**Data Model:**
```java
public class DailyHabits {
    private IntegerProperty studyHours;      // Range: 0-16
    private DoubleProperty waterIntake;      // Range: 0-5.0 liters
    private IntegerProperty stepsTaken;      // Range: 0-50000
    private DoubleProperty sleepHours;       // Range: 0-24.0
    private DoubleProperty moneySpent;       // Range: 0-1000.0
    private IntegerProperty goalsCompleted;  // Range: 0-10
    private ObjectProperty<LocalDate> date;
}
```

**Validation Rules:**
- Input sanitization prevents negative values and unrealistic ranges
- Real-time validation feedback using JavaFX binding expressions
- Automatic data persistence on successful validation

### Mood System Architecture

**Mood Calculation Algorithm:**
```java
public enum PetMood {
    HAPPY(0.8, "😊", Color.GOLD),
    NEUTRAL(0.5, "😐", Color.LIGHTBLUE), 
    SLEEPY(0.3, "😴", Color.LAVENDER),
    WORRIED(0.2, "😟", Color.LIGHTCORAL),
    CELEBRATING(1.0, "🎉", Color.LIME);
    
    private final double energyMultiplier;
    private final String emoji;
    private final Color environmentTint;
}
```

**Mood Determination Logic:**
1. Calculate habit completion percentage for last 3 days
2. Apply consistency bonus for streak maintenance
3. Factor in achievement unlocks for celebration triggers
4. Determine sleep-based mood adjustments
5. Apply weighted average with previous mood for smooth transitions

### Environment System

**Dynamic Background Components:**
- **Base Environment**: Scalable vector graphics that adapt to screen size
- **Mood Overlays**: Color tinting and particle effects based on current mood
- **Evolution Decorations**: Stage-specific furniture and decorative elements
- **Interactive Elements**: Clickable objects that trigger pet reactions

**Environment Evolution Mapping:**
```java
public class EnvironmentTheme {
    private Map<EvolutionStage, EnvironmentConfig> themeConfigs = Map.of(
        EGG, new EnvironmentConfig("soft-gradient.svg", List.of()),
        BABY, new EnvironmentConfig("nursery.svg", List.of("plant-small.svg")),
        TEEN, new EnvironmentConfig("study-room.svg", List.of("desk.svg", "books.svg")),
        ADULT, new EnvironmentConfig("office.svg", List.of("bookshelf.svg", "window.svg")),
        LEGENDARY, new EnvironmentConfig("magical.svg", List.of("particles.svg", "aura.svg"))
    );
}
```

### Animation Engine

**Animation Framework:**
- **Timeline-based Animations**: JavaFX Timeline with KeyFrame sequences for complex animations
- **Transition Animations**: Built-in JavaFX transitions for smooth property changes
- **Sprite Animation**: Custom sprite sheet handling for pet character animations
- **Particle Systems**: Custom particle effects for mood and celebration animations

**Performance Optimization:**
- Animation pooling to prevent memory leaks during long sessions
- Reduced motion support for accessibility compliance
- Frame rate limiting to maintain 60fps on target hardware
- Lazy loading of animation resources based on current evolution stage

## Data Models

### Core Data Structures

**Pet Evolution Data:**
```java
public class PetEvolutionData {
    private final Map<EvolutionStage, Integer> xpThresholds = Map.of(
        BABY, 100,
        TEEN, 300, 
        ADULT, 600,
        LEGENDARY, 1000
    );
    
    private final Map<EvolutionStage, Duration> stageMinimumTime = Map.of(
        EGG, Duration.ofDays(1),
        BABY, Duration.ofDays(3),
        TEEN, Duration.ofDays(7),
        ADULT, Duration.ofDays(14)
    );
}
```

**Achievement System Data:**
```java
public class Achievement {
    private String id;
    private String title;
    private String description;
    private AchievementCategory category;
    private Predicate<UserProgress> unlockCondition;
    private boolean isUnlocked;
    private LocalDateTime unlockedDate;
}

public enum AchievementCategory {
    CONSISTENCY("Daily habit streaks"),
    MILESTONE("Total progress markers"), 
    EVOLUTION("Pet development stages");
}
```

**Local Storage Schema:**
- **SQLite Database**: Embedded database for habit history and achievement tracking
- **JSON Configuration**: User preferences and pet customization settings
- **Binary Assets**: Cached animation frames and sprite sheets for offline performance

### Data Persistence Strategy

**Automatic Backup System:**
- Daily incremental backups of habit data and pet progress
- Export functionality for user-initiated full data backup
- Import capability for data restoration across devices
- Data integrity validation on application startup

**Offline-First Design:**
- All core functionality available without network connectivity
- Local data storage with optional cloud synchronization
- Graceful degradation when storage limits are reached
- Data compression for efficient storage utilization

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system—essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

The following properties define the correctness requirements for the Digital Pet Evolution application. Each property represents a universal rule that must hold across all possible inputs and system states, enabling comprehensive validation through property-based testing.

### Property 1: Pet Visual Consistency Across Evolution
*For any* Digital Pet at any evolution stage, the core visual elements (large expressive eyes, rounded shapes, soft outlines, transparent backgrounds) shall remain consistent while only size, accessories, and aura effects change incrementally according to predefined specifications.
**Validates: Requirements 1.1, 1.4, 2.4, 10.4**

### Property 2: Pet Responsiveness to Habit Changes  
*For any* Daily Habits data showing improvement or decline patterns, the Digital Pet shall display appropriate mood-based reactions (positive for improvements, concerned for declines) within the specified time limits while maintaining the supportive aesthetic.
**Validates: Requirements 1.2, 1.3**

### Property 3: Interactive Response Consistency
*For any* user interaction with the Digital Pet (click or tap), the pet shall respond with a randomized animation from predefined sets within 0.5 seconds, ensuring consistent responsiveness across all evolution stages and mood states.
**Validates: Requirements 1.5**

### Property 4: XP Calculation Transparency
*For any* combination of Daily Habits completion percentage and Consistency Score, the XP System shall calculate experience points using the defined transparent algorithm, producing consistent and predictable results for identical inputs.
**Validates: Requirements 2.1**

### Property 5: Evolution Progression Integrity
*For any* Digital Pet with sufficient XP for evolution, the pet shall progress through exactly five stages (Egg→Baby→Teen→Adult→Legendary) in order with predefined thresholds, never regressing to previous stages, and triggering celebration animations.
**Validates: Requirements 2.2, 2.3**

### Property 6: Habit Input Validation Completeness
*For any* input to the Habit Tracker, the system shall validate exactly six categories (study hours, water intake, steps, sleep hours, money spent, goals completed) within specified ranges, rejecting invalid inputs with clear error messaging and accepting valid inputs with confirmation feedback.
**Validates: Requirements 3.1, 3.4, 3.5**

### Property 7: UI Responsiveness and Feedback
*For any* Daily Habits submission, the UI Manager shall display sliders with live value updates, trigger pet reactions matching current mood, update XP within 1 second, and persist data across application sessions.
**Validates: Requirements 3.2, 3.3, 3.5**

### Property 8: Mood System State Management
*For any* recent Daily Habits pattern, the Mood System shall calculate pet mood using exactly five predefined states (Happy, Sleepy, Celebrating, Worried, Neutral) based on habit patterns, with each mood triggering appropriate environment effects.
**Validates: Requirements 4.1, 4.2, 4.3, 4.4, 4.5, 4.6**

### Property 9: Environment Evolution Synchronization
*For any* Digital Pet evolution stage change, the Environment shall evolve through exactly five predefined visual themes with smooth transition animations, matching the pet's current stage with appropriate decorative elements and lighting.
**Validates: Requirements 5.1**

### Property 10: Navigation Accessibility and Performance
*For any* screen navigation, the UI Manager shall provide access to all primary sections within maximum 2 taps, use smooth transitions under 300ms duration, and maintain persistent bottom navigation throughout the application.
**Validates: Requirements 6.3, 6.4**

### Property 11: Accessibility Compliance Comprehensive
*For any* interactive element in the application, the UI Manager shall ensure WCAG 2.1 AA compliance with minimum 44px touch targets, 4.5:1 color contrast ratios, minimum 14px font sizes, and alternative text for all visual elements.
**Validates: Requirements 6.5, 10.2, 10.5**

### Property 12: Visual Design Consistency
*For any* UI element throughout the application, the UI Manager shall use consistent design patterns including rounded corners (minimum 8px), soft drop shadows, 16px grid spacing, and the defined color palette (#A8DADC, #B8E6B8, #F1FAEE) while avoiding harsh colors and sharp edges.
**Validates: Requirements 6.1, 7.4, 10.1, 10.3**

### Property 13: Statistics Visualization Completeness
*For any* progress data display, the UI Manager shall present XP progression as line charts, Daily Habits as color-coded bar charts with legends, and mood history as timelines with visual indicators, all supporting multiple time ranges (7, 30, 90 days) with persistent preferences.
**Validates: Requirements 7.1, 7.2, 7.3, 7.5**

### Property 14: Achievement System Functionality
*For any* user progress milestone, the Achievement System shall track achievements across three categories (consistency, milestone, evolution), display progress indicators for locked achievements, and trigger celebration effects within 1 second when achievements are unlocked.
**Validates: Requirements 8.1, 8.2, 8.4, 8.5**

### Property 15: Animation Performance and Accessibility
*For any* animation in the application, the Animation Engine shall maintain 60fps performance, provide smooth transitions with easing functions, implement responsive feedback within 100ms, support reduced motion preferences, and ensure idle animations cycle appropriately based on mood and evolution stage.
**Validates: Requirements 9.1, 9.2, 9.3, 9.5**

### Property 16: Achievement Celebration Consistency
*For any* achievement unlock event, the Animation Engine shall trigger confetti burst effects and celebratory Digital Pet animations lasting 2-3 seconds, providing consistent celebration experiences across all achievement types.
**Validates: Requirements 9.4**

## Error Handling

### Input Validation Strategy

**Habit Input Validation:**
- Range validation with immediate visual feedback using JavaFX binding expressions
- Type safety through strongly-typed property wrappers
- Graceful degradation when invalid data is detected
- User-friendly error messages with suggested corrections

**Data Persistence Error Handling:**
- Automatic retry mechanisms for transient storage failures
- Data integrity validation on application startup
- Backup restoration capabilities when corruption is detected
- User notification system for critical data issues

### Animation and Performance Error Handling

**Animation Failure Recovery:**
- Fallback to static images when animation resources fail to load
- Performance monitoring with automatic quality reduction on low-end devices
- Memory management with automatic cleanup of unused animation resources
- Graceful degradation of particle effects when performance thresholds are exceeded

**UI Responsiveness Safeguards:**
- Timeout mechanisms for long-running operations
- Loading indicators for operations exceeding 100ms
- Background processing for data-intensive calculations
- User feedback for operations that cannot complete within expected timeframes

## Testing Strategy

### Dual Testing Approach

The application employs a comprehensive testing strategy combining unit tests for specific scenarios and property-based tests for universal correctness validation. This dual approach ensures both concrete functionality and general system behavior are thoroughly validated.

**Unit Testing Focus:**
- Specific examples demonstrating correct behavior for each feature
- Edge cases and boundary conditions (empty inputs, maximum values, state transitions)
- Integration points between components (pet-environment synchronization, mood-animation coordination)
- Error conditions and recovery scenarios
- User interface interaction flows and accessibility features

**Property-Based Testing Focus:**
- Universal properties that hold across all valid inputs and system states
- Comprehensive input coverage through randomized test generation
- Correctness validation for complex state interactions
- Performance characteristics under varied load conditions
- Data consistency across application sessions and device restarts

### Property-Based Testing Configuration

**Testing Framework:** The application uses **QuickCheck for Java** (or **jqwik**) as the property-based testing library, providing robust random input generation and shrinking capabilities for effective counterexample identification.

**Test Configuration Requirements:**
- Minimum 100 iterations per property test to ensure comprehensive coverage
- Each property test references its corresponding design document property
- Tag format: **Feature: digital-pet-evolution, Property {number}: {property_text}**
- Custom generators for domain-specific data types (EvolutionStage, PetMood, DailyHabits)
- Shrinking strategies optimized for UI state and animation testing

**Test Data Generation:**
- Smart generators that constrain inputs to realistic ranges and valid combinations
- Mood-based test scenarios that reflect actual user behavior patterns
- Evolution stage progression testing with realistic XP accumulation patterns
- Accessibility testing with varied screen sizes and user preference combinations
- Performance testing with realistic animation loads and user interaction frequencies

### Integration Testing Strategy

**Component Integration:**
- Pet-Environment synchronization testing across all evolution and mood combinations
- Animation-Performance integration testing under various system load conditions
- Data persistence integration testing with simulated application lifecycle events
- Accessibility integration testing with assistive technology simulation

**End-to-End Validation:**
- Complete user journey testing from initial pet creation through legendary evolution
- Cross-session data persistence validation with simulated device restarts
- Performance validation under extended usage scenarios
- Accessibility validation with real assistive technology tools
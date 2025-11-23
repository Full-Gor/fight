# Shadow Fight 2 Clone

A complete recreation of the popular Shadow Fight 2 fighting game for Android, built with **LibGDX** and **Kotlin**.

![Shadow Fight 2 Clone](https://img.shields.io/badge/Platform-Android%20%7C%20Desktop-green)
![LibGDX](https://img.shields.io/badge/Framework-LibGDX%201.12-blue)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple)

## Features

### Combat System
- **Fluid Combat Mechanics**: Realistic martial arts combat with punches, kicks, and special moves
- **Combo System**: Chain attacks together for devastating combos
- **Blocking & Countering**: Defensive mechanics with timing-based counters
- **Weapon Combat**: Various weapons with unique movesets and damage multipliers
- **Magic Abilities**: Special powers including fireballs, lightning, and shadow attacks

### Characters & Animation
- **Silhouette Art Style**: Authentic Shadow Fight visual style with procedurally generated animations
- **Smooth Animations**: Idle, walk, jump, attack, and hit stun animations
- **Dynamic Hit Effects**: Particles, sparks, and screen shake on impact

### Game Modes
- **Story Mode**: 7 Acts with unique bosses (Hermit, Butcher, Wasp, Widow, Shogun, Titan, Gates of Shadows)
- **Tournament**: Fight through brackets for rewards
- **Survival**: Endless wave-based combat

### Progression System
- **Level Up**: Gain experience and unlock new content
- **Equipment Shop**: Buy and equip weapons, armor, helms, ranged weapons, and magic
- **Currency System**: Earn coins and gems through victories

### AI System
- **Adaptive AI**: Enemies adapt their strategy based on difficulty
- **Multiple Strategies**: Aggressive, defensive, combo-focused, and retreat behaviors
- **Boss AI**: Unique challenging behaviors for boss fights

## Project Structure

```
app/
├── src/main/
│   ├── java/com/shadowfight/game/
│   │   ├── core/           # Main game class, audio, effects
│   │   ├── entities/       # Fighter, animations
│   │   ├── combat/         # Attacks, equipment, weapons
│   │   ├── ai/             # Enemy AI system
│   │   ├── screens/        # Game screens (menu, fight, shop)
│   │   ├── input/          # Touch controls
│   │   └── utils/          # Configuration, helpers
│   ├── assets/             # Game assets
│   └── res/                # Android resources
desktop/
└── src/main/kotlin/        # Desktop launcher for testing
```

## Controls

### Mobile (Touch)
- **Virtual Joystick**: Move left/right, crouch (down)
- **Jump Button**: Green button
- **Punch Button**: Red button
- **Kick Button**: Blue button
- **Block Button**: Yellow button (hold)

### Desktop (Keyboard)
- **A/D or Arrow Keys**: Move left/right
- **W or Up**: Jump
- **S or Down**: Crouch
- **J**: Punch
- **K**: Kick
- **L**: Block (hold)
- **ESC/P**: Pause

## Building

### Prerequisites
- Android Studio Arctic Fox or newer
- JDK 17
- Android SDK 34

### Build APK
```bash
./gradlew :app:assembleDebug
```

### Run Desktop Version
```bash
./gradlew :desktop:run
```

### Build Release APK
```bash
./gradlew :app:assembleRelease
```

## Technical Details

### Framework
- **LibGDX 1.12.1**: Cross-platform game framework
- **Box2D**: Physics engine for collision detection
- **FreeType**: Font rendering

### Architecture
- **Screen-based navigation**: Each game state is a separate screen
- **Component-based entities**: Fighters with modular components
- **State machine AI**: Behavior-based enemy AI
- **Procedural animations**: Runtime-generated silhouette animations

### Performance
- **60 FPS target**: Optimized rendering and update loops
- **Efficient collision**: Axis-aligned bounding boxes with spatial partitioning
- **Memory management**: Proper disposal of resources

## Game Balance

### Difficulty Progression
- Act 1-2: Easy (AI difficulty 0.4-0.5)
- Act 3-4: Medium (AI difficulty 0.5-0.6)
- Act 5-6: Hard (AI difficulty 0.7-0.8)
- Act 7: Expert (AI difficulty 0.9+)

### Equipment Scaling
- Weapons: 1.0x - 3.5x damage multiplier
- Armor: 0 - 35 defense bonus
- Required levels for progression

## Credits

- Inspired by **Shadow Fight 2** by Nekki
- Built with **LibGDX** framework
- Developed as an educational clone project

## License

This project is for educational purposes only. Shadow Fight is a trademark of Nekki.

---

**Note**: This is a fan-made clone for learning purposes. All rights to the original Shadow Fight 2 belong to Nekki.

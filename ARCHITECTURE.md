# Project Architecture Restructure

## Tổng quan
Dự án đã được tái cấu trúc hoàn toàn để dễ quản lý, mở rộng và bảo trì. Kiến trúc mới áp dụng các design patterns hiện đại và tách biệt rõ ràng các thành phần.

## Cấu trúc Package

### 1. `entities/` - Game Objects
Chứa tất cả các thực thể trong game:
- **GameObject.java** - Base class cho tất cả game objects
- **Player.java** - Quản lý player với health, movement, invincibility
- **Bullet.java** - Đạn với damage và speed
- **Enemy.java** - Base class cho enemies
- **EnemyBasic.java, EnemyMedium.java, EnemyHeavy.java** - Các loại enemy khác nhau
- **Explosion.java** - Hiệu ứng nổ
- **PowerUp.java** - Các power-up với enum types

### 2. `engine/` - Game Engine Core
Trái tim của game engine:
- **GameEngine.java** - Main engine điều phối tất cả systems
- **GameState.java** - Quản lý trạng thái game (score, level, power-ups)
- **BackgroundRenderer.java** - Xử lý scrolling background

### 3. `managers/` - System Managers
Các manager chuyên biệt:
- **InputManager.java** - Xử lý input từ touch events
- **CollisionManager.java** - Phát hiện va chạm giữa các objects
- **SoundManager.java** - Quản lý sound effects và background music
- **ResourceManager.java** - Load và scale bitmaps
- **EnemyManager.java** - Spawn và quản lý enemies
- **PowerUpManager.java** - Spawn và quản lý power-ups

### 4. `ui/` - User Interface (sẽ mở rộng)
Dành cho các components UI phức tạp

### 5. `utils/` - Utilities (sẽ mở rộng)
Các utility classes và helper functions

## Ưu điểm của kiến trúc mới

### 1. **Separation of Concerns**
- Mỗi class có trách nhiệm cụ thể và rõ ràng
- GameView chỉ lo về rendering và input
- GameEngine điều phối các systems
- Managers xử lý logic riêng biệt

### 2. **Inheritance Hierarchy**
- GameObject làm base class cho tất cả entities
- Enemy hierarchy cho các loại enemy khác nhau
- Polymorphism cho collision detection và rendering

### 3. **Manager Pattern**
- Tách biệt logic thành các managers chuyên biệt
- Dễ thêm features mới (VD: ParticleManager, UIManager)
- Dễ test và debug từng component

### 4. **Resource Management**
- Centralized resource loading và scaling
- Tránh memory leaks
- Optimized bitmap handling

### 5. **Game State Management**
- Centralized game state với power-ups, scoring
- Level progression với speed multipliers
- Easy save/load implementation later

## Cách sử dụng

### Để chạy game với architecture mới:
```java
// Sử dụng NewGameView thay vì GameView cũ
NewGameView gameView = new NewGameView(context);
setContentView(gameView);
gameView.startGame();
```

### Để thêm enemy type mới:
1. Tạo class extends Enemy
2. Set properties trong constructor
3. Override update() nếu cần special behavior
4. Thêm vào EnemyManager.spawnEnemy()

### Để thêm power-up mới:
1. Thêm type vào PowerUp.PowerUpType enum
2. Handle trong GameState.applyPowerUp()
3. Thêm bitmap vào ResourceManager

### Để thêm sound effect mới:
1. Thêm sound ID vào SoundManager
2. Load trong initialize()
3. Tạo public method để play

## Files cần update tiếp theo

1. **MainActivity.java** - Update để sử dụng NewGameView
2. **AndroidManifest.xml** - Thêm NewGameActivity
3. **Bitmap resources** - Integrate với ResourceManager
4. **Sound resources** - Integrate với SoundManager

## Migration từ code cũ

- GameView cũ vẫn hoạt động bình thường
- NewGameView sử dụng architecture mới
- Có thể chuyển dần từng feature
- Hoặc switch hoàn toàn sang NewGameView

## Performance Benefits

1. **Better object pooling** - Có thể implement object pools cho bullets, explosions
2. **Optimized collision detection** - Spatial partitioning có thể thêm later
3. **Resource caching** - Centralized resource management
4. **Frame rate control** - Built-in FPS limiting

Kiến trúc mới này sẽ làm cho việc thêm features mới dễ dàng hơn rất nhiều!
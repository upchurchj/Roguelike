
import random
from kivy.app import App
from kivy.uix.boxlayout import BoxLayout
from kivy.uix.label import Label
from kivy.core.window import Window
from kivy.clock import Clock

# Game constants
MAP_WIDTH = 40
MAP_HEIGHT = 20
ROOM_COUNT = 5
ENEMY_COUNT = 3

class Room:
    def __init__(self, x, y, w, h):
        self.x, self.y, self.w, self.h = x, y, w, h
        self.center_x = x + w // 2
        self.center_y = y + h // 2

class Entity:
    def __init__(self, x, y, char, hp=10):
        self.x, self.y = x, y
        self.char = char
        self.hp = hp

class RoguelikeGame:
    def __init__(self):
        self.map = [['.' for _ in range(MAP_WIDTH)] for _ in range(MAP_HEIGHT)]
        self.rooms = []
        self.player = None
        self.enemies = []
        self.items = []
        self.gold = 0
        self.message = "Welcome to Roguelike!"
        self.generate_dungeon()

    def generate_dungeon(self):
        """Procedural dungeon generation with rooms and corridors."""
        for _ in range(ROOM_COUNT):
            w = random.randint(5, 12)
            h = random.randint(4, 8)
            x = random.randint(1, MAP_WIDTH - w - 1)
            y = random.randint(1, MAP_HEIGHT - h - 1)
            room = Room(x, y, w, h)
            self.rooms.append(room)
            self._carve_room(room)

        # Connect rooms with corridors
        for i in range(len(self.rooms) - 1):
            r1, r2 = self.rooms[i], self.rooms[i + 1]
            self._carve_corridor(r1.center_x, r1.center_y, r2.center_x, r2.center_y)

        # Place player
        self.player = Entity(self.rooms[0].center_x, self.rooms[0].center_y, '@', hp=20)

        # Place enemies
        for _ in range(ENEMY_COUNT):
            room = random.choice(self.rooms[1:])
            enemy = Entity(room.center_x, room.center_y, 'E', hp=5)
            self.enemies.append(enemy)

        # Place items
        for room in self.rooms[1:]:
            if random.random() < 0.5:
                self.items.append((room.center_x, room.center_y, 'P'))  # Potion

    def _carve_room(self, room):
        for y in range(room.y, room.y + room.h):
            for x in range(room.x, room.x + room.w):
                self.map[y][x] = ' '

    def _carve_corridor(self, x1, y1, x2, y2):
        for x in range(min(x1, x2), max(x1, x2) + 1):
            if 0 <= y1 < MAP_HEIGHT and 0 <= x < MAP_WIDTH:
                self.map[y1][x] = ' '
        for y in range(min(y1, y2), max(y1, y2) + 1):
            if 0 <= y < MAP_HEIGHT and 0 <= x2 < MAP_WIDTH:
                self.map[y][x2] = ' '

    def render(self):
        """Render the game state as a string."""
        display = [row[:] for row in self.map]
        display[self.player.y][self.player.x] = self.player.char
        for enemy in self.enemies:
            if 0 <= enemy.y < MAP_HEIGHT and 0 <= enemy.x < MAP_WIDTH:
                display[enemy.y][enemy.x] = enemy.char
        for x, y, char in self.items:
            if 0 <= y < MAP_HEIGHT and 0 <= x < MAP_WIDTH and display[y][x] == ' ':
                display[y][x] = char
        return '\n'.join(''.join(row) for row in display)

    def move_player(self, dx, dy):
        """Move player and handle collisions."""
        nx, ny = self.player.x + dx, self.player.y + dy
        if 0 <= nx < MAP_WIDTH and 0 <= ny < MAP_HEIGHT and self.map[ny][nx] != '#':
            self.player.x, self.player.y = nx, ny
            self._check_item_pickup()
            self._move_enemies()
            self._check_enemy_collision()

    def _check_item_pickup(self):
        """Pickup items on the same tile."""
        self.items = [(x, y, char) for x, y, char in self.items if not (x == self.player.x and y == self.player.y)]

    def _move_enemies(self):
        """Simple chase AI: move towards player."""
        for enemy in self.enemies[:]:
            if enemy.hp <= 0:
                self.enemies.remove(enemy)
                continue
            dx = 1 if enemy.x < self.player.x else (-1 if enemy.x > self.player.x else 0)
            dy = 1 if enemy.y < self.player.y else (-1 if enemy.y > self.player.y else 0)
            enemy.x += dx
            enemy.y += dy

    def _check_enemy_collision(self):
        """Combat: player vs enemies."""
        for enemy in self.enemies:
            if enemy.x == self.player.x and enemy.y == self.player.y:
                enemy.hp -= 5
                self.message = f"Hit enemy! Enemy HP: {enemy.hp}"

class RoguelikeApp(App):
    def build(self):
        Window.size = (480, 800)
        self.game = RoguelikeGame()
        self.layout = BoxLayout(orientation='vertical')
        self.game_label = Label(text=self.game.render(), font_size='8sp', markup=False)
        self.info_label = Label(text=f"HP: {self.game.player.hp} | Gold: {self.game.gold}\n{self.game.message}", height=100, size_hint_y=0.2)
        self.layout.add_widget(self.game_label)
        self.layout.add_widget(self.info_label)
        self._keyboard = Window.request_keyboard(self._keyboard_closed, self.layout)
        self._keyboard.bind(on_key_down=self._on_keyboard_down)
        return self.layout

    def _keyboard_closed(self):
        self._keyboard.unbind(on_key_down=self._on_keyboard_down)
        self._keyboard = None

    def _on_keyboard_down(self, keyboard, keycode, text, modifiers):
        key = keycode[1]
        if key == 'up' or key == 'w':
            self.game.move_player(0, -1)
        elif key == 'down' or key == 's':
            self.game.move_player(0, 1)
        elif key == 'left' or key == 'a':
            self.game.move_player(-1, 0)
        elif key == 'right' or key == 'd':
            self.game.move_player(1, 0)
        elif key == 'q':
            return False
        self._update_display()
        return True

    def _update_display(self):
        self.game_label.text = self.game.render()
        self.info_label.text = f"HP: {self.game.player.hp} | Gold: {self.game.gold}\n{self.game.message}"

if __name__ == '__main__':
    RoguelikeApp().run()


package gui.game;

import javafx.scene.input.KeyCode;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class InputController {

    private final Set<KeyCode> pressedKeys = new HashSet<>();

    public void press(KeyCode key) {
        pressedKeys.add(key);
    }

    public void release(KeyCode key) {
        pressedKeys.remove(key);
    }

    public boolean isPressed(KeyCode key) {
        return pressedKeys.contains(key);
    }

    public Set<KeyCode> getPressedKeys() {
        return Collections.unmodifiableSet(pressedKeys);
    }

    public void clear() {
        pressedKeys.clear();
    }
}
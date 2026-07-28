package fr.quentincillierre.hangman.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javafx.scene.image.Image;

/**
 * Single place responsible for turning a classpath path (as built by
 * CharacterManager) into a javafx Image. Every controller should load
 * pictures through this class instead of calling
 * {@code new Image(getClass().getResourceAsStream(...))} directly - that
 * way there is exactly one spot to touch if the loading strategy ever needs
 * to change (e.g. adding disk-based characters, error/placeholder images,
 * pre-loading, etc.), and repeated lookups of the same picture are cheap.
 */
public final class ImageLoader {

    private static final Map<String, Image> CACHE = new ConcurrentHashMap<>();

    private ImageLoader() {
    }

    /**
     * Loads (and caches) the image at the given classpath path, e.g.
     * "/pictures/character1_boy1/3-hangman.png".
     *
     * @throws IllegalStateException if the resource cannot be found, so that
     *                                a missing picture fails fast and loudly
     *                                instead of silently showing a blank
     *                                ImageView.
     */
    public static Image load(String classpathImagePath) {
        return CACHE.computeIfAbsent(classpathImagePath, ImageLoader::readImage);
    }

    private static Image readImage(String classpathImagePath) {
        var resourceStream = ImageLoader.class.getResourceAsStream(classpathImagePath);
        if (resourceStream == null) {
            throw new IllegalStateException("Missing image resource on classpath: " + classpathImagePath);
        }
        return new Image(resourceStream);
    }
}

package fr.quentincillierre.hangman.util;

import java.net.URL;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class SoundManager {

    private static SoundManager instance;

    private MediaPlayer bgMusicPlayer;
    private AudioClip correctClip;
    private AudioClip failedClip;
    private AudioClip clickClip;

    private boolean muted = false;
    private boolean initialized = false;

    private SoundManager() {
        initSounds();
    }

    public static synchronized SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    private void initSounds() {
        try {
            // Background music
            URL bgUrl = getClass().getResource("/sound_effects/background_music.mp3");
            if (bgUrl != null) {
                Media bgMedia = new Media(bgUrl.toExternalForm());
                bgMusicPlayer = new MediaPlayer(bgMedia);
                bgMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                bgMusicPlayer.setVolume(0.35); // Soft background music volume
            }

            // Click SFX
            URL clickUrl = getClass().getResource("/sound_effects/clicked.mp3");
            if (clickUrl != null) {
                clickClip = new AudioClip(clickUrl.toExternalForm());
                clickClip.setVolume(0.6);
            }

            // Correct SFX
            URL correctUrl = getClass().getResource("/sound_effects/correct.mp3");
            if (correctUrl != null) {
                correctClip = new AudioClip(correctUrl.toExternalForm());
                correctClip.setVolume(0.7);
            }

            // Failed SFX
            URL failedUrl = getClass().getResource("/sound_effects/failed.mp3");
            if (failedUrl != null) {
                failedClip = new AudioClip(failedUrl.toExternalForm());
                failedClip.setVolume(0.7);
            }

            initialized = true;
        } catch (Exception e) {
            System.err.println("Audio initialization warning: " + e.getMessage());
        }
    }

    public void playBackgroundMusic() {
        if (!initialized || muted || bgMusicPlayer == null) return;
        try {
            if (bgMusicPlayer.getStatus() != MediaPlayer.Status.PLAYING) {
                bgMusicPlayer.play();
            }
        } catch (Exception e) {
            // Ignore audio device errors
        }
    }

    public void stopBackgroundMusic() {
        if (bgMusicPlayer != null) {
            try {
                bgMusicPlayer.stop();
            } catch (Exception e) {
                // Ignore
            }
        }
    }

    public void playClickSound() {
        if (!initialized || muted || clickClip == null) return;
        try {
            clickClip.play();
        } catch (Exception e) {
            // Ignore
        }
    }

    public void playCorrectSound() {
        if (!initialized || muted || correctClip == null) return;
        try {
            correctClip.play();
        } catch (Exception e) {
            // Ignore
        }
    }

    public void playFailedSound() {
        if (!initialized || muted || failedClip == null) return;
        try {
            failedClip.play();
        } catch (Exception e) {
            // Ignore
        }
    }

    public boolean isMuted() {
        return muted;
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
        if (bgMusicPlayer != null) {
            bgMusicPlayer.setMute(muted);
        }
        if (muted) {
            if (clickClip != null) clickClip.stop();
            if (correctClip != null) correctClip.stop();
            if (failedClip != null) failedClip.stop();
        } else {
            playBackgroundMusic();
        }
    }

    public boolean toggleMute() {
        setMuted(!muted);
        return muted;
    }
}

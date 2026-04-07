import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;

public class AudioManager {

    private Clip musicClip;

    public void playMusicLoop(String path) {
        if (path == null || path.isEmpty()) {
            return;
        }

        // Reuse the current clip when the same track is already playing.
        if (musicClip != null && musicClip.isOpen()) {
            return;
        }

        File file = new File(path);
        if (!file.exists()) {
            return;
        }

        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file);
            musicClip = AudioSystem.getClip();
            musicClip.open(audioInputStream);
            musicClip.loop(Clip.LOOP_CONTINUOUSLY);
            musicClip.start();
        } catch (Exception e) {
            stopMusic();
        }
    }

    public void stopMusic() {
        if (musicClip == null) {
            return;
        }

        if (musicClip.isRunning()) {
            musicClip.stop();
        }
        musicClip.close();
        musicClip = null;
    }
}
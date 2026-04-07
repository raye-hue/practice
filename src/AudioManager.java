import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.io.File;

public class AudioManager {

    private Clip musicClip;
    private float musicVolumePercent = 100f;

    public void setMusicVolumePercent(float volumePercent) {
        musicVolumePercent = Math.max(0f, Math.min(100f, volumePercent));
        applyVolumeToCurrentClip();
    }

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
            applyVolumeToCurrentClip();
            musicClip.loop(Clip.LOOP_CONTINUOUSLY);
            musicClip.start();
        } catch (Exception e) {
            stopMusic();
        }
    }

    private void applyVolumeToCurrentClip() {
        if (musicClip == null || !musicClip.isOpen()) {
            return;
        }

        if (!musicClip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            return;
        }

        FloatControl gainControl = (FloatControl) musicClip.getControl(FloatControl.Type.MASTER_GAIN);
        if (musicVolumePercent <= 0f) {
            gainControl.setValue(gainControl.getMinimum());
            return;
        }

        float gain = (float) (20.0 * Math.log10(musicVolumePercent / 100.0));
        gain = Math.max(gainControl.getMinimum(), Math.min(gainControl.getMaximum(), gain));
        gainControl.setValue(gain);
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
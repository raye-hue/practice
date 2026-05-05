package core;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import java.io.File;

public class AudioManager {

    private Clip musicClip;
    private float musicVolumePercent = 100f;
    private String musicPath = null;

    public void setMusicVolumePercent(float volumePercent) {
        musicVolumePercent = Math.max(0f, Math.min(100f, volumePercent));
        applyVolumeToCurrentClip();
    }

    public void playMusicLoop(String path) {
        if (path == null || path.isEmpty()) {
            return;
        }

        // If the requested track is already playing, do nothing.
        if (musicClip != null && musicClip.isOpen()) {
            if (musicPath != null && musicPath.equals(path) && musicClip.isRunning()) {
                return;
            }
            // Different track requested; stop current music first.
            stopMusic();
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
            musicPath = path;
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
        musicPath = null;
    }

    public static void playSoundEffect(String path) {
        playSoundEffect(path, 1.0f);
    }

    public static void playSoundEffect(String path, float speedMultiplier) {
        if (path == null || path.isEmpty()) {
            return;
        }

        float playbackSpeed = Math.max(0.1f, speedMultiplier);

        File file = new File(path);
        if (!file.exists()) {
            return;
        }

        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file);
            AudioInputStream din = null;
            try {
                javax.sound.sampled.AudioFormat baseFormat = audioInputStream.getFormat();
                javax.sound.sampled.AudioFormat targetFormat = new javax.sound.sampled.AudioFormat(
                        javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED,
                        baseFormat.getSampleRate() * playbackSpeed,
                        16,
                        baseFormat.getChannels(),
                        baseFormat.getChannels() * 2,
                        baseFormat.getSampleRate() * playbackSpeed,
                        false
                );
                if (AudioSystem.isConversionSupported(targetFormat, baseFormat)) {
                    din = AudioSystem.getAudioInputStream(targetFormat, audioInputStream);
                }
            } catch (Exception conversionIgnored) {
                // Fall back to original stream if conversion not possible
            }

            Clip clip = AudioSystem.getClip();
            if (din != null) {
                clip.open(din);
            } else {
                clip.open(audioInputStream);
            }
            clip.addLineListener(new LineListener() {
                @Override
                public void update(LineEvent event) {
                    if (event.getType() == LineEvent.Type.STOP) {
                        try {
                            event.getLine().close();
                        } catch (Exception ignored) {}
                    }
                }
            });
            try {
                clip.setFramePosition(0);
            } catch (Exception ignored) {}
            clip.start();
        } catch (Exception ignored) {
            // Print the exception to console for debugging missing/unplayable audio files.
            System.err.println("AudioManager: failed to play sound: " + path);
            ignored.printStackTrace();
        }
    }
}

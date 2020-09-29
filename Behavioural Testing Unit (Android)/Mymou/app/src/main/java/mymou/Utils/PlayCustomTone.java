package mymou.Utils;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;
import android.util.Log;

/**
 * The type Play tone thread.
 */
class PlayCustomTone extends Thread {

    private boolean isPlaying = false;
    private final int freqOfTone;
    private final int duration;
    private AudioTrack audioTrack = null;

    /**
     * Instantiates a new Play tone thread.
     *
     * @param freqOfTone the freq of tone
     * @param duration the duration
     */
    public PlayCustomTone(int freqOfTone, int duration) {
        this.freqOfTone = freqOfTone;
        this.duration = duration;
    }

    @Override public void run() {
        super.run();

        if (!isPlaying) {
            isPlaying = true;

            int sampleRate = 44100;// 44.1 KHz
            float duration_s = duration;
            duration_s /= 1000;
            
            Log.d("asdf", ""+duration_s);
            double dnumSamples = duration_s * sampleRate;
            dnumSamples = Math.ceil(dnumSamples);
            int numSamples = (int) dnumSamples;
            double[] sample = new double[numSamples];
            byte[] generatedSnd = new byte[2 * numSamples];

            for (int i = 0; i < numSamples; ++i) {      // Fill the sample array
                sample[i] = Math.sin(freqOfTone * 2 * Math.PI * i / (sampleRate));
            }

            // convert to 16 bit pcm sound array
            int idx = 0;
            int i;
            int ramp = numSamples / 20;  // Amplitude ramp as a percent of sample count

            for (i = 0; i < numSamples - ramp; ++i) {
                // scale to maximum amplitude
                final short val = (short) (sample[i] * 32767);
                // in 16 bit wav PCM, first byte is the low order byte
                generatedSnd[idx++] = (byte) (val & 0x00ff);
                generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
            }

            for (i = numSamples - ramp; i < numSamples; ++i) { // Ramp amplitude down
                // Ramp down to zero
                final short val = (short) (sample[i] * 32767 * (numSamples - i) / ramp);
                // in 16 bit wav PCM, first byte is the low order byte
                generatedSnd[idx++] = (byte) (val & 0x00ff);
                generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
            }

            try {
                int bufferSize = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT);
                audioTrack =
                        new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                                AudioFormat.ENCODING_PCM_16BIT, bufferSize, AudioTrack.MODE_STREAM);

                audioTrack.play(); // Play the track
                audioTrack.write(generatedSnd, 0, generatedSnd.length);    // Load the track
            } catch (Exception e) {
                e.printStackTrace();
            }
            stopTone();
        }
    }

    /**
     * Stop tone.
     */
    void stopTone() {
        if (audioTrack != null && audioTrack.getState() == AudioTrack.PLAYSTATE_PLAYING) {
            audioTrack.stop();
            audioTrack.release();
            isPlaying = false;
        }
    }
}

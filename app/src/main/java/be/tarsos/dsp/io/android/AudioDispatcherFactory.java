package be.tarsos.dsp.io.android;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import java.io.InputStream;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.io.UniversalAudioInputStream;

public class AudioDispatcherFactory {

    public static AudioDispatcher fromDefaultMicrophone(
            int sampleRate,
            int audioBufferSize,
            int bufferOverlap) {

        // Get minimum buffer size required
        int minBufferSize = AudioRecord.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
        );

        // Create AudioRecord object
        AudioRecord audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                Math.max(minBufferSize, audioBufferSize)
        );

        // Define audio format
        TarsosDSPAudioFormat format = new TarsosDSPAudioFormat(
                sampleRate,
                16,
                1,
                true,
                false
        );

        // Create input stream from microphone
        InputStream audioStream = new AudioRecordInputStream(audioRecord);

        // Create and return AudioDispatcher
        return new AudioDispatcher(
                new UniversalAudioInputStream(audioStream, format),
                audioBufferSize,
                bufferOverlap
        );
    }
}

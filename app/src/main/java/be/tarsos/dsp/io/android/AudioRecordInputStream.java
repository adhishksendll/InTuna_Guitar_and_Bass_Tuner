package be.tarsos.dsp.io.android;

import android.media.AudioRecord;

import java.io.InputStream;

public class AudioRecordInputStream extends InputStream {

    private final AudioRecord audioRecord;
    private final byte[] buffer;

    public AudioRecordInputStream(AudioRecord audioRecord) {
        this.audioRecord = audioRecord;
        this.buffer = new byte[1024];
        this.audioRecord.startRecording();
    }

    @Override
    public int read() {
        return 0;
    }

    @Override
    public int read(byte[] b, int off, int len) {
        return audioRecord.read(b, off, len);
    }
}

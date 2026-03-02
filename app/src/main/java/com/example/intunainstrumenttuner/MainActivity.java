package com.example.intunainstrumenttuner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;

import java.util.Collections;
import java.util.LinkedList;

public class MainActivity extends AppCompatActivity {

    TextView noteDisplay;
    TextView frequencyDisplay;
    TextView tuningNameDisplay;

    Button guitarTuningsButton;
    Button bassTuningsButton;
    Button autoDetectButton;
    Button startStopButton;

    LinearLayout stringButtonsLayout;

    TunerMeterView tunerMeter;

    AudioDispatcher dispatcher;

    String[] currentNotes;
    double[] currentFreq;
    String currentTuningName;

    int selectedIndex = 0;

    boolean autoDetect = true;
    boolean isTunerRunning = false;

    public static final int GUITAR_REQUEST = 100;
    public static final int BASS_REQUEST = 200;

    public static final int MIC_PERMISSION_CODE = 1;

    // Pitch stability enhancements
    private static final double AMPLITUDE_THRESHOLD = 0.05;
    private static final int PITCH_BUFFER_SIZE = 10;
    private static final double PITCH_STABILITY_THRESHOLD = 0.5; // Hz
    private final LinkedList<Float> pitchBuffer = new LinkedList<>();
    private float lastStablePitch = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        noteDisplay = findViewById(R.id.noteDisplay);
        frequencyDisplay = findViewById(R.id.frequencyDisplay);
        tuningNameDisplay = findViewById(R.id.tuningNameDisplay);

        guitarTuningsButton = findViewById(R.id.guitarTuningsButton);
        bassTuningsButton = findViewById(R.id.bassTuningsButton);
        autoDetectButton = findViewById(R.id.autoDetectButton);
        startStopButton = findViewById(R.id.startStopButton);

        stringButtonsLayout = findViewById(R.id.stringButtonsLayout);

        tunerMeter = findViewById(R.id.tunerMeter);

        // Default tuning
        currentTuningName = "Standard (6-string)";
        currentNotes = new String[]{"E","A","D","G","B","E"};
        currentFreq = new double[]{82.41,110,146.83,196,246.94,329.63};

        updateUI();
        createStringButtons();

        startStopButton.setOnClickListener(v -> {
            if (isTunerRunning) {
                stopTuner();
            } else {
                requestMicPermission();
            }
        });

        guitarTuningsButton.setOnClickListener(v -> {
            // Check if these activities exist or use a simplified approach if they don't
            try {
                Intent intent = new Intent(MainActivity.this, Class.forName("com.example.intunainstrumenttuner.GuitarTuningsActivity"));
                startActivityForResult(intent, GUITAR_REQUEST);
            } catch (ClassNotFoundException e) {
                Toast.makeText(this, "Guitar Tunings Activity not found", Toast.LENGTH_SHORT).show();
            }
        });

        bassTuningsButton.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(MainActivity.this, Class.forName("com.example.intunainstrumenttuner.BassTuningsActivity"));
                startActivityForResult(intent, BASS_REQUEST);
            } catch (ClassNotFoundException e) {
                Toast.makeText(this, "Bass Tunings Activity not found", Toast.LENGTH_SHORT).show();
            }
        });

        autoDetectButton.setOnClickListener(v -> {

            autoDetect = !autoDetect;

            if(autoDetect)
                autoDetectButton.setText("AUTO DETECT: ON");
            else
                autoDetectButton.setText("AUTO DETECT: OFF");
        });
    }

    private void requestMicPermission(){

        if(ContextCompat.checkSelfPermission(
                MainActivity.this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    MIC_PERMISSION_CODE
            );

        } else {

            startTuner();
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults){

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults);

        if(requestCode == MIC_PERMISSION_CODE){

            if(grantResults.length>0 &&
                    grantResults[0]==PackageManager.PERMISSION_GRANTED){

                startTuner();

            }else{

                Toast.makeText(this,
                        "Microphone permission required",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startTuner() {
        if (!isTunerRunning) {
            startPitchDetection();
            isTunerRunning = true;
            startStopButton.setText("Stop");
        }
    }

    private void stopTuner() {
        if (isTunerRunning) {
            if (dispatcher != null) {
                dispatcher.stop();
            }
            isTunerRunning = false;
            startStopButton.setText("Start");
        }
    }


    private void startPitchDetection(){

        dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(44100, 2048, 0);

        PitchDetectionHandler handler = new PitchDetectionHandler() {
            @Override
            public void handlePitch(PitchDetectionResult result, AudioEvent e) {
                if (e.getRMS() > AMPLITUDE_THRESHOLD) {
                    float pitch = result.getPitch();
                    if (pitch > 0) {
                        processPitch(pitch);
                    }
                }
            }
        };

        AudioProcessor processor = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 44100, 2048, handler);
        dispatcher.addAudioProcessor(processor);

        new Thread(dispatcher, "Pitch Thread").start();
    }

    private void processPitch(float pitch) {
        pitchBuffer.add(pitch);
        if (pitchBuffer.size() > PITCH_BUFFER_SIZE) {
            pitchBuffer.removeFirst();
        }

        if (isPitchStable()) {
            float stablePitch = getMedian(new LinkedList<>(pitchBuffer));
            if (Math.abs(stablePitch - lastStablePitch) > PITCH_STABILITY_THRESHOLD) {
                lastStablePitch = stablePitch;
                runOnUiThread(() -> {
                    frequencyDisplay.setText(String.format("%.2f Hz", stablePitch));

                    if (autoDetect) {
                        selectedIndex = findClosestString(stablePitch);
                    }

                    noteDisplay.setText(currentNotes[selectedIndex]);
                    updateNeedle(stablePitch);
                });
            }
        }
    }

    private boolean isPitchStable() {
        if (pitchBuffer.size() < PITCH_BUFFER_SIZE) {
            return false;
        }
        float min = Collections.min(pitchBuffer);
        float max = Collections.max(pitchBuffer);
        return (max - min) < PITCH_STABILITY_THRESHOLD;
    }


    private float getMedian(LinkedList<Float> list) {
        if (list.isEmpty()) return 0;
        Collections.sort(list);
        int middle = list.size() / 2;
        if (list.size() % 2 == 1) {
            return list.get(middle);
        } else {
            return (list.get(middle - 1) + list.get(middle)) / 2.0f;
        }
    }


    private void updateNeedle(float pitch){

        double target = currentFreq[selectedIndex];

        double diff = pitch - target;

        float meterPos =
                (float)(0.5 + diff/25);

        tunerMeter.setNeedlePosition(meterPos);
    }

    private int findClosestString(double pitch){

        int index = 0;

        double minDiff =
                Math.abs(pitch-currentFreq[0]);

        for(int i=1;i<currentFreq.length;i++){

            double diff =
                    Math.abs(pitch-currentFreq[i]);

            if(diff<minDiff){

                minDiff=diff;
                index=i;
            }
        }

        return index;
    }

    private void updateUI(){

        tuningNameDisplay.setText(currentTuningName);

        noteDisplay.setText(currentNotes[selectedIndex]);

        tunerMeter.setNeedlePosition(0.5f);
    }

    private void createStringButtons(){

        stringButtonsLayout.removeAllViews();

        for(int i=0;i<currentNotes.length;i++){

            int index=i;

            Button button=new Button(this);

            button.setText(currentNotes[i]);

            button.setOnClickListener(v->{

                autoDetect=false;

                autoDetectButton.setText("AUTO DETECT: OFF");

                selectedIndex=index;

                updateUI();
            });

            stringButtonsLayout.addView(button);
        }
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data){

        super.onActivityResult(requestCode,resultCode,data);

        if(resultCode==RESULT_OK && data!=null){

            currentTuningName=
                    data.getStringExtra("tuningName");

            currentNotes=
                    data.getStringArrayExtra("notes");

            currentFreq=
                    data.getDoubleArrayExtra("freq");

            selectedIndex=0;

            updateUI();

            createStringButtons();
        }
    }

    @Override
    protected void onDestroy(){

        super.onDestroy();

        if(dispatcher!=null)
            dispatcher.stop();
    }
}

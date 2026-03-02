package com.example.intunainstrumenttuner;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class GuitarTuningsActivity extends AppCompatActivity {

    String[] tuningNames = {

            "Standard (6)",
            "Drop D",
            "Drop C",
            "Drop B",

            "Standard (7)",
            "Drop A (7)",

            "Standard (8)",
            "Drop E (8)"
    };

    String[][] notes = {

            {"E","A","D","G","B","E"},
            {"D","A","D","G","B","E"},
            {"C","G","C","F","A","D"},
            {"B","F#","B","E","G#","C#"},

            {"B","E","A","D","G","B","E"},
            {"A","E","A","D","G","B","E"},

            {"F#","B","E","A","D","G","B","E"},
            {"E","B","E","A","D","G","B","E"}
    };

    double[][] freq = {

            {82.41,110,146.83,196,246.94,329.63},
            {73.42,110,146.83,196,246.94,329.63},
            {65.41,98,130.81,174.61,220,293.66},
            {61.74,92.50,123.47,164.81,207.65,277.18},

            {61.74,82.41,110,146.83,196,246.94,329.63},
            {55.00,82.41,110,146.83,196,246.94,329.63},

            {46.25,61.74,82.41,110,146.83,196,246.94,329.63},
            {41.20,61.74,82.41,110,146.83,196,246.94,329.63}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);

        LinearLayout layout=new LinearLayout(this);

        layout.setOrientation(LinearLayout.VERTICAL);

        TextView title=new TextView(this);

        title.setText("Select Guitar Tuning");

        layout.addView(title);

        for(int i=0;i<tuningNames.length;i++){

            int index=i;

            Button button=new Button(this);

            button.setText(tuningNames[index]);

            button.setOnClickListener(v->{

                Intent result=new Intent();

                result.putExtra("tuningName",
                        tuningNames[index]);

                result.putExtra("notes",
                        notes[index]);

                result.putExtra("freq",
                        freq[index]);

                setResult(RESULT_OK,result);

                finish();
            });

            layout.addView(button);
        }

        setContentView(layout);
    }
}

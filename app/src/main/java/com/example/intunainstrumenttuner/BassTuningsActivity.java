package com.example.intunainstrumenttuner;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class BassTuningsActivity extends AppCompatActivity {

    String[] tuningNames={

            "Standard (4)",
            "Drop D",

            "Standard (5)",
            "Drop A",

            "Standard (6)"
    };

    String[][] notes={

            {"E","A","D","G"},
            {"D","A","D","G"},

            {"B","E","A","D","G"},
            {"A","E","A","D","G"},

            {"B","E","A","D","G","C"}
    };

    double[][] freq={

            {41.20,55,73.42,98},
            {36.71,55,73.42,98},

            {30.87,41.20,55,73.42,98},
            {27.50,41.20,55,73.42,98},

            {30.87,41.20,55,73.42,98,130.81}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);

        LinearLayout layout=new LinearLayout(this);

        layout.setOrientation(LinearLayout.VERTICAL);

        TextView title=new TextView(this);

        title.setText("Select Bass Tuning");

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

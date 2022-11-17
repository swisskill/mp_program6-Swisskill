package com.example.botcontrol;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //---------------------------------Setup----------------------------------------------------
        Objects.requireNonNull(((MainActivity) this).getSupportActionBar()).hide();//remove toolbar
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        JoystickView joystick = findViewById(R.id.joystickView);
        TextView textView = findViewById(R.id.textView);
        //---------------------------------BUTTONS--------------------------------------------------
        joystick.setOnMoveListener((angle, strength) -> {//lambda; cause why not
            int quad;
            if (strength !=0){
                quad = quadrant(angle); //quad is important for x,y movement. actually, may just want to send out x,y pair, not quad
            } else {quad = 0;}
            textView.setText(angle + ":" + quad + ":" + strength);
        });
    }
    //---------------------------------METHODS------------------------------------------------------
    public int quadrant(int angle){
        int quad = 0;
        if(337.5<angle || angle<22.5){quad = 1;}  //E ;that's so weird that I have to use an or. I wish i could just do #<angle<#
        if(22.5<angle && angle<67.5){quad = 2;}   //NE
        if(67.5<angle && angle<112.5){quad = 3;}  //N
        if(112.5<angle && angle<157.5){quad = 4;} //NW
        if(157.5<angle && angle<202.5){quad = 5;} //W
        if(202.5<angle && angle<247.5){quad = 6;} //SW
        if(247.5<angle && angle<292.5){quad = 7;} //S
        if(292.5<angle && angle<337.5){quad = 8;} //SE
        return quad;
    }
}




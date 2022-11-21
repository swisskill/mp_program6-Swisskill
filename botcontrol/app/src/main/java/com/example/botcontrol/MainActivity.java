package com.example.botcontrol;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    String IP;
    String Player;
    @SuppressLint({"SetTextI18n", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //---------------------------------Setup----------------------------------------------------
        Objects.requireNonNull(((MainActivity) this).getSupportActionBar()).hide();//remove toolbar
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        JoystickView joystick = findViewById(R.id.joystickView);
        TextView textView = findViewById(R.id.textView);
        FloatingActionButton fab = findViewById(R.id.floatingActionButton);
        Spinner dropdown = findViewById(R.id.spinner);
        String[] items = new String[]{"Demo", "Tank", "Scout"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);
        Log.d("", dropdown.getSelectedItem().toString()); // this is how you get the item out of it baby
        FloatingActionButton net = findViewById(R.id.net);
        //---------------------------------BUTTONS--------------------------------------------------
        fab.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction()==MotionEvent.ACTION_DOWN){
                    fab.setForeground(getResources().getDrawable(R.drawable.bpress));
                    //Create thread for fire networking?
                    //will need to depend on bot probably. different functions or something
                } else {
                    fab.setForeground(getResources().getDrawable(R.drawable.nopress));
                }
                return false;
            }
        });
        //---------------------------
        net.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
                //probably now run a network thread that establishes a connection with the server
            }
        });
        //---------------------------
        joystick.setOnMoveListener((angle, strength) -> {//lambda; cause why not
            int[] coord = {0,0}; //this scares me. how often is it getting reset. is this bad programming?
            if (strength !=0){
                coord = quadrant(angle); //this stuff seems sketch
            } else {coord[0] = 0; coord[1] = 0;}
            textView.setText(angle + ": " + coord[0] + " " + coord[1] );
        });
    }
    //---------------------------------METHODS------------------------------------------------------
    public int[] quadrant(int angle){
        int[] coord = {0,0}; //where 0th is x and 1st is y
        if(337.5<angle || angle<22.5){coord[0] = 1; coord[1] = 0;}  //E ;that's so weird that I have to use an or. I wish i could just do #<angle<#
        if(22.5<angle && angle<67.5){coord[0] = 1; coord[1] = 1;}   //NE
        if(67.5<angle && angle<112.5){coord[0] = 0; coord[1] = 1;}  //N
        if(112.5<angle && angle<157.5){coord[0] = -1; coord[1] = 1;} //NW
        if(157.5<angle && angle<202.5){coord[0] = -1; coord[1] = 0;} //W
        if(202.5<angle && angle<247.5){coord[0] = -1; coord[1] = -1;} //SW
        if(247.5<angle && angle<292.5){coord[0] = 0; coord[1] = -1;} //S
        if(292.5<angle && angle<337.5){coord[0] = 1; coord[1] = -1;} //SE
        return coord;
    }
    //---------------------------
    void showDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        final View textenter = inflater.inflate(R.layout.dialog, null);
        final EditText ip = textenter.findViewById(R.id.ip); //does this need to be a "final"
        final EditText player = textenter.findViewById(R.id.player);

        final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, androidx.appcompat.R.style.Base_Theme_AppCompat_Dialog));
        builder.setView(textenter).setTitle("Connect");
        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //probably something about submitting to the server
                // or maybe we just set things and then keep networking in the ui code instead of a function call
                IP = ip.getText().toString();
                Player = player.getText().toString();
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel(); //don't know if this is needed
            }
        });
        builder.show();
    }

}




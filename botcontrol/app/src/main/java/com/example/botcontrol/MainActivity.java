package com.example.botcontrol;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    String IP;
    int PORT = 3012;
    String Player;
    String whichBot;
    TextView logger;
    int rCompass;
    Thread startNet;
    Thread send;
    Connect start;
    Sendmsg Sendmsg;
    public PrintWriter out;
    public BufferedReader in;
    String botType;
    @SuppressLint({"SetTextI18n", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //---------------------------------Setup----------------------------------------------------
        Objects.requireNonNull(((MainActivity) this).getSupportActionBar()).hide();//remove toolbar
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //-------------------declarations
        JoystickView joystick = findViewById(R.id.joystickView);
        JoystickView joystick2 = findViewById(R.id.joystickView2);
        FloatingActionButton fire = findViewById(R.id.floatingActionButton);
        logger = findViewById(R.id.textView);
        FloatingActionButton net = findViewById(R.id.net);
        Spinner dropdown = findViewById(R.id.spinner);
        String[] items = new String[]{"Demo", "Tank", "Scout"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);
        Log.d("", dropdown.getSelectedItem().toString()); // this is how you get the item out of it baby
        //---------------------------------BUTTONS--------------------------------------------------
        fire.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction()==MotionEvent.ACTION_DOWN){
                    fire.setForeground(getResources().getDrawable(R.drawable.bpress));
                } else {
                    fire.setForeground(getResources().getDrawable(R.drawable.nopress));
                    Sendmsg = new Sendmsg("fire " + rCompass);
                    send = new Thread(Sendmsg);
                    send.start();
                }
                return false;
            }
        });
        //---------------------------
        net.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                whichBot = dropdown.getSelectedItem().toString();
                showDialog();
                botType = dropdown.getSelectedItem().toString();
            }
        });
        //----------------------------
        joystick.setOnMoveListener((angle, strength) -> {
            int[] coord = {0,0};
            if (strength !=0){
                coord = quadrant(angle);
            }
//            Log.d("ANGLE:X:Y", angle + ": " + coord[0] + " " + coord[1] + " str " + strength );
            Sendmsg = new Sendmsg("move " + coord[0] + " " + coord[1]);
            send = new Thread(Sendmsg);
            send.start();
        });
        //---------------------------
        joystick2.setOnMoveListener((angle, strength) -> {//lambda; cause why not
            if(angle > 0 && angle < 90){rCompass = 90-angle;}
            else{rCompass = 450-angle;}
//            Log.d("ANGLE", rCompass + " : " + angle);
        });
    }
    //---------------------------------Network Classes------------------------------------------------------
    public class Sendmsg implements Runnable{
        String sendOpt;
        public Sendmsg(String opt){
            sendOpt = opt;
        }
        public void run(){
            try{
                out.println(sendOpt);
            } catch (Exception e){
                mkmsg(String.valueOf(e));
            }
        }
    }
    public class Connect implements Runnable{
        String str = "";
        public void run() {
            mkmsg("HOST:" + IP);
            try {
                InetAddress serverAddr = InetAddress.getByName(IP);
                mkmsg("Attempt Connecting..." + IP + "\n");
                Socket socket = new Socket(serverAddr, PORT);

                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                try {
//                    armor bullet scan
                    mkmsg("Attempting to send message ...\n");
                    if (botType.compareTo("Scout") == 0) {
                        out.println("Swisskill 0 0 3");
                    } else if (botType.compareTo("Tank") == 0) {
                        out.println("Swisskill 4 1 0");
                    } else {
                        out.println("Swisskill 0 0 0");
                    }
                    while (str.compareTo("GameOver") != 0) {
//                    mkmsg("Message sent...\n");
//                    mkmsg("Attempting to receive a message ...\n");
                    str = in.readLine();
                    Log.d("status ", str);
                    mkmsg("received a message:\n" + str + "\n");
                    }
                } catch (Exception e) {
                    Log.wtf("Error happened sending/receiving ", e);
                    mkmsg("Error happened sending/receiving\n");
                }
            } catch (Exception e) {
                Log.wtf("Unable to connect... ", e);
                mkmsg("Unable to connect...\n");
            }
        }
    }
    //---------------------------------METHODS------------------------------------------------------
    public int[] quadrant(int angle){
        int[] coord = {0,0}; //where 0th is x and 1st is y
        if(337.5<angle || angle<22.5){coord[0] = 1; coord[1] = 0;}  //E
        if(22.5<angle && angle<67.5){coord[0] = 1; coord[1] = -1;}   //NE
        if(67.5<angle && angle<112.5){coord[0] = 0; coord[1] = -1;}  //N
        if(112.5<angle && angle<157.5){coord[0] = -1; coord[1] = -1;} //NW
        if(157.5<angle && angle<202.5){coord[0] = -1; coord[1] = 0;} //W
        if(202.5<angle && angle<247.5){coord[0] = -1; coord[1] = 1;} //SW
        if(247.5<angle && angle<292.5){coord[0] = 0; coord[1] = 1;} //S
        if(292.5<angle && angle<337.5){coord[0] = 1; coord[1] = 1;} //SE
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
                IP = ip.getText().toString();
                if (IP.compareTo("home") == 0){IP = "192.168.1.97";}
                else if (IP.compareTo("class") == 0){IP = "10.216.217.131";}
                Player = player.getText().toString();
                start = new Connect();
                startNet = new Thread(start);
                startNet.start();
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.show();
    }
    public Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            logger.setText(msg.getData().getString("msg"));
            return true;
        }
    });
    public void mkmsg(String str) {
        Message msg = new Message();
        Bundle b = new Bundle();
        b.putString("msg", str);
        msg.setData(b);
        handler.sendMessage(msg);
    }

}




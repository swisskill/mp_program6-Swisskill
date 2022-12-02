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
        //        logger.append("\n"); why was this in Jim's code?
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
            }
        });
        //----------------------------
        joystick.setOnMoveListener((angle, strength) -> {//lambda; cause why not
            int[] coord = {0,0};
            if (strength !=0){
                coord = quadrant(angle);
            }
            Log.wtf("ANGLE:X:Y", angle + ": " + coord[0] + " " + coord[1] + " str " + strength );
            Sendmsg = new Sendmsg("move " + coord[0] + " " + coord[1]);
            send = new Thread(Sendmsg);
            send.start();
        });
        //---------------------------
        joystick2.setOnMoveListener((angle, strength) -> {//lambda; cause why not
            rCompass = (angle - 90);
            if(rCompass < 0){rCompass = rCompass*-1;}
            Log.d("ANGLE", rCompass + "");
            //we'll need to just save this information. No need to send any net threads until firing
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
        public void run() {
            mkmsg("HOST:" + IP);
            try {
                InetAddress serverAddr = InetAddress.getByName(IP);
                mkmsg("Attempt Connecting..." + IP + "\n");
                Socket socket = new Socket(serverAddr, PORT);

                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                //strangely enough, messages aren't updating. Need to find a way to actually continue to run this thing. While?
                try {
                    //write a message to the server
                    mkmsg("Attempting to send message ...\n");
                    out.println("Swisskill 0 4 1");//now we know this is where you send messages to the server
                    mkmsg("Message sent...\n");
                    mkmsg("Attempting to receive a message ...\n");
                    String str = in.readLine();
                    mkmsg("received a message:\n" + str + "\n");
                } catch (Exception e) {
                    mkmsg("Error happened sending/receiving\n");
                }
            } catch (Exception e) {
                Log.wtf("", e);
                mkmsg("Unable to connect...\n");
            }
        }
    }
    //---------------------------------METHODS------------------------------------------------------
    public int[] quadrant(int angle){
        int[] coord = {0,0}; //where 0th is x and 1st is y
        if(337.5<angle || angle<22.5){coord[0] = 1; coord[1] = 0;}  //E
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
                IP = ip.getText().toString();
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




/*

public class MainActivity extends AppCompatActivity {
    TextView logger;
    Button mkconn;
    EditText hostname, port;
    Thread myNet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        logger = findViewById(R.id.logger);
        logger.append("\n");
        hostname = findViewById(R.id.EThostname);
        //This address is the localhost for the computer the emulator is running on.  If you are running
        //tcpserv in another emulator on the same machine, use this address
        hostname.setText("10.0.2.2");
        //This would be more running on the another phone or different host and likely not this ip address either.
        //hostname.setText("10.121.174.200");
        port = findViewById(R.id.ETport);
        mkconn = findViewById(R.id.makeconn);
        mkconn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Connect stuff = new Connect();
                myNet = new Thread(stuff);
                myNet.start();
            }
        });
    }

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            logger.append(msg.getData().getString("msg"));
            return true;
        }

    });

    public void mkmsg(String str) {
        //handler junk, because thread can't update screen!
        Message msg = new Message();
        Bundle b = new Bundle();
        b.putString("msg", str);
        msg.setData(b);
        handler.sendMessage(msg);
    }

    /**
     * this code does most of the work in a thread, so that it doesn't lock up the activity_main (UI) thread
     * It call mkmsg (which calls the handler to update the screen)
     */
/*
    class Connect implements Runnable {
        public PrintWriter out;
        public BufferedReader in;

        public void run() {
            int p = Integer.parseInt(port.getText().toString());
            String h = hostname.getText().toString();
            mkmsg("host is " + h + "\n");
            mkmsg(" Port is " + p + "\n");
            try {
                InetAddress serverAddr = InetAddress.getByName(h);
                mkmsg("Attempt Connecting..." + h + "\n");
                Socket socket = new Socket(serverAddr, p);
                String message = "Hello from Client android emulator";

                //made connection, setup the read (in) and write (out)
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                //now send a message to the server and then read back the response.
                try {
                    //write a message to the server
                    mkmsg("Attempting to send message ...\n");
                    out.println(message);
                    mkmsg("Message sent...\n");

                    //read back a message from the server.
                    mkmsg("Attempting to receive a message ...\n");
                    String str = in.readLine();
                    mkmsg("received a message:\n" + str + "\n");

                    mkmsg("We are done, closing connection\n");
                } catch (Exception e) {
                    mkmsg("Error happened sending/receiving\n");

                } finally {
                    in.close();
                    out.close();
                    socket.close();
                }

            } catch (Exception e) {
                mkmsg("Unable to connect...\n");
            }
        }
    }
}
*/

}




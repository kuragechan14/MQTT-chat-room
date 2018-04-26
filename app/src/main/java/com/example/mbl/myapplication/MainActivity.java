package com.example.mbl.myapplication;

import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;

    private Button pub_btn,sub_btn,sub_btn2,conn_btn,disconn_btn;
    private TextView subText,subText2;
    private EditText pub_msg;
    static String mqtt_host = "tcp://m14.cloudmqtt.com:14392";
    static String username = "mntjixvc";
    static String password = "hVzk8tgGTyf4";
    static String pub_topic = "test/B2A";   //each chatroom different
    static String sub_topic = "test/B2A";
    static String sub_topic2 = "test/A2B";
    static String clientId="bs";   //every host different
    MqttAndroidClient client;
    MqttConnectOptions options;
    Vibrator vibrator;

    ListView listview;


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dashboard);
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        findID();

        String[] str={"msg1","msg2"};
        ArrayAdapter adapter=new ArrayAdapter(this,android.R.layout.simple_list_item_1,str);
        listview.setAdapter(adapter);




        ////////////////////////MQTT/////////////////////////
        //String clientId = MqttClient.generateClientId();
        client =new MqttAndroidClient(this.getApplicationContext(), mqtt_host,clientId);
        options = new MqttConnectOptions();  //set into option
        options.setUserName(username);
        options.setPassword(password.toCharArray());

        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            //recieve msg
            public void messageArrived(String topic, MqttMessage message) throws Exception {

                if(topic.equalsIgnoreCase(sub_topic))
                {
                    subText.setText("送出: "+new String(message.getPayload()));
                    vibrator.vibrate(500);  //set intervel
                }
                if(topic.equalsIgnoreCase(sub_topic2))
                {
                    subText2.setText("收到: "+new String(message.getPayload()));
                    vibrator.vibrate(500);  //set intervel
                }

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

        conn_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Log.d("MQTT", "conn_btn");
                conn();  //connect to MQTT broker
            }
        });

        disconn_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Log.d("MQTT", "disconn_btn");
                disconn();  //Disconnect
            }
        });
        pub_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Log.d("MQTT", "pub_button");

                String pub_input=pub_msg.getText().toString();
                pub(pub_topic,pub_input);  //publish
                Toast.makeText(MainActivity.this,"Publish!",Toast.LENGTH_LONG).show();
            }
        });

        sub_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Log.d("MQTT", "sub_button");
                sub(sub_topic);  //subscribe
                sub(sub_topic2);
                Toast.makeText(MainActivity.this,"Subscribe!",Toast.LENGTH_LONG).show();
            }
        });


/*
        sub_btn2.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Log.d("MQTT", "sub_button2");
                sub(sub_topic2);  //subscribe
                Toast.makeText(MainActivity.this,"Subscribe!",Toast.LENGTH_LONG).show();
            }
        });
*/

    }

    public void findID()
    {
        pub_btn = (Button) findViewById(R.id.pub_btn);
        sub_btn = (Button) findViewById(R.id.sub_btn);
        sub_btn2 = (Button) findViewById(R.id.sub_btn2);
        conn_btn = (Button) findViewById(R.id.conn_btn);
        disconn_btn = (Button) findViewById(R.id.disconn_btn);
        subText=(TextView)findViewById(R.id.subText);
        subText2=(TextView)findViewById(R.id.subText2);
        vibrator=(Vibrator)getSystemService(VIBRATOR_SERVICE);
        pub_msg=(EditText) findViewById(R.id.pub_msg);

        listview=(ListView)findViewById(R.id.listview);

    }

    public void conn()
    {
        try {
            //IMqttToken token = client.connect();
            IMqttToken token = client.connect(options); //try connection
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Log.d("MQTT", "Conn: onSuccess");
                    //sub();  //subscribe
                    Toast.makeText(MainActivity.this,"Connect Success!",Toast.LENGTH_LONG).show();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.d("MQTT", "Conn: onFailure");
                    Toast.makeText(MainActivity.this,"Connect Failed!",Toast.LENGTH_LONG).show();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void disconn()
    {
        try {
            //IMqttToken token = client.connect();
            IMqttToken token = client.disconnect(); //try connection
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Log.d("MQTT", "Disconn: onSuccess");
                    Toast.makeText(MainActivity.this,"Disconnect Success!",Toast.LENGTH_LONG).show();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.d("MQTT", "Disconn: onFailure");
                    Toast.makeText(MainActivity.this,"Disconnect Failed!",Toast.LENGTH_LONG).show();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void pub(String topic,String msg)
    {
        Log.d("MQTT", "pub");
        try {
            client.publish(topic, msg.getBytes(),0,false);  //???
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void sub(String topic)
    {
        Log.d("MQTT", "sub");
        try {
            client.subscribe(topic,0);  //qos=0
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }


}

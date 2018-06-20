package com.example.alstn0107.mypiano_real;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String DO = "1";
    public static final String RE = "2";
    public static final String MI = "3";
    public static final String PA = "4";
    public static final String SOL = "5";
    public static final String LA = "6";
    public static final String SI = "7";
    public static final String DO_2 = "8";

    // GUI Components
    private TextView mBluetoothStatus;
    private TextView mReadBuffer;
    private Button mListPairedDevicesBtn;
    private Button btPiano;
    private Button btDrum;
    private Button btGuitar;
    private Button btEguitar;
    private Button mDiscoverBtn;
    private BluetoothAdapter mBTAdapter;
    private Set<BluetoothDevice> mPairedDevices;
    private ArrayAdapter<String> mBTArrayAdapter;
    private ListView mDevicesListView;

    private static Handler mHandler; // Our main handler that will receive callback notifications
    private ConnectedThread mConnectedThread; // bluetooth background worker thread to send and receive data
    private BluetoothSocket mBTSocket = null; // bi-directional client-to-client data path

    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // "random" unique identifier


    // #defines for identifying shared types between calling functions
    private final static int REQUEST_ENABLE_BT = 1; // used to identify adding bluetooth names
    private final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message update
    private final static int CONNECTING_STATUS = 3; // used in bluetooth handler to identify message status

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBluetoothStatus = (TextView) findViewById(R.id.bluetoothStatus);
        mReadBuffer = (TextView) findViewById(R.id.readBuffer);
        //mScanBtn = (Button)findViewById(R.id.scan);   scan 없앰
        mDiscoverBtn = (Button) findViewById(R.id.discover);
        mListPairedDevicesBtn = (Button) findViewById(R.id.PairedBtn);

        mBTArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        mBTAdapter = BluetoothAdapter.getDefaultAdapter(); // get a handle on the bluetooth radio

        mDevicesListView = (ListView) findViewById(R.id.devicesListView);
        mDevicesListView.setAdapter(mBTArrayAdapter); // assign model to view
        mDevicesListView.setOnItemClickListener(mDeviceClickListener);

        mHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                String type = ((MyApplication) getApplication()).getType();

                if (msg.what == MESSAGE_READ) {
                    String readMessage = null;
                    try {
                        readMessage = new String((byte[]) msg.obj, 0, msg.arg1, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    //Toast.makeText(MainActivity.this, readMessage, Toast.LENGTH_SHORT).show();

                    if (readMessage != null) {
                        mReadBuffer.setText(readMessage);  //CharAt()은 왜 안될까 쨋든 여기서 readMessage가 소리를 결정하는 값이다

                        switch (type) {
                            case "piano":
                                playPiano(readMessage);
                                break;
                            case "drum":
                                playDrum(readMessage);
                                break;
                            case "guitarE":
                                playGuitarE(readMessage);
                                break;
                            case "guitar":
                                playGuitar(readMessage);
                                break;
                        }
                    }
                }

                if (msg.what == CONNECTING_STATUS) {
                    if (msg.arg1 == 1)
                        mBluetoothStatus.setText("Connected to Device: " + msg.obj);
                    else
                        mBluetoothStatus.setText("Connection Failed");
                }
            }
        };


        if (mBTArrayAdapter == null) {
            // Device does not support Bluetooth
            mBluetoothStatus.setText("Status: Bluetooth not found");
            Toast.makeText(getApplicationContext(), "Bluetooth device not found!", Toast.LENGTH_SHORT).show();
        } else {
            mListPairedDevicesBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listPairedDevices(v);
                }
            });

            mDiscoverBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    discover(v);
                }
            });
        }
        try {
            AssetFileDescriptor afd = getAssets().openFd("do.mp3");
            MediaPlayer player = new MediaPlayer();
            player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            player.prepare();
            player.start();


        } catch (IOException e) {

        }
        btPiano = (Button) findViewById(R.id.piano);
        btDrum = (Button) findViewById(R.id.drum);   //굉장히 수상한소스
        btEguitar = (Button) findViewById(R.id.guitarE);
        btGuitar = (Button) findViewById(R.id.guitar);

        btPiano.setOnClickListener(this);
        btDrum.setOnClickListener(this);
        btEguitar.setOnClickListener(this);
        btGuitar.setOnClickListener(this);
    }

    private void playPiano(String n) {
        MediaPlayer mp = null;
        switch (n) {
            case DO:
                mp = MediaPlayer.create(this, R.raw.do_1);
                break;
            case RE:
                mp = MediaPlayer.create(this, R.raw.re);
                break;
            case MI:
                mp = MediaPlayer.create(this, R.raw.mi);
                break;
            case PA:
                mp = MediaPlayer.create(this, R.raw.pa);
                break;
            case SOL:
                mp = MediaPlayer.create(this, R.raw.sol);
                break;
            case LA:
                mp = MediaPlayer.create(this, R.raw.la);
                break;
            case SI:
                mp = MediaPlayer.create(this, R.raw.si);
                break;
            case DO_2:
                mp = MediaPlayer.create(this, R.raw.do_2);
                break;
        }
        mp.start();
    }

    private void playDrum(String n) {
        MediaPlayer mp = null;
        switch (n) {
            case DO:
                mp = MediaPlayer.create(this, R.raw.d_close);
                break;
            case RE:
                mp = MediaPlayer.create(this, R.raw.d_crash);
                break;
            case MI:
                mp = MediaPlayer.create(this, R.raw.d_hitom);
                break;
            case PA:
                mp = MediaPlayer.create(this, R.raw.d_kick);
                break;
            case SOL:
                mp = MediaPlayer.create(this, R.raw.d_lowtom);
                break;
            case LA:
                mp = MediaPlayer.create(this, R.raw.d_midtom);
                break;
            case SI:
                mp = MediaPlayer.create(this, R.raw.d_open);
                break;
            case DO_2:
                mp = MediaPlayer.create(this, R.raw.d_ride);
                break;
        }
        mp.start();
    }

    private void playGuitarE(String n) {
        MediaPlayer mp = null;
        switch (n) {
            case DO:
                mp = MediaPlayer.create(this, R.raw.eg_do);
                break;
            case RE:
                mp = MediaPlayer.create(this, R.raw.eg_re);
                break;
            case MI:
                mp = MediaPlayer.create(this, R.raw.eg_mi);
                break;
            case PA:
                mp = MediaPlayer.create(this, R.raw.eg_pa);
                break;
            case SOL:
                mp = MediaPlayer.create(this, R.raw.eg_sol);
                break;
            case LA:
                mp = MediaPlayer.create(this, R.raw.eg_ra);
                break;
            case SI:
                mp = MediaPlayer.create(this, R.raw.eg_si);
                break;
            case DO_2:
                mp = MediaPlayer.create(this, R.raw.eg_do2);
                break;
        }
        mp.start();
    }

    private void playGuitar(String n) {
        MediaPlayer mp = null;
        switch (n) {
            case DO:
                mp = MediaPlayer.create(this, R.raw.g_do);
                break;
            case RE:
                mp = MediaPlayer.create(this, R.raw.g_re);
                break;
            case MI:
                mp = MediaPlayer.create(this, R.raw.g_mi);
                break;
            case PA:
                mp = MediaPlayer.create(this, R.raw.g_pa);
                break;
            case SOL:
                mp = MediaPlayer.create(this, R.raw.g_sol);
                break;
            case LA:
                mp = MediaPlayer.create(this, R.raw.g_ra);
                break;
            case SI:
                mp = MediaPlayer.create(this, R.raw.g_si);
                break;
            case DO_2:
                mp = MediaPlayer.create(this, R.raw.g_do2);
                break;
        }
        mp.start();
    }


    @Override
    public void onClick(View view) {
        Intent intent = null;
        switch (view.getId()) {
            case R.id.piano:
                intent = new Intent(this, PlayActivity.class);
                intent.putExtra("type", "piano");
                break;
            case R.id.drum:
                intent = new Intent(this, PlayActivity.class);
                intent.putExtra("type", "drum");
                break;
            case R.id.guitarE:
                intent = new Intent(this, PlayActivity.class);
                intent.putExtra("type", "guitarE");
                break;
            case R.id.guitar:
                intent = new Intent(this, PlayActivity.class);
                intent.putExtra("type", "guitar");
                break;
        }
        startActivity(intent);
    }

    private void bluetoothOn(View view) {
        if (!mBTAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            mBluetoothStatus.setText("Bluetooth enabled");
            Toast.makeText(getApplicationContext(), "Bluetooth turned on", Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(getApplicationContext(), "Bluetooth is already on", Toast.LENGTH_SHORT).show();
        }
    }

    // Enter here after user selects "yes" or "no" to enabling radio
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent Data) {
        // Check which request we're responding to
        if (requestCode == REQUEST_ENABLE_BT) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // The user picked a contact.
                // The Intent's data Uri identifies which contact was selected.
                mBluetoothStatus.setText("Enabled");
            } else
                mBluetoothStatus.setText("Disabled");
        }
    }


    private void discover(View view) {
        // Check if the device is already discovering
        if (mBTAdapter.isDiscovering()) {
            mBTAdapter.cancelDiscovery();
            Toast.makeText(getApplicationContext(), "Discovery stopped", Toast.LENGTH_SHORT).show();
        } else {
            if (mBTAdapter.isEnabled()) {
                mBTArrayAdapter.clear(); // clear items
                mBTAdapter.startDiscovery();
                Toast.makeText(getApplicationContext(), "Discovery started", Toast.LENGTH_SHORT).show();
                registerReceiver(blReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
            } else {
                Toast.makeText(getApplicationContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
            }
        }
    }

    final BroadcastReceiver blReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // add the name to the list
                mBTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                mBTArrayAdapter.notifyDataSetChanged();
            }
        }
    };

    private void listPairedDevices(View view) {
        mPairedDevices = mBTAdapter.getBondedDevices();
        if (mBTAdapter.isEnabled()) {
            // put it's one to the adapter
            for (BluetoothDevice device : mPairedDevices)
                mBTArrayAdapter.add(device.getName() + "\n" + device.getAddress());

            Toast.makeText(getApplicationContext(), "Show Paired Devices", Toast.LENGTH_SHORT).show();
        } else
            Toast.makeText(getApplicationContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
    }

    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {

            if (!mBTAdapter.isEnabled()) {
                Toast.makeText(getBaseContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
                return;
            }

            mBluetoothStatus.setText("Connecting...");
            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            final String address = info.substring(info.length() - 17);
            final String name = info.substring(0, info.length() - 17);

            // Spawn a new thread to avoid blocking the GUI one
            new Thread() {
                public void run() {
                    boolean fail = false;

                    BluetoothDevice device = mBTAdapter.getRemoteDevice(address);

                    try {
                        mBTSocket = createBluetoothSocket(device);
                    } catch (IOException e) {
                        fail = true;
                        Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                    }
                    // Establish the Bluetooth socket connection.
                    try {
                        mBTSocket.connect();
                    } catch (IOException e) {
                        try {
                            fail = true;
                            mBTSocket.close();
                            mHandler.obtainMessage(CONNECTING_STATUS, -1, -1)
                                    .sendToTarget();
                        } catch (IOException e2) {
                            //insert code to deal with this
                            Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                    if (fail == false) {
                        mConnectedThread = new ConnectedThread(mBTSocket);
                        mConnectedThread.start();

                        mHandler.obtainMessage(CONNECTING_STATUS, 1, -1, name)
                                .sendToTarget();
                    }
                }
            }.start();
        }
    };

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connection with BT device using UUID
    }

    class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.available();
                    if (bytes != 0) {
                        SystemClock.sleep(200); //pause and wait for rest of data. Adjust this depending on your sending speed. 100으로 잘안되면 200으로 바꿔야됩니다.
                        bytes = mmInStream.available(); // how many bytes are ready to be read?
                        bytes = mmInStream.read(buffer, 0, bytes); // record how many bytes we actually read
                        mHandler.obtainMessage(MESSAGE_READ, 1, -1, buffer)
                                .sendToTarget(); // Send the obtained bytes to the UI activity
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }

        }
    }
}


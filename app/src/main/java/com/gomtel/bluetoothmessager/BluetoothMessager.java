package com.gomtel.bluetoothmessager;
    import android.app.Activity;
    import android.bluetooth.BluetoothAdapter;
    import android.bluetooth.BluetoothDevice;
    import android.content.Intent;
    import android.graphics.Bitmap;
    import android.graphics.BitmapFactory;
    import android.graphics.drawable.BitmapDrawable;
    import android.graphics.drawable.Drawable;
    import android.media.AudioManager;
    import android.media.MediaPlayer;
    import android.media.SoundPool;
    import android.os.Bundle;
    import android.os.Handler;
    import android.os.Message;
    import android.util.Base64;
//    import android.util.Log;
    import android.view.KeyEvent;
    import android.view.Menu;
    import android.view.MenuInflater;
    import android.view.MenuItem;
    import android.view.View;
    import android.view.Window;
    import android.view.View.OnClickListener;
    import android.view.inputmethod.EditorInfo;
    import android.widget.ArrayAdapter;
    import android.widget.Button;
    import android.widget.EditText;
    import android.widget.ListView;
    import android.widget.SimpleAdapter;
    import android.widget.TextView;
    import android.widget.Toast;

    import com.gomtel.bluetoothmessager.util.LLog;

    import java.io.IOException;
    import java.util.ArrayList;
    import java.util.HashMap;
    import java.util.List;

/**
 * This is the main Activity that displays the current chat session.
 */
public class BluetoothMessager extends Activity{
    // Debugging
    private static final String TAG = "BluetoothChat";
    private static final boolean D = true;

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final int MESSAGE_FIND = 6;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;
    private static final String CMD_OPEN_REMOTE_CAMERA = "1 RemoteCamera";
    private static final String CMD_CAPTURE_REMOTE_CAMERA = "2 RemoteCamera";


    // Layout Views
    private TextView mTitle;
    private ListView mConversationView;
//    private EditText mOutEditText;
//    private Button mSendButton;

    // Name of the connected device
    private String mConnectedDeviceName = null;
    // Array adapter for the conversation thread
    private MessageAdapter mConversationArrayAdapter;
    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the chat services
    private BluetoothService mService = null;
    private ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String,Object>>();
    private String[] from = {"icon","message"};
    private int[] to = {R.id.notification_icon,R.id.notification_msg};
    private String icon;
    private Drawable iconDrawable;
    private String readMessage = null;
    private String msgContent;
    private String number;
    private MediaPlayer mp;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LLog.e(TAG, "savedInstanceState  "+savedInstanceState);
        if(D) LLog.e(TAG, "+++ ON CREATE +++");

        // Set up the window layout
//        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.main);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mp = MediaPlayer.create(this, R.raw.canon);

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if(D) LLog.e(TAG, "++ ON START ++");
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else {
            if (mService == null) setupChat();
        }
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        if(D) LLog.e(TAG, "+ ON RESUME +");

        if (mService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mService.getState() == BluetoothService.STATE_NONE) {
                // Start the Bluetooth chat services
                mService.start();
            }
        }
    }

    private void setupChat() {
        LLog.d(TAG, "setupChat()");

        // Initialize the array adapter for the conversation thread
        mConversationArrayAdapter = new MessageAdapter(this,listItem,R.layout.message,from,to);
        mConversationView = (ListView) findViewById(R.id.in);
        mConversationView.setDividerHeight(0);
        mConversationView.setAdapter(mConversationArrayAdapter);

        // Initialize the compose field with a listener for the return key
//        mOutEditText = (EditText) findViewById(R.id.edit_text_out);
//        mOutEditText.setOnEditorActionListener(mWriteListener);

        // Initialize the send button with a listener that for click events
//        mSendButton = (Button) findViewById(R.id.button_send);
//        mSendButton.setOnClickListener(new OnClickListener() {
//            public void onClick(View v) {
//                // Send a message using content of the edit text widget
//                TextView view = (TextView) findViewById(R.id.edit_text_out);
//                String message = view.getText().toString();
//                sendMessage(message);
//            }
//        });

        // Initialize the BluetoothChatService to perform bluetooth connections
        mService = new BluetoothService(this, mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        if(D) LLog.e(TAG, "- ON PAUSE -");
    }

    @Override
    public void onStop() {
        super.onStop();
        if(D) LLog.e(TAG, "-- ON STOP --");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth chat services
        mp.release();
        if (mService != null) mService.stop();
        if(D) LLog.e(TAG, "--- ON DESTROY ---");
    }

    private void ensureDiscoverable() {
        if(D) LLog.d(TAG, "ensure discoverable");
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    /**
     * Sends a message.
     * @param message  A string of text to send.
     */
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mService.getState() != BluetoothService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
//            mOutEditText.setText(mOutStringBuffer);
        }
    }

    // The action listener for the EditText widget, to listen for the return key
    private TextView.OnEditorActionListener mWriteListener =
            new TextView.OnEditorActionListener() {
                public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
                    // If the action is a key-up event on the return key, send the message
                    if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                        String message = view.getText().toString();
                        sendMessage(message);
                    }
                    if(D) LLog.i(TAG, "END onEditorAction");
                    return true;
                }
            };



    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {




        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    if(D) LLog.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            mTitle.setText(R.string.title_connected_to);
                            mTitle.append(mConnectedDeviceName);
//                            mConversationArrayAdapter.clear();
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            mTitle.setText(R.string.title_connecting);
                            break;
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_NONE:
                            mTitle.setText(R.string.title_not_connected);
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
//                    byte[] writeBuf = (byte[]) msg.obj;
//                    // construct a string from the buffer
//                    String writeMessage = new String(writeBuf);
//                    mConversationArrayAdapter.add("Me:  " + writeMessage);
                    break;
                case MESSAGE_FIND:
//                    mp.stop();
                    mp.start();
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    List<NotificationMessage> list = null;
                    ParseNotification parse = new ParseNotification();
                    try {
                        LLog.e(TAG,"lixiang---01");
                        list = parse.parse(readBuf);
                        LLog.e(TAG,"lixiang---02");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                    if(list != null) {
                        for (int i = 0; i < list.size(); i++) {
                            HashMap<String,Object> map = new HashMap<String,Object>();
                            NotificationMessage message = list.get(i);
                            String subtype = message.getSubType();
                            if(subtype.equals(ParseNotification.NOTI)) {
                                readMessage =message.getTickerText();
                                icon = message.getIcon();
                                if (icon != null)
                                    iconDrawable = stringToDrawable(icon);
                                map.put("icon", iconDrawable);
                                map.put("message", readMessage);
                            }else if(subtype.equals(ParseNotification.SMS)){
                                msgContent = message.getContent();
                                number = message.getNumber();
                                iconDrawable = resIdToDrawable(R.drawable.message);
                                map.put("icon", iconDrawable);
                                map.put("message", number+": "+msgContent);
                            }else if(subtype.equals(ParseNotification.MISSCALL)){
                                number = message.getNumber();
                                iconDrawable = resIdToDrawable(R.drawable.call);
                                map.put("icon", iconDrawable);
                                map.put("message", getResources().getString(R.string.missed_call)+": "+number);
                            }
                            listItem.add(map);
                        }
                    }

                    mConversationArrayAdapter.notifyDataSetChanged();
                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected to "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();

                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private synchronized Drawable stringToDrawable(String icon) {
        byte[] img= Base64.decode(icon.getBytes(), Base64.DEFAULT);
        Bitmap bitmap;
        if (img != null) {
            bitmap = BitmapFactory.decodeByteArray(img, 0, img.length);
            Drawable drawable = new BitmapDrawable(bitmap);
            LLog.e(TAG,"lixiang---drawable = "+drawable);
            return drawable;
        }
        return null;
    }

    private synchronized Drawable resIdToDrawable(int id) {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(),id);
            Drawable drawable = new BitmapDrawable(bitmap);
            return drawable;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(D) LLog.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupChat();
                } else {
                    // User did not enable Bluetooth or an error occured
                    LLog.d(TAG, "BT not enabled");
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        mService.connect(device, secure);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent serverIntent = null;
        switch (item.getItemId()) {
            case R.id.connect_scan:
                // Launch the DeviceListActivity to see devices and do scan
                serverIntent = new Intent(this, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
                return true;
            case R.id.disconnect_scan:
                // Launch the DeviceListActivity to see devices and do scan
//                serverIntent = new Intent(this, DeviceListActivity.class);
//                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
                if(mService != null)
                    mService.stop();
                return true;

            case R.id.clean_list:
                cleanList();
                return true;

            case R.id.remote_camera:
                sendMessage(CMD_OPEN_REMOTE_CAMERA);
                return true;

            case R.id.remote_capture:
                sendMessage(CMD_CAPTURE_REMOTE_CAMERA);
                return true;
        }
        return false;
    }

    private void cleanList() {
//        mConversationArrayAdapter.clear();
        listItem.clear();
        mConversationArrayAdapter.notifyDataSetChanged();
        mConversationView.setAdapter(mConversationArrayAdapter);
    }

}

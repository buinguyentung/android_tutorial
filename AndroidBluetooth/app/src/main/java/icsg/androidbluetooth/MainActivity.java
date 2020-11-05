package icsg.androidbluetooth;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "AndroidBluetoothApp";

    private static final int REQUEST_ENABLE_BT = 1;

    private static final int REQUEST_ACCESS_COARSE_LOCATION = 16;

    private String device1 = "";

    private final Handler handler = new Handler();
    private Runnable mTimer1;

    ListView listDevicesFound;
    Button btnScanDevice;
    TextView stateBluetooth;
    TextView tvDeviceInfo;
    BluetoothAdapter bluetoothAdapter;
    ArrayAdapter<String> btArrayAdapter;

    private LineChart chart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnScanDevice = (Button) findViewById(R.id.scandevice);
        tvDeviceInfo  = (TextView) findViewById(R.id.deviceinfo);

        stateBluetooth = (TextView) findViewById(R.id.bluetoothstate);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        listDevicesFound = (ListView) findViewById(R.id.devicesfound);
        btArrayAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1);
        listDevicesFound.setAdapter(btArrayAdapter);

        CheckBlueToothState();

        btnScanDevice.setOnClickListener(btnScanDeviceOnClickListener);

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(ActionFoundReceiver, filter);

        // MPAndroidChart
        chart = (LineChart) findViewById(R.id.reportingChart);
        // enable touch gestures
        chart.setTouchEnabled(true);
        // enable scaling and dragging
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setDrawGridBackground(false);
        // if disabled, scaling can be done on x- and y-axis separately
        chart.setPinchZoom(true);

        LineData data = new LineData();
        data.setValueTextColor(Color.RED);
        // add empty data
        chart.setData(data);
        chart.notifyDataSetChanged();

        XAxis xl = chart.getXAxis();
        xl.setTextColor(Color.BLACK);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setAxisMaximum(0f);
        leftAxis.setAxisMinimum(-110f);
        leftAxis.setDrawGridLines(true);
        leftAxis.setEnabled(true);

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);

        chart.getDescription().setEnabled(true);
        Description description = new Description();
        description.setText("Time");
        description.setTextSize(15f);
        chart.setDescription(description);

    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "#onDestroy");
        super.onDestroy();
        unregisterReceiver(ActionFoundReceiver);
    }

    private void CheckBlueToothState(){
        Log.d(TAG, "#CheckBlueToothState");
        if (bluetoothAdapter == null){
            stateBluetooth.setText("Bluetooth NOT support");
        } else {
            Toast.makeText(this, "BluetoothAdapter not null", Toast.LENGTH_SHORT).show();
            if (bluetoothAdapter.isEnabled()){
                if (bluetoothAdapter.isDiscovering()) {
                    stateBluetooth.setText("Bluetooth is currently in device discovery process.");
                } else {
                    stateBluetooth.setText("Bluetooth is Enabled.");
                    btnScanDevice.setEnabled(true);
                }
            } else {
                stateBluetooth.setText("Bluetooth is NOT Enabled!");
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }

    private Button.OnClickListener btnScanDeviceOnClickListener
            = new Button.OnClickListener(){

        @Override
        public void onClick(View arg0) {
            Log.d(TAG, "#onClick");
            Toast.makeText(MainActivity.this, "Start looking", Toast.LENGTH_SHORT).show();
            btArrayAdapter.clear();
            //bluetoothAdapter.startDiscovery();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                switch (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    case PackageManager.PERMISSION_DENIED:
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                REQUEST_ACCESS_COARSE_LOCATION);
                        break;
                    case PackageManager.PERMISSION_GRANTED:
                        boolean a = bluetoothAdapter.startDiscovery();
                        Toast.makeText(MainActivity.this, "Start discovery is " + a, Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }};

    private final BroadcastReceiver ActionFoundReceiver = new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "#onReceive");
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                short rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                Log.d(TAG, "Name: " + device.getName() + "\n" + "Address: " + device.getAddress() + "\n" + "rssi: " + rssi + " dBm");
                btArrayAdapter.add("Name: "+ device.getName() + "\n" + "Address: " + device.getAddress() + "\n" + "rssi: " + rssi + " dBm");
                btArrayAdapter.notifyDataSetChanged();

                if (device1.isEmpty() && rssi > -80) {
                    device1 = device.getAddress();
                    tvDeviceInfo.setText(device.getName() + " " + device1);
                    addData(rssi);
                    Toast.makeText(MainActivity.this, "Device: " + device1, Toast.LENGTH_SHORT).show();

                } else if (device.getAddress().equals(device1)) {
                    addData(rssi);
                    Toast.makeText(MainActivity.this, device1 + ": " + rssi, Toast.LENGTH_SHORT).show();
                }
            }

            mTimer1 = new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "#Runnable");
                    bluetoothAdapter.startDiscovery();
//                    handler.postDelayed(this, 200);
                }
            };
            handler.postDelayed(mTimer1, 1000);
        }};

    private void addData(short rssi) {
        Log.d(TAG, "#addData rssi = " + rssi);
        LineData data = chart.getData();

        ILineDataSet dataSet = data.getDataSetByIndex(0);
        if(dataSet == null){
            LineDataSet lineDataSet = new LineDataSet(null, "rssi data");
            lineDataSet.setColor(Color.RED);
            lineDataSet.setCircleColor(Color.RED);
            lineDataSet.setValueTextColor(Color.BLUE);
            lineDataSet.setHighlightEnabled(true);
            dataSet = lineDataSet;
            data.addDataSet(dataSet);
        }
        Log.d(TAG, "#addData new entry");
        int lastIndex = dataSet.getEntryCount();

        dataSet.addEntry(new Entry(lastIndex, (float) rssi));

        data.notifyDataChanged();
        chart.notifyDataSetChanged();
        chart.setVisibleXRangeMaximum(20.0f);

        chart.moveViewToX(dataSet.getEntryCount());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "#onActivityResult");
        if (requestCode == REQUEST_ENABLE_BT) {
            CheckBlueToothState();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        Log.d(TAG, "#onRequestPermissionsResult");
        switch (requestCode) {
            case REQUEST_ACCESS_COARSE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    boolean a = bluetoothAdapter.startDiscovery();
                    Toast.makeText(MainActivity.this, "Start discovery is " + a, Toast.LENGTH_SHORT).show();
                }
                else {
                    //exit application or do the needful
                }
                return;
            }
        }
    }
}
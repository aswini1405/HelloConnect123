package com.velolabs.helloconnect123;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.util.UUID;

public class MainActivity extends Activity {
    private Handler mHandler;
    private RelativeLayout rl_main = null;
    private Button bt_gatt = null;
    private TextView tv_connect = null;
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser = null;
    private AdvertiseCallback mAdvertiseCallback = null;
    private BluetoothGattServer mGattServer = null;
    private BluetoothManager mMnager = null;
    private BluetoothAdapter mAdapter = null;
    /** UUID **/
    public static final String UUID_SAMPLE_NAME_SERVICE = "000fefe-0000-1000-8000-00805f9b34fb";
    public static final String UUID_SAMPLE_NAME_CHARACTERISTIC = "0000ffee-0000-1000-8000-00805f9b34fb";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        initLayout();
        initBLE();
    }

    private void init() {
        mHandler = new Handler();
    }

    private void initLayout() {
        rl_main = (RelativeLayout) findViewById(R.id.rl_main);
        tv_connect = (TextView) findViewById(R.id.tv_connect);
        bt_gatt = (Button) findViewById(R.id.bt_gatt);
        bt_gatt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGattServer();
            }
        });
    }

    private void initBLE() {
        mMnager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mAdapter = mMnager.getAdapter();

        mBluetoothLeAdvertiser = mAdapter.getBluetoothLeAdvertiser();
        //mBLAdvertiser
        if (mBluetoothLeAdvertiser != null) {
            Log.d("OUT", "Not equal to NULL" + mBluetoothLeAdvertiser);
        } else {
            Log.d("OUT", "NULL" + mBluetoothLeAdvertiser);
            return;
        }

        mAdvertiseCallback = new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                super.onStartSuccess(settingsInEffect);
                Log.d("OUT", "onStartSuccess");
            }

            @Override
            public void onStartFailure(int errorCode) {
                super.onStartFailure(errorCode);
                Log.d("OUT", "onStartFailure " + errorCode);
            }
        };
    }

    /**
     * AdvertiseSettings
     */
    private static AdvertiseSettings createAdvSettings() {
        AdvertiseSettings.Builder builder = new AdvertiseSettings.Builder();
        /*
         * ADVERTISE_TX_POWER_ULTRA_LOW
         * ADVERTISE_TX_POWER_LOW
         * ADVERTISE_TX_POWER_MEDIUM
         * ADVERTISE_TX_POWER_HIGH
         */
        builder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH);
        builder.setConnectable(true);
        builder.setTimeout(0);
        /**
         * ADVERTISE_MODE_LOW_POWER
         * ADVERTISE_MODE_BALANCED
         * ADVERTISE_MODE_LOW_LATENCY
         */
        builder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED);
        return builder.build();
    }

    private static AdvertiseData createAdvData() {

        AdvertiseData.Builder builder = new AdvertiseData.Builder();

        // builder.addServiceUuid() ;
        builder.addServiceUuid(ParcelUuid.fromString(UUID_SAMPLE_NAME_SERVICE));

        /**
         * @param manufacturerId Manufacturer ID assigned by Bluetooth SIG.
         * https://www.bluetooth.org/en-us/specification/assigned-numbers/company-identifiers
         */
        /**@param manufacturerSpecificData Manufacturer specific data
         * Manufacturer Specific Data
         */

        byte mLeManufacturerData[] = {0x67, 0x00, 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08};
        byte mLeManufacturerData2[] = {(byte) 0x4C, (byte) 0x00, (byte) 0x02, (byte) 0x15, (byte) 0x15, (byte) 0x15, (byte) 0x15};
        builder.addManufacturerData(0x3103 + 1, mLeManufacturerData2);

//        builder.addManufacturerData(0x3103 + 1, new byte[]{
//                (byte) 0x4C, (byte) 0x00, (byte) 0x02, (byte) 0x15,
//
//                // UUID 16byte
//                // Mac uuidgen -- 8F3901A3-8A33-49DC-B113-A8AA39818898
//                (byte) 8F, (byte) 0x39, (byte) 0x01, (byte) 0xA3,
//                (byte) 0x8A, (byte) 0x33, (byte) 0x49, (byte) 0xDC,
//                (byte) 0xB1, (byte) 0x13, (byte) 0xA8, (byte) 0xAA,
//                (byte) 0x39, (byte) 0x81, (byte) 0x88, (byte) 0x98,
//
//                // major/minor
//                (byte) 0x00, (byte) 0x01,   // major
//                (byte) 0x00, (byte) 0x02,   // minor
//                // TxPower
//                (byte) -64,
//        });

        // Beacon Adv false
        builder.setIncludeTxPowerLevel(false);

        // name
        builder.setIncludeDeviceName(true);

        return builder.build();
    }

    private void reLayout(final Boolean type, final String address) {
        new Runnable() {
            @Override
            public void run() {
                if (!type) {
                    tv_connect.setText("Not type " + address);
                    rl_main.setBackgroundColor(Color.BLUE);
                } else {
                    tv_connect.setText("Type " + address);
                    rl_main.setBackgroundColor(Color.GREEN);
                }
            }
        };
    }

    private void startGattServer() {
        mGattServer = mMnager.openGattServer(getApplication(), new BluetoothGattServerCallback() {
            @Override
            public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
                super.onConnectionStateChange(device, status, newState);
                {
                    switch (newState) {
                        case BluetoothProfile.STATE_DISCONNECTED:
                            Log.d("OUT", device.getName() + " Fetch Name ");
                            reLayout(false, device.getAddress());
                            break;
                        case BluetoothProfile.STATE_CONNECTED:
                            Log.d("OUT", "device " + device);
                            Log.d("OUT", device.getName() + " Fetch Name ");
                            reLayout(true, device.getAddress());
                            break;
                    }
                }
            }

            @Override
            public void onServiceAdded(int status, BluetoothGattService service) {
                super.onServiceAdded(status, service);
            }

            /**A remote client has requested to read a local characteristic.*/
            @Override
            public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
                super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
            }

            @Override
            public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
                super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);

                mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, null);
            }

            @Override
            public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor) {
                super.onDescriptorReadRequest(device, requestId, offset, descriptor);
            }

            @Override
            public void onDescriptorWriteRequest(BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
                super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value);
            }

            @Override
            public void onExecuteWrite(BluetoothDevice device, int requestId, boolean execute) {
                super.onExecuteWrite(device, requestId, execute);
            }

            @Override
            public void onNotificationSent(BluetoothDevice device, int status) {
                super.onNotificationSent(device, status);
            }
        });

        BluetoothGattService nameService = new BluetoothGattService(UUID.fromString(UUID_SAMPLE_NAME_SERVICE), BluetoothGattService.SERVICE_TYPE_PRIMARY);
        BluetoothGattCharacteristic nameCharacteristic = new BluetoothGattCharacteristic(
                UUID.fromString(UUID_SAMPLE_NAME_CHARACTERISTIC),
                BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_READ | BluetoothGattCharacteristic.PERMISSION_WRITE
        );
        nameService.addCharacteristic(nameCharacteristic);
        mGattServer.addService(nameService);
        mBluetoothLeAdvertiser.startAdvertising(createAdvSettings(), createAdvData(), mAdvertiseCallback);
    }
}
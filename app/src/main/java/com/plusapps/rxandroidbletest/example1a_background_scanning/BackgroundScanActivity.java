package com.plusapps.rxandroidbletest.example1a_background_scanning;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;

import com.plusapps.rxandroidbletest.R;
import com.plusapps.rxandroidbletest.SampleApplication;
import com.plusapps.rxandroidbletest.util.LocationPermission;
import com.plusapps.rxandroidbletest.util.ScanExceptionHandler;
import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.exceptions.BleScanException;

import com.polidea.rxandroidble2.scan.ScanFilter;
import com.polidea.rxandroidble2.scan.ScanSettings;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class BackgroundScanActivity extends AppCompatActivity {

    private static final int SCAN_REQUEST_CODE = 42;
    private RxBleClient rxBleClient;
    private PendingIntent callbackIntent;
    private boolean hasClickedScan;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_example1a);
        ButterKnife.bind(this);
        rxBleClient = SampleApplication.getRxBleClient(this);
        callbackIntent = PendingIntent.getBroadcast(this, SCAN_REQUEST_CODE,
                new Intent(this, ScanReceiver.class), 0);
    }

    @OnClick(R.id.scan_start_btn)
    public void onScanStartClick() {
        hasClickedScan = true;
        if (LocationPermission.isLocationPermissionGranted(this)) {
            scanBleDeviceInBackground();
        } else {
            LocationPermission.requestLocationPermission(this);
        }
    }

    private void scanBleDeviceInBackground() {
        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            try {
                rxBleClient.getBackgroundScanner().scanBleDeviceInBackground(
                        callbackIntent,
                        new ScanSettings.Builder()
                                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                                .build(),
                        new ScanFilter.Builder()
                                .setDeviceAddress("5C:31:3E:BF:F7:34")
                                // add custom filters if needed
                                .build()
                );
            } catch (BleScanException scanException) {
                Log.w("BackgroundScanActivity", "Failed to start background scan", scanException);
                ScanExceptionHandler.handleException(this, scanException);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions,
            @NonNull final int[] grantResults) {
        if (LocationPermission.isRequestLocationPermissionGranted(requestCode, permissions, grantResults)
                && hasClickedScan) {
            hasClickedScan = false;
            scanBleDeviceInBackground();
        }
    }

    @OnClick(R.id.scan_stop_btn)
    public void onScanStopClick() {
        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            rxBleClient.getBackgroundScanner().stopBackgroundBleScan(callbackIntent);
        }
    }

}

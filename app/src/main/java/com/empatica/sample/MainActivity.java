package com.empatica.sample;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.DropBoxManager;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.empatica.empalink.ConnectionNotAllowedException;
import com.empatica.empalink.EmpaDeviceManager;
import com.empatica.empalink.EmpaticaDevice;
import com.empatica.empalink.config.EmpaSensorStatus;
import com.empatica.empalink.config.EmpaSensorType;
import com.empatica.empalink.config.EmpaStatus;
import com.empatica.empalink.delegate.EmpaDataDelegate;
import com.empatica.empalink.delegate.EmpaStatusDelegate;


import java.io.FileOutputStream;
import java.io.File;
import java.io.*;

import java.text.SimpleDateFormat;
import java.util.*;





public class MainActivity extends AppCompatActivity implements EmpaDataDelegate, EmpaStatusDelegate {

    //private static final String TAG = "MainActivity";
    //rivate static final String ACCESS_TOKEN = "lY_d3DAmzgAAAAAAAAAAdn7IAKZUPyYJXhaYmllRlCFZwhYgt_m6fNafXq8DcgWK";

    private static final int REQUEST_ENABLE_BT = 1;

    private static final int REQUEST_PERMISSION_ACCESS_COARSE_LOCATION = 1;

    private static final int REQUEST_PERMISSION_ACCESS_STORAGE = 2;

    private static final int REQUEST_PERMISSION = 3;

    // TODO insert your API Key here
    private static final String EMPATICA_API_KEY = "53f2dd8a88424021ad82b35c2f6cf3b4";

    private EmpaDeviceManager deviceManager = null;

    private TextView accel_xLabel;

    private TextView accel_yLabel;

    private TextView accel_zLabel;

    private TextView bvpLabel;

    private TextView edaLabel;

    private TextView ibiLabel;

    private TextView temperatureLabel;

    private TextView batteryLabel;

    private TextView statusLabel;

    private TextView deviceNameLabel;

    private LinearLayout dataCnt;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        /**
         * Permissions
         */
        if (checkAndRequestPermissions()) {
            // carry on the normal flow, as the case of  permissions  granted.
        }


        /**
         * Initialize vars that reference UI components E4
         */
        statusLabel = (TextView) findViewById(R.id.status);

        dataCnt = (LinearLayout) findViewById(R.id.dataArea);

        accel_xLabel = (TextView) findViewById(R.id.accel_x);

        accel_yLabel = (TextView) findViewById(R.id.accel_y);

        accel_zLabel = (TextView) findViewById(R.id.accel_z);

        bvpLabel = (TextView) findViewById(R.id.bvp);

        edaLabel = (TextView) findViewById(R.id.eda);

        ibiLabel = (TextView) findViewById(R.id.ibi);

        temperatureLabel = (TextView) findViewById(R.id.temperature);

        batteryLabel = (TextView) findViewById(R.id.battery);

        deviceNameLabel = (TextView) findViewById(R.id.deviceName);

        final Button disconnectButton = findViewById(R.id.disconnectButton);
        disconnectButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (deviceManager != null) {

                    deviceManager.disconnect();
                }
            }

        });

        initEmpaticaDeviceManager();
    }

    /**
     * Permissions Request
     */

    String[] permissions = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    private boolean checkAndRequestPermissions() {
        int result;
        List<String> mPermissionList = new ArrayList<>();
        for (String p : permissions) {
            result = ContextCompat.checkSelfPermission(this, p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(p);
            }
        }
        if (!mPermissionList.isEmpty()) {
            ActivityCompat.requestPermissions(this, mPermissionList.toArray(new String[mPermissionList.size()]), REQUEST_PERMISSION);
            return false;
        }
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissionsList[], @NonNull int[] grantResults) {
        switch (requestCode) {
            //STORAGE
            case REQUEST_PERMISSION:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission was granted, yay!
                    initEmpaticaDeviceManager();
                } else {
                    // Permission denied, boo!
                    final boolean needRationale1 = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    new AlertDialog.Builder(this)
                            .setTitle("Permission required")
                            .setMessage("Without this permission application cannot be started, allow it in order to start the sppllication.")
                            .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // try again
                                    if (needRationale1) {
                                        // the "never ask again" flash is not set, try again with permission request
                                        initEmpaticaDeviceManager();
                                    } else {
                                        // the "never ask again" flag is set so the permission requests is disabled, try open app settings to enable the permission
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                                        intent.setData(uri);
                                        startActivity(intent);
                                    }
                                }
                            })
                            .setNegativeButton("Exit application", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // without permission exit is the only way
                                    finish();
                                }
                            })
                            .show();
                }
                break;
        }
            /*
            //LOCATION
            case REQUEST_PERMISSION_ACCESS_COARSE_LOCATION:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission was granted, yay!
                    initEmpaticaDeviceManager();
                } else {
                    // Permission denied, boo!
                    final boolean needRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION);
                    new AlertDialog.Builder(this)
                            .setTitle("Permission required")
                            .setMessage("Without this permission bluetooth low energy devices cannot be found, allow it in order to connect to the device.")
                            .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // try again
                                    if (needRationale) {
                                        // the "never ask again" flash is not set, try again with permission request
                                        initEmpaticaDeviceManager();
                                    } else {
                                        // the "never ask again" flag is set so the permission requests is disabled, try open app settings to enable the permission
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                                        intent.setData(uri);
                                        startActivity(intent);
                                    }
                                }
                            })
                            .setNegativeButton("Exit application", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // without permission exit is the only way
                                    finish();
                                }
                            })
                            .show();
                }
                break;

            //STORAGE
            case REQUEST_PERMISSION_ACCESS_STORAGE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission was granted, yay!
                    initEmpaticaDeviceManager();
                } else {
                    // Permission denied, boo!
                    final boolean needRationale1 = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    new AlertDialog.Builder(this)
                            .setTitle("Permission required")
                            .setMessage("Without this permission storage cannot be accessed, allow it in order to access the storage.")
                            .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // try again
                                    if (needRationale1) {
                                        // the "never ask again" flash is not set, try again with permission request
                                        initEmpaticaDeviceManager();
                                    } else {
                                        // the "never ask again" flag is set so the permission requests is disabled, try open app settings to enable the permission
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                                        intent.setData(uri);
                                        startActivity(intent);
                                    }
                                }
                            })
                            .setNegativeButton("Exit application", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // without permission exit is the only way
                                    finish();
                                }
                            })
                            .show();
                }
                break;
        }
        */

    }


    private void initEmpaticaDeviceManager() {
        // Android 6 (API level 23) now require ACCESS_COARSE_LOCATION permission to use BLE
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSION_ACCESS_COARSE_LOCATION);
        } else {

            if (TextUtils.isEmpty(EMPATICA_API_KEY)) {
                new AlertDialog.Builder(this)
                        .setTitle("Warning")
                        .setMessage("Please insert your API KEY")
                        .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // without permission exit is the only way
                                finish();
                            }
                        })
                        .show();
                return;
            }

            // Create a new EmpaDeviceManager. MainActivity is both its data and status delegate.
            deviceManager = new EmpaDeviceManager(getApplicationContext(), this, this);

            // Initialize the Device Manager using your API key. You need to have Internet access at this point.
            deviceManager.authenticateWithAPIKey(EMPATICA_API_KEY);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (deviceManager != null) {
            deviceManager.stopScanning();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (deviceManager != null) {
            deviceManager.cleanUp();
        }
    }

    @Override
    public void didDiscoverDevice(EmpaticaDevice bluetoothDevice, String deviceName, int rssi, boolean allowed) {
        // Check if the discovered device can be used with your API key. If allowed is always false,
        // the device is not linked with your API key. Please check your developer area at
        // https://www.empatica.com/connect/developer.php
        if (allowed) {
            // Stop scanning. The first allowed device will do.
            deviceManager.stopScanning();
            try {
                // Connect to the device
                deviceManager.connectDevice(bluetoothDevice);
                updateLabel(deviceNameLabel, "To: " + deviceName);
            } catch (ConnectionNotAllowedException e) {
                // This should happen only if you try to connect when allowed == false.
                Toast.makeText(MainActivity.this, "Sorry, you can't connect to this device", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void didRequestEnableBluetooth() {
        // Request the user to enable Bluetooth
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // The user chose not to enable Bluetooth
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            // You should deal with this
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    public void didUpdateSensorStatus(@EmpaSensorStatus int status, EmpaSensorType type) {

        didUpdateOnWristStatus(status);
    }


    /**
     * Control UI accoring to the status to BLE connection
     */

    @Override
    public void didUpdateStatus(EmpaStatus status) {
        // Update the UI
        updateLabel(statusLabel, status.name());

        // The device manager is ready for use
        if (status == EmpaStatus.READY) {
            updateLabel(statusLabel, status.name() + " - Turn on your device");
            // Start scanning
            deviceManager.startScanning();

            // The device manager has established a connection
            hide();

        } else if (status == EmpaStatus.CONNECTED) {

            show();

            // The device manager disconnected from a device
        } else if (status == EmpaStatus.DISCONNECTED) {

            updateLabel(deviceNameLabel, "Waiting for a new device");

            hide();
        }
    }

    /**
     * ACC
     */
    @Override
    public void didReceiveAcceleration(int x, int y, int z, double timestamp) {
        updateLabel(accel_xLabel, "" + x);
        updateLabel(accel_yLabel, "" + y);
        updateLabel(accel_zLabel, "" + z);
    }

    /**
     * BVP
     */
    @Override
    public void didReceiveBVP(float bvp, double timestamp) {
        updateLabel(bvpLabel, "" + bvp);
    }

    /**
     * EDA
     */

    @Override
    public void didReceiveGSR(float gsr, double timestamp) {
        updateLabel(edaLabel, "" + gsr);
    }

    /**
     * IBI
     */
    @Override
    public void didReceiveIBI(float ibi, double timestamp) {
        updateLabel(ibiLabel, "" + ibi);
        float saveData = ibi;
        // save IBI data to the local memory
        save(saveData);
    }

    /**
     * TEMP
     */
    @Override
    public void didReceiveTemperature(float temp, double timestamp) {
        updateLabel(temperatureLabel, "" + temp);
    }

    /**
     * Update a label with some text, making sure this is run in the UI thread
     */
    private void updateLabel(final TextView label, final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                label.setText(text);
            }
        });
    }

    /**
     * Battery
     */
    @Override
    public void didReceiveBatteryLevel(float battery, double timestamp) {
        updateLabel(batteryLabel, String.format("%.0f %%", battery * 100));
        System.out.println("String.valueOf(battery * 100)");
    }

    /**
     * Tag
     */
    @Override
    public void didReceiveTag(double timestamp) {

    }

    @Override
    public void didEstablishConnection() {

        show();
    }

    @Override
    public void didUpdateOnWristStatus(@EmpaSensorStatus final int status) {

        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                if (status == EmpaSensorStatus.ON_WRIST) {

                    ((TextView) findViewById(R.id.wrist_status_label)).setText("ON WRIST");
                } else {

                    ((TextView) findViewById(R.id.wrist_status_label)).setText("NOT ON WRIST");
                }
            }
        });
    }

    void show() {

        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                dataCnt.setVisibility(View.VISIBLE);
                // dataCnt IS A LINEAR lAYOUT
            }
        });
    }

    void hide() {
        // textView and disconnect button invisible
        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                dataCnt.setVisibility(View.INVISIBLE);
            }
        });
    }

    /**
     * Save file at internal storage -> Empa
     *
     * @param saveData
     */
    void save(float saveData) {
        try {
            File directory = new File(Environment.getExternalStorageDirectory().getPath() + "/Empa");
            if (!directory.exists()) {
                directory.mkdir();
            }
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH:mm");
            String time = format.format(new Date(System.currentTimeMillis()));
            String fileName = "IBIData" + time + ".txt";
            //String file_path = Environment.getExternalStorageDirectory().getPath() + "/Empa/" + fileName;
            String file_path =  Environment.getExternalStorageDirectory().getPath()+"/Android/data/com.empatica.sample/" + fileName;
            File file = new File(file_path);
            //file.setExecutable(true);
            if (!file.exists()) {
                File dir = new File(file.getParent());
                dir.mkdirs();
                file.createNewFile();
            }

            FileOutputStream outStream = new FileOutputStream(file, true);
            outStream.write(String.valueOf(saveData).getBytes());
            outStream.close();
            System.out.println("New Data Saved");


            /**
             * Call Upload()
             *//*
            Intent intent = new Intent(MainActivity.this,Upload.class);
            startActivity(intent);
            System.out.println("New File Uploaded");*/

        } catch (Exception e) {
            e.printStackTrace();
        }


    }
/*
    void uploadFile(String file_path, String fileName) {
        AndroidAuthSession session;

        initDropBox();

        DropBoxManager.Entry response;

        writeFileContent(file_path);
        File file = new File(file_path);
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
        } catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }


        try {
            response = mDBApi.putFile("/my_file.txt", inputStream,
                    file.length(), null, null);
            Log.i("DbExampleLog", "The uploaded file's rev is: " + response.rev);
        } catch (DropboxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

        }

    }


    void initDropBox() {

        // Create Dropbox client
        DbxRequestConfig config = DbxRequestConfig.newBuilder("dropbox/Apps/E4 Link").build();
        DbxClientV2 client = new DbxClientV2(config, ACCESS_TOKEN);
        return client;
        // Get current account info

        AppKeyPair appKeys = new AppKeyPair(55bv7menhh43eth, uyh8y3vm4yr5uii);
        session = new AndroidAuthSession(appKeys);
        mDBApi = new DropboxAPI<AndroidAuthSession>(session);
        mDBApi.getSession().startOAuth2Authentication(MainActivity.this);

    }

*/
/*
    @Override
    public void createFolder(String parentPath, String newDirName) throws Exception {
        File body = new File();
        body.setName(newDirName);
        body.setMimeType(FOLDER_MIME_TYPE);

        GDrivePath parentGdrivePath = new GDrivePath(parentPath);

        body.setParents(
                Arrays.asList(new ParentReference().setId(parentGdrivePath.getGDriveId())));
        try
        {
            File file = getDriveService(parentGdrivePath.getAccount()).files().insert(body).execute();

            logDebug("created folder "+newDirName+" in "+parentPath+". id: "+file.getId());

            return new GDrivePath(parentPath, file).getFullPath();
        }
        catch (Exception e)
        {
            throw convertException(e);
        }

    }
    */






/*
    void uploadtoDrive(File file, String fileName) {

        File fileMetadata = new File(file);
        fileMetadata.setName("photo.jpg");
        java.io.File filePath = new java.io.File("files/photo.jpg");
        FileContent mediaContent = new FileContent("image/jpeg", filePath);
        File drivefile = drive.files().create(fileMetadata, mediaContent)
                .setFields("id")
                .execute();
        System.out.println("File ID: " + (drivefile.getId());
        /**
         * Method 1
         */
        /*
        Intent inent = new Intent(MainActivity.this,MyDriveUpload.class);

        startActivity(inent);
        */
    /**
     *Method 2
     */
            /*
            try {
                String folderId = "0BwwA4oUTeiV1TGRPeTVjaWRDY1E";
                File fileMetadata = new File(file);
                fileMetadata.setName("photo.jpg");
                fileMetadata.setParents(Collections.singletonList(folderId));
                java.io.File filePath = new java.io.File("files/photo.jpg");
                FileContent mediaContent = new FileContent("txt", filePath);
                File file = drive.files().create(fileMetadata, mediaContent)
                        .setFields("id")
                        .execute();
                System.out.println("File ID: " + file.getId());

                // File's metadata.
                InputStream is = new BufferedInputStream(new FileInputStream(file));
                file.setmimetype(URLConnection.guessContentTypeFromStream(is));

                //TODO: Upload to specific folder!
                // File's content.
                final FileContent mediaContent = new FileContent(URLConnection.guessContentTypeFromStream(is), file);
                new Thread() {
                    public void run() {
                        try {
                            File uploadedFile = DriveFiles.getDriveFileInstance().getDrive_files().insert(body, mediaContent).execute();
                            closeActionHandlerDialog();
                            new RefreshAction().updateAction(context);
                        } catch (Exception ie) {
                            closeActionHandlerDialog();
                        }
                    }
                }.start();
            } catch (Exception e) {
                closeActionHandlerDialog();
            }
        }
        */


    /**
     *  Method 3
     */
/*
        Blob file = new Blob(String.valueOf(saveData),{type: 'text/plain'});
        var metadata = {
                'name': fileName, // Filename at Google Drive
                'mimeType': 'text/plain', // mimeType at Google Drive
                'parents': ['### folder ID ###'], // Folder ID at Google Drive
};

        var accessToken = gapi.auth.getToken().access_token; // Here gapi is used for retrieving the access token.
        var form = new FormData();
        form.append('metadata', new Blob([JSON.stringify(metadata)], {type: 'application/json'}));
        form.append('file', file);

        XMLHttpRequest xhr = new XMLHttpRequest();
        xhr.open('post', 'https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart&fields=id');
        xhr.setRequestHeader('Authorization', 'Bearer ' + accessToken);
        xhr.responseType = 'json';
        xhr.onload = () => {
            console.log(xhr.response.id); // Retrieve uploaded file ID.
        };
        xhr.send(form);
    */


}



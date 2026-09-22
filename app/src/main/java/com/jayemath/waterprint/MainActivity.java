package com.jayemath.waterprint;

import android.Manifest;
import android.app.*;
import android.bluetooth.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.*;
import android.webkit.*;
import android.widget.ImageView;
import android.graphics.Color;
import android.util.Base64;
import org.json.JSONObject;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class MainActivity extends Activity {

    WebView web;
    ImageView cover;
    BluetoothSocket socket;
    OutputStream out;

    final UUID SPP_UUID =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    final ExecutorService io =
            Executors.newSingleThreadExecutor();

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        requestBt();
        showCoverThenWeb();
    }

    void requestBt() {
        if (Build.VERSION.SDK_INT >= 31 &&
                checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)
                        != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.BLUETOOTH_CONNECT
                    },
                    41
            );
        }
    }

    void showCoverThenWeb() {
        cover = new ImageView(this);
        cover.setBackgroundColor(Color.rgb(6, 29, 69));
        cover.setScaleType(ImageView.ScaleType.CENTER_CROP);

        try {
            cover.setImageBitmap(
                    android.graphics.BitmapFactory.decodeStream(
                            getAssets().open("cover-768.png")
                    )
            );
        } catch (Exception ignored) {}

        setContentView(cover);

        new Handler(Looper.getMainLooper())
                .postDelayed(() -> setupWeb(), 1200);
    }

    void setupWeb() {

        web = new WebView(this);

        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setDomStorageEnabled(true);
        web.getSettings().setAllowFileAccess(true);
        web.getSettings().setAllowContentAccess(true);

        web.setWebViewClient(new WebViewClient());

        web.addJavascriptInterface(
                new Bridge(),
                "AndroidPrinter"
        );

        setContentView(web);

        web.loadUrl("file:///android_asset/index.html");
    }

    void status(String s) {

        runOnUiThread(() -> {

            if (web != null) {

                web.evaluateJavascript(
                        "window.onNativePrinterStatus && " +
                        "window.onNativePrinterStatus(" +
                        JSONObject.quote(s) +
                        ")",
                        null
                );
            }
        });
    }

    boolean btOk() {

        if (Build.VERSION.SDK_INT >= 31) {

            return checkSelfPermission(
                    Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED;
        }

        return true;
    }

    class Bridge {

        @JavascriptInterface
        public boolean isAvailable() {

            return btOk() &&
                    BluetoothAdapter.getDefaultAdapter() != null;
        }

        @JavascriptInterface
        public void chooseAndConnect() {

            if (!btOk()) {
                requestBt();
                status("Bluetooth permission required");
                return;
            }

            BluetoothAdapter adapter =
                    BluetoothAdapter.getDefaultAdapter();

            if (adapter == null) {
                status("Bluetooth not available");
                return;
            }

            if (!adapter.isEnabled()) {

                try {

                    startActivity(
                            new Intent(
                                    BluetoothAdapter.ACTION_REQUEST_ENABLE
                            )
                    );

                    status(
                            "Turn Bluetooth ON and press Connect again."
                    );

                } catch (Exception e) {

                    status(
                            "Please turn Bluetooth ON in Android Settings."
                    );
                }

                return;
            }

            try {
                adapter.cancelDiscovery();
            } catch (Exception ignored) {}

            Set<BluetoothDevice> paired;

            try {

                paired = adapter.getBondedDevices();

            } catch (SecurityException e) {

                status("Bluetooth permission denied");
                return;
            }

            if (paired == null || paired.isEmpty()) {

                status(
                        "No paired printer found. " +
                        "Pair P301A-C374 first."
                );

                return;
            }

            ArrayList<BluetoothDevice> printers =
                    new ArrayList<>(paired);

            String[] names =
                    new String[printers.size()];

            for (int i = 0; i < printers.size(); i++) {

                BluetoothDevice d = printers.get(i);

                String name;

                try {
                    name = d.getName();
                } catch (Exception e) {
                    name = "Bluetooth Printer";
                }

                if (name == null || name.trim().isEmpty()) {
                    name = "Bluetooth Printer";
                }

                names[i] =
                        name +
                        "\n" +
                        d.getAddress();
            }

            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Select Bluetooth Printer")
                    .setItems(
                            names,
                            (dialog, which) ->
                                    connect(printers.get(which))
                    )
                    .setNegativeButton(
                            "Cancel",
                            null
                    )
                    .show();
        }

        @JavascriptInterface
        public void disconnect() {

            closeSocket();
            status("Disconnected");
        }

        @JavascriptInterface
        public void printBase64(String b64) {

            io.execute(() -> {

                try {

                    if (socket == null ||
                            !socket.isConnected() ||
                            out == null) {

                        throw new IOException(
                                "Printer not connected"
                        );
                    }

                    byte[] data =
                            Base64.decode(
                                    b64,
                                    Base64.DEFAULT
                            );

                    status("Sending to printer...");

                    final int CHUNK = 256;

                    for (
                            int p = 0;
                            p < data.length;
                            p += CHUNK
                    ) {

                        int n =
                                Math.min(
                                        CHUNK,
                                        data.length - p
                                );

                        out.write(
                                data,
                                p,
                                n
                        );

                        out.flush();

                        if (p + n < data.length) {

                            try {

                                Thread.sleep(40);

                            } catch (InterruptedException e) {

                                Thread.currentThread().interrupt();
                                throw e;
                            }
                        }
                    }

                    try {

                        Thread.sleep(300);

                    } catch (InterruptedException e) {

                        Thread.currentThread().interrupt();
                        throw e;
                    }

                    status("Printed successfully");

                } catch (Exception e) {

                    String msg = e.getMessage();

                    if (msg == null || msg.trim().isEmpty()) {
                        msg = "Bluetooth print error";
                    }

                    status("Print failed: " + msg);

                    closeSocket();
                }
            });
        }
    }

    void connect(BluetoothDevice device) {

        io.execute(() -> {

            try {

                closeSocket();

                String name;

                try {
                    name = device.getName();
                } catch (Exception e) {
                    name = "Bluetooth Printer";
                }

                if (name == null) {
                    name = "Bluetooth Printer";
                }

                status("Connecting to " + name + "...");

                /*
                 * Stop discovery before RFCOMM connection.
                 */
                try {
                    BluetoothAdapter
                            .getDefaultAdapter()
                            .cancelDiscovery();
                } catch (Exception ignored) {}

                /*
                 * Method 1:
                 * Normal secure SPP connection.
                 */
                try {

                    socket =
                            device.createRfcommSocketToServiceRecord(
                                    SPP_UUID
                            );

                    socket.connect();

                } catch (Exception first) {

                    closeSocket();

                    /*
                     * Method 2:
                     * Insecure RFCOMM.
                     */
                    try {

                        java.lang.reflect.Method method =
                                device.getClass()
                                        .getMethod(
                                                "createInsecureRfcommSocketToServiceRecord",
                                                UUID.class
                                        );

                        socket =
                                (BluetoothSocket)
                                        method.invoke(
                                                device,
                                                SPP_UUID
                                        );

                        socket.connect();

                    } catch (Exception second) {

                        closeSocket();

                        /*
                         * Method 3:
                         * Channel 1.
                         * Many cheap XP-P301A type
                         * printers use RFCOMM channel 1.
                         */
                        try {

                            java.lang.reflect.Method method =
                                    device.getClass()
                                            .getMethod(
                                                    "createRfcommSocket",
                                                    int.class
                                            );

                            socket =
                                    (BluetoothSocket)
                                            method.invoke(
                                                    device,
                                                    1
                                            );

                            socket.connect();

                        } catch (Exception third) {

                            closeSocket();

                            throw new IOException(
                                    "Unable to connect to " +
                                    name
                            );
                        }
                    }
                }

                out = socket.getOutputStream();

                status(
                        "Connected: " +
                        name
                );

            } catch (Exception e) {

                closeSocket();

                String msg = e.getMessage();

                if (msg == null || msg.trim().isEmpty()) {
                    msg = "Bluetooth printer connection failed";
                }

                status(
                        "Connection failed: " +
                        msg
                );
            }
        });
    }

    void closeSocket() {

        try {

            if (out != null) {
                out.flush();
                out.close();
            }

        } catch (Exception ignored) {}

        try {

            if (socket != null) {
                socket.close();
            }

        } catch (Exception ignored) {}

        out = null;
        socket = null;
    }

    @Override
    protected void onDestroy() {

        closeSocket();

        io.shutdownNow();

        super.onDestroy();
    }
}

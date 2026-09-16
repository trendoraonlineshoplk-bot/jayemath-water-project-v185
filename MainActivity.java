package com.jayemath.waterprint;

import android.Manifest;
import android.app.*;
import android.bluetooth.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.*;
import android.webkit.*;
import android.widget.Toast;
import android.widget.ImageView;
import android.graphics.Color;
import android.view.ViewGroup;
import android.util.Base64;
import org.json.JSONObject;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class MainActivity extends Activity {
    WebView web;
    ImageView cover; BluetoothSocket socket; OutputStream out; final UUID SPP_UUID=UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    final ExecutorService io=Executors.newSingleThreadExecutor();
    @Override public void onCreate(Bundle b){super.onCreate(b); requestBt(); showCoverThenWeb();}
    void requestBt(){
        if(Build.VERSION.SDK_INT>=31 && checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)!=PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.BLUETOOTH_SCAN,Manifest.permission.BLUETOOTH_CONNECT},41);
    }
    void showCoverThenWeb(){
        cover=new ImageView(this);
        cover.setBackgroundColor(Color.rgb(6,29,69));
        cover.setScaleType(ImageView.ScaleType.CENTER_CROP);
        try{cover.setImageBitmap(android.graphics.BitmapFactory.decodeStream(getAssets().open("cover-768.png")));}catch(Exception ignored){}
        setContentView(cover);
        new Handler(Looper.getMainLooper()).postDelayed(()->setupWeb(),1200);
    }
    void setupWeb(){ web=new WebView(this); web.getSettings().setJavaScriptEnabled(true); web.getSettings().setDomStorageEnabled(true); web.setWebViewClient(new WebViewClient()); web.addJavascriptInterface(new Bridge(),"AndroidPrinter"); setContentView(web); web.loadUrl("file:///android_asset/index.html"); }
    void status(String s){ runOnUiThread(()->{ if(web!=null) web.evaluateJavascript("window.onNativePrinterStatus && window.onNativePrinterStatus("+JSONObject.quote(s)+")",null); }); }
    boolean btOk(){ if(Build.VERSION.SDK_INT>=31) return checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)==PackageManager.PERMISSION_GRANTED; return true; }
    class Bridge {
      @JavascriptInterface public boolean isAvailable(){return btOk() && BluetoothAdapter.getDefaultAdapter()!=null;}
      @JavascriptInterface public void chooseAndConnect(){
        if(!btOk()){requestBt(); return;}
        BluetoothAdapter a=BluetoothAdapter.getDefaultAdapter();
        if(a==null){status("Bluetooth not available");return;}
        if(!a.isEnabled()){
          try{ startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)); status("Please turn Bluetooth ON, then tap Connect again."); }
          catch(Exception e){ status("Please turn Bluetooth ON in Android settings."); }
          return;
        }
        Set<BluetoothDevice> ds=a.getBondedDevices();
        if(ds==null||ds.isEmpty()){status("No paired Bluetooth printer found. Pair XP-P301A in Android Bluetooth settings first.");return;}
        ArrayList<BluetoothDevice> list=new ArrayList<>(ds);
        String[] names=new String[list.size()];
        for(int i=0;i<list.size();i++){BluetoothDevice d=list.get(i); names[i]=(d.getName()==null?"Bluetooth device":d.getName())+"\n"+d.getAddress();}
        new AlertDialog.Builder(MainActivity.this).setTitle("Select Bluetooth Printer").setItems(names,(d,w)->connect(list.get(w))).setNegativeButton("Cancel",null).show();
      }
      @JavascriptInterface public void disconnect(){closeSocket(); status("Disconnected");}
      @JavascriptInterface public void printBase64(String b64){
        io.execute(()->{try{if(socket==null||!socket.isConnected())throw new IOException("Printer not connected"); byte[] data=Base64.decode(b64,Base64.DEFAULT); out.write(data);out.flush();status("Printed successfully");}catch(Exception e){status("Print failed: "+e.getMessage());closeSocket();}});
      }
    }
    void connect(BluetoothDevice d){ io.execute(()->{
      try{
        closeSocket(); status("Connecting to "+d.getName()+"...");
        try{
          socket=d.createRfcommSocketToServiceRecord(SPP_UUID);
          socket.connect();
        }catch(Exception first){
          closeSocket();
          // Some Classic SPP receipt printers work only with the insecure RFCOMM channel.
          try{
            java.lang.reflect.Method m=d.getClass().getMethod("createInsecureRfcommSocketToServiceRecord",UUID.class);
            socket=(BluetoothSocket)m.invoke(d,SPP_UUID);
            socket.connect();
          }catch(Exception second){ throw first; }
        }
        out=socket.getOutputStream(); status("Connected: "+d.getName());
      }catch(Exception e){closeSocket();status("Connection failed: "+(e.getMessage()==null?"Bluetooth printer connection failed":e.getMessage()));}
    }); }
    void closeSocket(){try{if(out!=null)out.close();}catch(Exception ignored){}try{if(socket!=null)socket.close();}catch(Exception ignored){}out=null;socket=null;}
    @Override protected void onDestroy(){closeSocket();io.shutdownNow();super.onDestroy();}
}

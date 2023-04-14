package com.github.rmtmckenzie.native_device_orientation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.WindowManager;
import android.provider.Settings;
import android.content.Context;
import androidx.annotation.NonNull;
import java.util.Map;
import io.flutter.Log;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.Result;
import android.database.ContentObserver;
import android.os.Handler;


/** NativeDeviceOrientationPlugin */
public class NativeDeviceOrientationPlugin implements FlutterPlugin {

  private static final String METHOD_CHANEL = "com.github.rmtmckenzie/flutter_native_device_orientation/orientation";
  private static final String EVENT_CHANNEL = "com.github.rmtmckenzie/flutter_native_device_orientation/orientationevent";

  /// these deal with communicating with flutter
  private MethodChannel channel;
  private final MethodCallHandler methodCallHandler = new MethodCallHandler();
  private EventChannel eventChannel;
  private final StreamHandler streamHandler = new StreamHandler();

  private Context context;

  private OrientationReader reader;
  private IOrientationListener listener;

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    channel =  new MethodChannel(flutterPluginBinding.getBinaryMessenger(), METHOD_CHANEL);
    channel.setMethodCallHandler(methodCallHandler);

    eventChannel  = new EventChannel(flutterPluginBinding.getBinaryMessenger(), EVENT_CHANNEL);
    eventChannel.setStreamHandler(streamHandler);

    context = flutterPluginBinding.getApplicationContext();
    reader = new OrientationReader(context);
  }


  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
    eventChannel.setStreamHandler(null);
  }

  class MethodCallHandler implements MethodChannel.MethodCallHandler {
    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull final Result result) {
      switch (call.method) {
        case "getOrientation":
          Boolean useSensor = call.argument("useSensor");
          result.success(reader.getOrientation().name());
          break;

        case "pause":
          // if a listener is currently active, stop listening. The app is going to the background
          if(listener != null){
            listener.stopOrientationListener();
          }
          result.success(null);
          break;

        case "resume":
          // start listening for orientation changes again. The app is in the foreground.
          if(listener != null){
            listener.startOrientationListener();
          }
          result.success(null);
          break;
        default:
          result.notImplemented();
      }
    }
  }

  class StreamHandler implements EventChannel.StreamHandler {
    @Override
    public void onListen(Object parameters, final EventChannel.EventSink eventSink) {
   
      // initialize the callback. It is the same for both listeners.
      IOrientationListener.OrientationCallback callback = new IOrientationListener.OrientationCallback() {
        @Override
        public void receive(NativeOrientation orientation) {
          eventSink.success(orientation.name());
        }
      };

     
      listener = new OrientationListener(reader, context, callback);
      listener.startOrientationListener();
    }

    @Override
    public void onCancel(Object arguments) {
      listener.stopOrientationListener();
      listener = null;
    }
  }
}

enum NativeOrientation {
    PortraitUp,
    PortraitDown,
    LandscapeLeft,
    LandscapeRight,
    Unknown
}


 interface IOrientationListener {

    interface OrientationCallback {
        void receive(NativeOrientation orientation);
    }

    void startOrientationListener();

    void stopOrientationListener();
}


 class OrientationListener implements IOrientationListener {

    private final OrientationReader reader;
    private final Context context;
    private final OrientationCallback callback;
    private ContentObserver contentObserver;
    private NativeOrientation lastOrientation = null;

    public OrientationListener(OrientationReader reader, Context context, OrientationCallback callback) {
        this.reader = reader;
        this.context = context;
        this.callback = callback;
    }

    public void startOrientationListener() {
        if (contentObserver != null) return;

        contentObserver = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                NativeOrientation orientation = reader.getOrientation();
                if (!orientation.equals(lastOrientation)) {
                    lastOrientation = orientation;
                    callback.receive(orientation);
                }
            }
        };

        context.getContentResolver().registerContentObserver(Settings.System.getUriFor(Settings.System.ACCELEROMETER_ROTATION), true, contentObserver);

        lastOrientation = reader.getOrientation();
        // send initial orientation.
        callback.receive(lastOrientation);
    }

    public void stopOrientationListener() {
        if (contentObserver == null) return;
        context.getContentResolver().unregisterContentObserver(contentObserver);
        contentObserver = null;
    }

}


 class OrientationReader {

    public OrientationReader(Context context) {
        this.context = context;
    }

    private final Context context;

    @SuppressLint("SwitchIntDef")
    public NativeOrientation getOrientation() {
        final int canAutoRotate = android.provider.Settings.System.getInt(context.getContentResolver(), android.provider.Settings.System.ACCELEROMETER_ROTATION, 0);
        if (canAutoRotate == 1) {
            return NativeOrientation.LandscapeLeft;
        } else {
            return NativeOrientation.LandscapeRight;
        }
    }
}

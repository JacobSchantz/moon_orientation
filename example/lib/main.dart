import 'package:flutter/material.dart';
import 'package:native_device_orientation/native_device_orientation.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  @override
  Widget build(BuildContext context) {
    final stream = NativeDeviceOrientationCommunicator().onOrientationChanged(useSensor: false);

    final streamBuilder = StreamBuilder(
      stream: stream,
      builder: (BuildContext context, AsyncSnapshot<NativeDeviceOrientation> snapshot) {
        if (!snapshot.hasData) {
          return Center(
            child: Text('Loading...'),
          );
        }

        final autoRotateEnabled = snapshot.data == NativeDeviceOrientation.landscapeLeft;

        return Center(
          child: Text(
            'AutoRotateEnabled: $autoRotateEnabled',
            style: TextStyle(fontSize: 20),
          ),
        );
      },
    );

    // final future = NativeDeviceOrientationCommunicator().orientation(useSensor: false);

    // final futureBuilder = FutureBuilder(
    //   future: future,
    //   builder: (BuildContext context, AsyncSnapshot<NativeDeviceOrientation> snapshot) {
    //     if (!snapshot.hasData) {
    //       return Center(
    //         child: Text('Loading...'),
    //       );
    //     }

    //     final autoRotateEnabled = snapshot.data == NativeDeviceOrientation.landscapeLeft;

    //     return Center(
    //       child: Text(
    //         'AutoRotateEnabled: $autoRotateEnabled',
    //         style: TextStyle(fontSize: 20),
    //       ),
    //     );
    //   },
    // );

    return MaterialApp(
      home: Scaffold(
        body: Padding(
          padding: const EdgeInsets.all(24.0),
          child: streamBuilder,
          // child: f,
        ),
      ),
    );
  }
}

import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:receive_intent/receive_intent.dart';

void main() {
  const MethodChannel channel = MethodChannel('receive_intent');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await ReceiveIntent.platformVersion, '42');
  });
}

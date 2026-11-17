# المعمارية التقنية

- OPPO Android: واجهة المشاهدة ومستقبل WebRTC وإدارة الاقتران.
- iPhone: Swift + ReplayKit Broadcast Upload Extension + مرسل WebRTC.
- Signaling: Node.js/WebSocket لتبادل SDP وICE.
- Media: WebRTC مع DTLS-SRTP.
- NAT traversal: TURN ببيانات اعتماد مؤقتة.
- Identity: Keychain وAndroid Keystore.
- Consent: موافقة صاحب iPhone على الاقتران وبدء مشاركة الشاشة.

## استعادة الاتصال
إذا انقطع الإنترنت وظلت جلسة ReplayKit نشطة، يحاول التطبيق إعادة إنشاء اتصال WebRTC. إذا أوقف iOS البث أو انتهت الجلسة، لا يمكن بدء بث جديد بصمت؛ يلزم إجراء جديد وموافقة صاحب الجهاز.

## الأمان
لا تخزن صور الشاشة على خادم الإشارة. استخدم WSS وTLS، رموزاً قصيرة العمر، مصادقة متبادلة، تحديد معدل الطلبات وسجلات تدقيق لا تتضمن محتوى الشاشة.

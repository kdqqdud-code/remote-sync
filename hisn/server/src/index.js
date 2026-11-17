import { WebSocketServer } from 'ws';
import { randomUUID } from 'node:crypto';

const port = Number(process.env.PORT || 8787);
const wss = new WebSocketServer({ port, maxPayload: 65536 });
const clients = new Map();
const sessions = new Map();

const send = (ws, payload) => {
  if (ws.readyState === ws.OPEN) ws.send(JSON.stringify(payload));
};
const fail = (ws, code, message) => send(ws, { type: 'error', code, message });

wss.on('connection', (ws) => {
  let deviceId = null;
  send(ws, { type: 'hello', protocol: 'hisn-signaling', version: 3 });

  ws.on('message', (raw) => {
    let msg;
    try { msg = JSON.parse(raw.toString()); }
    catch { return fail(ws, 'BAD_JSON', 'Invalid JSON'); }
    if (!msg || typeof msg.type !== 'string') return fail(ws, 'BAD_MESSAGE', 'type required');

    if (msg.type === 'register') {
      if (typeof msg.deviceId !== 'string' || msg.deviceId.length < 8 || msg.deviceId.length > 128)
        return fail(ws, 'BAD_DEVICE_ID', 'Invalid deviceId');
      deviceId = msg.deviceId;
      clients.set(deviceId, ws);
      return send(ws, { type: 'registered', deviceId });
    }

    if (!deviceId) return fail(ws, 'NOT_REGISTERED', 'Register first');

    if (msg.type === 'session:create') {
      const peer = clients.get(msg.peerDeviceId);
      if (!peer) return fail(ws, 'PEER_OFFLINE', 'Peer offline');
      const sessionId = randomUUID();
      sessions.set(sessionId, { a: deviceId, b: msg.peerDeviceId });
      send(ws, { type: 'session:created', sessionId });
      send(peer, { type: 'session:incoming', sessionId, fromDeviceId: deviceId });
      return;
    }

    if (['webrtc:offer', 'webrtc:answer', 'webrtc:ice', 'session:end'].includes(msg.type)) {
      const session = sessions.get(msg.sessionId);
      if (!session || ![session.a, session.b].includes(deviceId))
        return fail(ws, 'SESSION_DENIED', 'Unknown session');
      const peerId = session.a === deviceId ? session.b : session.a;
      const peer = clients.get(peerId);
      if (peer) send(peer, { ...msg, fromDeviceId: deviceId });
      if (msg.type === 'session:end') sessions.delete(msg.sessionId);
      return;
    }
    fail(ws, 'UNKNOWN_TYPE', 'Unsupported type');
  });

  ws.on('close', () => {
    if (deviceId && clients.get(deviceId) === ws) clients.delete(deviceId);
    for (const [id, session] of sessions) {
      if (session.a === deviceId || session.b === deviceId) sessions.delete(id);
    }
  });
});

console.log(`HISN signaling prototype listening on port ${port}`);
console.warn('Development only: authentication, TLS/WSS, durable pairing and TURN are not implemented.');

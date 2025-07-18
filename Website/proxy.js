const WebSocket = require('ws');
const net = require('net');

const wss = new WebSocket.Server({ port: 3000 });

wss.on('connection', ws => {
    const tcpClient = new net.Socket();
    tcpClient.connect(12345, 'localhost', () => {
        ws.send('Connected to Java Server');
    });

    tcpClient.on('data', (data) => {
        ws.send(data.toString());
    });

    ws.on('message', (message) => {
        tcpClient.write(message + '\n');
    });

    ws.on('close', () => {
        tcpClient.end();
    });
});
const WebSocket = require('ws');
const net = require('net');

const clients = new Map(); // Map of WebSocket to {username, tcpClient}

const wss = new WebSocket.Server({ port: 3000 });

wss.on('connection', ws => {
    const tcpClient = new net.Socket();
    tcpClient.connect(12345, 'localhost');

    ws.on('message', (message) => {
        if (message.toString().startsWith('USER:')) {
            clients.set(ws, { username: message.toString().substring(5), tcpClient });
            ws.send('Connected to Java Server');
        }
        tcpClient.write(message + '\n');
    });

    tcpClient.on('data', (data) => {
        ws.send(data.toString());
    });

    ws.on('close', () => {
        tcpClient.end();
        clients.delete(ws);
    });
});
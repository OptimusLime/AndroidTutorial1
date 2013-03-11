
var net = require('net'),
	net_port = Math.floor(Math.random()*(65535-1024))+1024; // Randomly generated port number;

var connected_sockets = {};
var last_net_socket_id = 0;

//package include with NodeJS for creating a socket server
net.createServer(function(socket) {

  // Generate new id for socket and add it to connected sockets list
  connected_sockets[last_net_socket_id] = socket;
  socket.id = last_net_socket_id;
  last_net_socket_id = last_net_socket_id + 1;

  console.log('net socket connected: [' + socket.id + '] '  + socket.remoteAddress +':'+ socket.remotePort);

  // When data received on socket
  socket.on('data', function(data) {
    console.log('[' + socket.id + '] Data received: ' + data);
    
    //whatever it is, let's just echo it write back to this person
     socket.write(data);
     
  });

  socket.on('close', function(data) {
      
      console.log('[' + socket.id + '] Closed: ' + data);
      
      delete connected_sockets[socket.id];
  });

  socket.on('error', function(error){
    console.log('[' + socket.id + '] Error received: ' + error);
  });
}).listen(net_port, function() {
  console.log('TCP server listening on port: ' + net_port);
});


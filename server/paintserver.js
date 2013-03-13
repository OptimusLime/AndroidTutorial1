
var net = require('net'),
	net_port = Math.floor(Math.random()*(65535-1024))+1024; // Randomly generated port number;

var connected_sockets = {};
var last_net_socket_id = 0;

broadcast = function(allSockets, msg, sender)
{
        for(var sKey in allSockets)
        {
                var socket = allSockets[sKey];
                if(socket != sender)
                {
                        socket.write(msg);
                }
        }
}


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
    
 	var dString = "" + data;
    var splits = dString.split('}{');
    for(var i=0; i< splits.length; i++)
    {
    	var dObj = JSON.parse((i==0 ? '' : '{') +  splits[i]
        		               + (splits[i].indexOf("}") !== -1 ? '' : '}'));
        dObj.sid = socket.id;
        //broadcast to all other sockets but ourselves
        //in reality, this should be localized to a paint room or something
    	broadcast(connected_sockets, JSON.stringify(dObj), socket);  
    }
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


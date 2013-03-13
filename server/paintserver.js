
var net = require('net'),
	net_port = Math.floor(Math.random()*(65535-1024))+1024; // Randomly generated port number;

var connected_sockets = {};
var last_net_socket_id = 0;

var socketToRoom = {};
var roomToSockets = {};
var rooms = [];
var defaultRoom = "Default";

//http://stackoverflow.com/questions/500606/difference-between-using-the-delete-operator-or-the-splice-function-on-an-array
// Array Remove - By John Resig (MIT Licensed)
Array.prototype.remove = function(from, to) {
  var rest = this.slice((to || from) + 1 || this.length);
  this.length = from < 0 ? this.length + from : from;
  return this.push.apply(this, rest);
};

broadcastToRoom = function(roomName, msg, sender)
{
	//grab our room of sockets
	var allSockets = getOrCreateRoom(roomName);
	//loop through and send out to our other sockets -- skipping the sender
        for(var sKey =0; sKey < allSockets.length; sKey++)
        {
        	var socket = allSockets[sKey];
            if(socket != sender)
            {
                socket.write(msg);
            }
        }
}


getOrCreateRoom = function(roomName)
{
	if(!roomToSockets[roomName])
  	{
  		roomToSockets[roomName] = [];
  		rooms.push(roomName);
  	}
  
  	return roomToSockets[roomName];
}

addSocketToRoom = function(room, socket)
{
	var socketArray = getOrCreateRoom(room);
	socketArray.push(socket);
	socketToRoom[socket] = room;
}
removeSocketFromRoom = function(socket)
{
	var room = socketToRoom[socket];
	var socketArray = getOrCreateRoom(room);
	var sIx = socketArray.indexOf(socket);
	
 	console.log('Removing socket at room index: ' + sIx);
	
	if(sIx !== -1)
		socketArray.remove(sIx);	
		
	delete socketToRoom[socket];
}

//package include with NodeJS for creating a socket server
net.createServer(function(socket) {

  // Generate new id for socket and add it to connected sockets list
  connected_sockets[last_net_socket_id] = socket;
  socket.id = last_net_socket_id;
  last_net_socket_id = last_net_socket_id + 1;
  
  //if we don't have this object in our sockettoroom object, it doesn't have a room!
  if(!socketToRoom[socket]){
  	addSocketToRoom(defaultRoom, socket);  
  	}

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
		//let the user know our socket ID
		//And the room we're currently in!
		dObj.sid = socket.id;
        dObj.room = socketToRoom[socket];
                
        //broadcast to all other sockets in the room!
    	broadcastToRoom(socketToRoom[socket], JSON.stringify(dObj), socket);  
    }
  });

  socket.on('close', function(data) {
      
      console.log('[' + socket.id + '] Closed: ' + data);
      
      //we closed, remove us from the room, and delete us!
      removeSocketFromRoom(socket);
      
      delete connected_sockets[socket.id];
  });

  socket.on('error', function(error){
    console.log('[' + socket.id + '] Error received: ' + error);
  });
}).listen(net_port, function() {
  console.log('TCP server listening on port: ' + net_port);
});


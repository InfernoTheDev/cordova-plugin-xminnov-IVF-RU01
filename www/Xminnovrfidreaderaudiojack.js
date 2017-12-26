var exec = require('cordova/exec');

function Xminnovrfidreaderaudiojack() { 
	console.log("Xminnovrfidreaderaudiojack.js: is created");
}

Xminnovrfidreaderaudiojack.prototype.registerService = function(success, error ){
	exec(success, error, "Xminnovrfidreaderaudiojack", 'registerService', []);
}

Xminnovrfidreaderaudiojack.prototype.unRegisterService = function(success, error ){
	exec(success, error, "Xminnovrfidreaderaudiojack", 'unregisterService', []);
}

Xminnovrfidreaderaudiojack.prototype.start = function( success, error ){
	exec(success, error, "Xminnovrfidreaderaudiojack", 'start', []);
}

Xminnovrfidreaderaudiojack.prototype.stop = function( success, error ){
	exec(success, error, "Xminnovrfidreaderaudiojack", 'stop', []);
}

Xminnovrfidreaderaudiojack.prototype.onDeviceStatusChange = function( callback, success, error ){
	Xminnovrfidreaderaudiojack.prototype.updateDeviceStatusChange = callback
	exec(success, error, "Xminnovrfidreaderaudiojack", 'onDeviceStatusChange',[]);
}

Xminnovrfidreaderaudiojack.prototype.updateDeviceStatusChange = function( status ){
	console.log(status)
}

Xminnovrfidreaderaudiojack.prototype.onScannerReceive = function( callback, success, error ){
	Xminnovrfidreaderaudiojack.prototype.tagReceive = callback
	exec(success, error, "Xminnovrfidreaderaudiojack", 'onScannerReceive',[]);
}

Xminnovrfidreaderaudiojack.prototype.tagReceive = function(payload){
	console.log("Received tag")
	console.log(payload)
}

exec(function(result){ 
	console.log(result)
	console.log("XminnovPlugin Ready OK") 
}, function(result){ 
	console.log("XminnovPlugin Ready ERROR") 
}, "Xminnovrfidreaderaudiojack",'ready',[]);

var xminnovrfidreaderaudiojack = new Xminnovrfidreaderaudiojack();
module.exports = xminnovrfidreaderaudiojack;

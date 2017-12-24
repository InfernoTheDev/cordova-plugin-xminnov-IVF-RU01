var exec = require('cordova/exec');

exports.coolMethod = function (arg0, success, error) {
    exec(success, error, 'xminnov-UHF-RFID-reader-audio-jack', 'coolMethod', [arg0]);
};

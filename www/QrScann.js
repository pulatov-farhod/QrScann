var exec = require('cordova/exec');
function plugin(){

}
plugin.prototype.qrRun = function (success,error) {
    exec(success,error, "QrScann", "qrRun", []);
    // exec(success, error, 'QrScann', 'qr_run', [arg0]);
};

module.exports = new plugin();

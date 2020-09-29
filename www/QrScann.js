var exec = require('cordova/exec');
function plugin(){

}
plugin.prototype.qrRun = function (arg0,success,error) {
    exec(success,error, "QrScann", "qrRun",[arg0]);
    // exec(success, error, 'QrScann', 'qr_run', [arg0]);
};
module.exports = new plugin();

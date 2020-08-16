
import UIKit
import SwiftUI
import SwiftQRScanner

@objc(QrScann) class QrScann:CDVPlugin, QRScannerCodeDelegate {
    var window: UIWindow?
  
    var commId: CDVInvokedUrlCommand?;
  
    @objc(qrRun:) func qrRun(command: CDVInvokedUrlCommand){
    
    commId = command

    let scanner = QRCodeScannerController()
    
    scanner.delegate = self

    self.viewController?.present(
          scanner,
          animated: true,
          completion: nil
        )
    }
    
    
    func qrScanner(_ controller: UIViewController, scanDidComplete result: String) {
        var pluginResult = CDVPluginResult(status: CDVCommandStatus_OK,messageAs: result);
        self.commandDelegate!.send(pluginResult, callbackId: commId?.callbackId);
    }
    
    func qrScannerDidFail(_ controller: UIViewController, error: String) {
        var pluginResult = CDVPluginResult (status: CDVCommandStatus_ERROR, messageAs: error);
        self.commandDelegate!.send(pluginResult, callbackId: commId?.callbackId);
    }
    
    func qrScannerDidCancel(_ controller: UIViewController) {
        var pluginResult = CDVPluginResult (status: CDVCommandStatus_ERROR, messageAs: "cancel scanner");
        self.commandDelegate!.send(pluginResult, callbackId: commId?.callbackId);
    }
    
}
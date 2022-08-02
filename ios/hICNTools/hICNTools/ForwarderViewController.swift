// Copyright (c) 2022 Cisco and/or its affiliates.
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at:
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

//
//  ForwarderViewController.swift
//  hICNTools
//


import UIKit

class ForwarderViewController: UIViewController {


    @IBOutlet weak var sourceIpLabel: UILabel!

    @IBOutlet weak var sourcePortTextField: UITextField!

    @IBOutlet weak var nextHopIpTextField: UITextField!

    @IBOutlet weak var nextHopPortTextField: UITextField!

    @IBOutlet weak var configurationTextView: UITextView!

    @IBOutlet weak var forwarderStatusLabel: UILabel!

    @IBOutlet weak var forwarderSwitch: UISwitch!

    @IBOutlet weak var updateButton: UIButton!

    let alert = UIAlertController(title: "Error!!", message: "Fields empty!", preferredStyle: UIAlertController.Style.alert)

    let savedValues = UserDefaults.standard

    var address: String?

    let constants = Constants()

    struct defaultsKeys {
        static let sourcePort = "sourcePort"
        static let nextHopIp = "nextHopIp"
        static let nextHopPort = "nextHopPort"
        static let forwarderConfiguration = "forwarderConfiguration"
        static let rtcProtocol = "rtcProtocol"
        static let fixedWindowSize = "fixedWindowSize"
        static let windowSize = "windowSize"

    }

    override func viewDidLoad() {
        super.viewDidLoad()
        initComponents()
        // Do any additional setup after loading the view.
    }

    func initComponents() {
        address = getWiFiAddress()
        if address == nil {
            address = getEthernetAddress()
        }
        sourceIpLabel.text = address
        if savedValues.string(forKey: defaultsKeys.sourcePort) == nil {
            sourcePortTextField.text = String(constants.DEFAULT_SOURCE_PORT)
        } else {
            sourcePortTextField.text = savedValues.string(forKey: defaultsKeys.sourcePort)
        }

        if savedValues.string(forKey: defaultsKeys.nextHopIp) == nil {
            nextHopIpTextField.text = constants.DEFAULT_NEXT_HOP_IP;
        } else {
            nextHopIpTextField.text = savedValues.string(forKey: defaultsKeys.nextHopIp)
        }

        if savedValues.string(forKey: defaultsKeys.nextHopPort) == nil {
            nextHopPortTextField.text = String(constants.DEFAULT_NEXT_HOP_PORT)
        } else {
            nextHopPortTextField.text = savedValues.string(forKey: defaultsKeys.nextHopPort)
        }

        nextHopIpTextField.layer.cornerRadius = 5
        nextHopIpTextField.layer.borderWidth = 1
        sourcePortTextField.layer.cornerRadius = 5
        sourcePortTextField.layer.borderWidth = 1
        nextHopPortTextField.layer.cornerRadius = 5
        nextHopPortTextField.layer.borderWidth = 1

        if savedValues.string(forKey: defaultsKeys.forwarderConfiguration) != nil {
            configurationTextView.text = savedValues.string(forKey: defaultsKeys.forwarderConfiguration)
        }
        configurationTextView.layer.cornerRadius = 5
        configurationTextView.layer.borderWidth = 1
        alert.addAction(UIAlertAction(title: "Close", style: UIAlertAction.Style.default, handler: nil))

        //startButton.isUserInteractionEnabled = false
        //self.stopButton.isUserInteractionEnabled = false;
    }



    func getWiFiAddress() -> String? {
        var address : String?

        // Get list of all interfaces on the local machine:
        var ifaddr : UnsafeMutablePointer<ifaddrs>?
        guard getifaddrs(&ifaddr) == 0 else { return nil }
        guard let firstAddr = ifaddr else { return nil }

        // For each interface ...
        for ifptr in sequence(first: firstAddr, next: { $0.pointee.ifa_next }) {
            let interface = ifptr.pointee

            // Check for IPv4 or IPv6 interface:
            let addrFamily = interface.ifa_addr.pointee.sa_family
            if addrFamily == UInt8(AF_INET) {

                // Check interface name:
                let name = String(cString: interface.ifa_name)
                if  name == "en0" {

                    // Convert interface address to a human readable string:
                    var addr = interface.ifa_addr.pointee
                    var hostname = [CChar](repeating: 0, count: Int(NI_MAXHOST))
                    getnameinfo(&addr, socklen_t(interface.ifa_addr.pointee.sa_len),
                                &hostname, socklen_t(hostname.count),
                                nil, socklen_t(0), NI_NUMERICHOST)
                    address = String(cString: hostname)

                }
            }
        }
        freeifaddrs(ifaddr)

        return address
    }

    func getEthernetAddress() -> String? {
           var address : String?

           // Get list of all interfaces on the local machine:
           var ifaddr : UnsafeMutablePointer<ifaddrs>?
           guard getifaddrs(&ifaddr) == 0 else { return nil }
           guard let firstAddr = ifaddr else { return nil }

           // For each interface ...
           for ifptr in sequence(first: firstAddr, next: { $0.pointee.ifa_next }) {
               let interface = ifptr.pointee

               // Check for IPv4 or IPv6 interface:
               let addrFamily = interface.ifa_addr.pointee.sa_family
               if addrFamily == UInt8(AF_INET) {

                   // Check interface name:
                   let name = String(cString: interface.ifa_name)
                   if  name == "en1" {

                       // Convert interface address to a human readable string:
                       var addr = interface.ifa_addr.pointee
                       var hostname = [CChar](repeating: 0, count: Int(NI_MAXHOST))
                       getnameinfo(&addr, socklen_t(interface.ifa_addr.pointee.sa_len),
                                   &hostname, socklen_t(hostname.count),
                                   nil, socklen_t(0), NI_NUMERICHOST)
                       address = String(cString: hostname)
                   }
               }
           }
           freeifaddrs(ifaddr)

           return address
       }

    @IBAction func updateOnClick(_ sender: Any) {
        address = getWiFiAddress()
        if address == nil {
            address = getEthernetAddress()
        }
        self.sourceIpLabel.text = address
    }

    @IBAction func onForwarderSwitchValueChanged(_ sender: Any) {
        if forwarderSwitch.isOn {
            if configurationTextView.text == nil || !checkDestinationIp() {
                self.present(alert, animated: true, completion: nil)
                forwarderSwitch.setOn(false, animated: true)
            } else {
                savedValues.set(sourcePortTextField.text, forKey: defaultsKeys.sourcePort)
                savedValues.set(nextHopIpTextField.text, forKey: defaultsKeys.nextHopIp)
                savedValues.set(nextHopPortTextField.text, forKey: defaultsKeys.nextHopPort)
                savedValues.set(configurationTextView.text, forKey: defaultsKeys.forwarderConfiguration)
                sourcePortTextField.isUserInteractionEnabled = false
                nextHopPortTextField.isUserInteractionEnabled = false
                nextHopIpTextField.isUserInteractionEnabled = false
                configurationTextView.isUserInteractionEnabled = false
                updateButton.isUserInteractionEnabled = false
                var forwarderConfigurationText = configurationTextView.text
                forwarderConfigurationText = forwarderConfigurationText!.replacingOccurrences(of: "%%source_ip%%", with: address!)
                forwarderConfigurationText = forwarderConfigurationText!.replacingOccurrences(of: "%%source_port%%", with: sourcePortTextField.text!)
                forwarderConfigurationText = forwarderConfigurationText!.replacingOccurrences(of: "%%destination_ip%%", with: nextHopIpTextField.text!)
                forwarderConfigurationText = forwarderConfigurationText!.replacingOccurrences(of: "%%destination_port%%", with: nextHopPortTextField.text!)
                let urlString = writeToFile(configuration: forwarderConfigurationText)
                print(forwarderConfigurationText ?? "" )
                forwarderStatusLabel.text = constants.FORWARDER_ENABLED

                DispatchQueue.global(qos: .background).async {
                    print("Starting Forwarder")
                    let urlCChar = urlString.cString(using: .utf8)
                    startHicnFwd(urlCChar, urlString.count)
                    DispatchQueue.main.async {
                        print("Forwarder is stopped")
                    }
                }
            }
        } else {
            sourcePortTextField.isUserInteractionEnabled = false
            nextHopIpTextField.isUserInteractionEnabled = true
            configurationTextView.isUserInteractionEnabled = true
            updateButton.isUserInteractionEnabled = false

            forwarderStatusLabel.text = constants.FORWARDER_DISABLED
            stopHicnFwd()
        }
    }

    func checkDestinationIp() -> Bool {
        if nextHopIpTextField.text != nil {
            let ipV4RegEx = "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$"
            let checkIpV4 = NSPredicate(format:"SELF MATCHES %@", ipV4RegEx)
            return checkIpV4.evaluate(with: nextHopIpTextField.text)
        }
        return false
    }

    func writeToFile(configuration: String?) -> String {

        if let dir = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first {
            let fileURL = dir.appendingPathComponent(constants.CONFIGURATION_FILE_NAME)

            do {
                try configuration?.write(to: fileURL, atomically: false, encoding: .utf8)
            }
            catch {
                print("impossible to write the file!\n")
            }

            return fileURL.absoluteString
        }
        return ""
    }

}

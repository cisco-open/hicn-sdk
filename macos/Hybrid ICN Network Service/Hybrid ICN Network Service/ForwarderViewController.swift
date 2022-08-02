import Cocoa
import Preferences
import Charts

final class ForwarderViewController: NSViewController, PreferencePane, NSApplicationDelegate {
	@IBOutlet weak var sourceIpPopUpButton: NSPopUpButton!

	@IBOutlet weak var updateIpSource: NSButtonCell!

	@IBOutlet weak var sourcePortTextField: NSTextField!

	@IBOutlet weak var nextHopIpTextField: NSTextField!

	@IBOutlet weak var nextHopPortTextField: NSTextField!

	@IBOutlet var configurationTextView: NSTextView!
	
	@IBOutlet weak var hicnFwdSwitch: NSSwitch!

	
	let preferencePaneIdentifier = PreferencePane.Identifier.forwarder
	let preferencePaneTitle = "Forwarder"
	let toolbarItemIcon = NSImage(named: NSImage.advancedName)!

	let constants = Constants()
    var isRunning: Bool = false
    let savedValues = UserDefaults.standard
	var addressesMap = [Int: String]()
	var pathConfig: String?
	
	struct defaultKeys {
		static let url = "url"
		static let hicnFwdConfiguration = "hicnFwdConfiguration"
		static let nextHopPort = "nextHopPort"
		static let nextHopIp = "nextHopIp"
		static let sourcePort = "sourcePort"
	}
	
	override var nibName: NSNib.Name? { "ForwarderViewController" }

	override func viewDidLoad() {
		super.viewDidLoad()
		initApplication()

		// Setup stuff here
	}

	@IBAction
	private func zoomAction(_ sender: Any) {} // swiftlint:disable:this attributes
	
	
	func getAddresses() -> [String:String]? {
        var addresses = [String:String]()
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
                
                if  name != constants.LO_0 {
                    
                    // Convert interface address to a human readable string:
                    var addr = interface.ifa_addr.pointee
                    var hostname = [CChar](repeating: 0, count: Int(NI_MAXHOST))
                    getnameinfo(&addr, socklen_t(interface.ifa_addr.pointee.sa_len),
                                &hostname, socklen_t(hostname.count),
                                nil, socklen_t(0), NI_NUMERICHOST)
                    // address = String(cString: hostname)
                    addresses[name] = String(cString: hostname)
                }
            }
        }
        freeifaddrs(ifaddr)
        return addresses
    }
	
	func initApplication() {
        isRunning = false
        let addresses = getAddresses()!
        if addresses.count > 0 {
            var i = 0
            for (interface, address) in addresses {
                print(interface, address)
                sourceIpPopUpButton.addItem(withTitle: interface + ": " + address )
                addressesMap[i] = address
                // adressesList
                i = i + 1
            }
        } else {
            hicnFwdSwitch.isEnabled = false
        }
        if savedValues.string(forKey: defaultKeys.sourcePort) != nil {
            sourcePortTextField.stringValue = savedValues.string(forKey: defaultKeys.sourcePort)!
        } else {
            sourcePortTextField.stringValue = String(constants.DEFAULT_SOURCE_PORT)
        }
        if savedValues.string(forKey: defaultKeys.nextHopIp) != nil {
            nextHopIpTextField.stringValue = savedValues.string(forKey: defaultKeys.nextHopIp)!
		} else {
			
			nextHopIpTextField.stringValue = String(constants.DEFAULT_NEXT_HOP_IP)
		}
        if savedValues.string(forKey: defaultKeys.nextHopPort) != nil {
            nextHopPortTextField.stringValue = savedValues.string(forKey: defaultKeys.nextHopPort)!

        } else {
			nextHopPortTextField.stringValue = String(constants.DEFAULT_NEXT_HOP_PORT)
        }
        
        if savedValues.string(forKey: defaultKeys.hicnFwdConfiguration) != nil {
			configurationTextView.string = savedValues.string(forKey: defaultKeys.hicnFwdConfiguration)!
        }
    }
	
	@IBAction func updateSourceIpButtonAction(_ sender: Any) {
		sourceIpPopUpButton.removeAllItems()
        addressesMap.removeAll()
        let addresses = getAddresses()!
        if addresses.count > 0 {
            var i = 0
            for (interface, address) in addresses {
                print(interface, address)
                sourceIpPopUpButton.addItem(withTitle: interface + ": " + address )
                addressesMap[i] = address
                // adressesList
                i = i + 1
            }
        } else {
            hicnFwdSwitch.isEnabled = false
        }
	}
	
	func checkDestinationIp() -> Bool {
        if nextHopIpTextField.stringValue != "" {
            let ipV4RegEx = "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$"
            let checkIpV4 = NSPredicate(format:"SELF MATCHES %@", ipV4RegEx)
            return checkIpV4.evaluate(with: nextHopIpTextField.stringValue)
        }
        return false
    }
    
    func dialogOK(question: String, text: String, style: NSAlert.Style) {
        let alert: NSAlert = NSAlert()
        alert.messageText = question
        alert.informativeText = text
        alert.alertStyle = style
        alert.addButton(withTitle: "Close")
        alert.runModal()
    }
	
	
	@IBAction func hicnFwdSwitchOnChange(_ sender: Any) {
		
		
		if hicnFwdSwitch.state == NSSwitch.StateValue(1) {
			if configurationTextView.string.isEmpty || !checkDestinationIp() {
                dialogOK(question: "Error!", text: "Maybe you have empty or wrong fields!", style: NSAlert.Style.critical)

                hicnFwdSwitch.state = NSSwitch.StateValue(0)
            } else {
                savedValues.set(sourcePortTextField.stringValue, forKey: defaultKeys.sourcePort)
                savedValues.set(nextHopIpTextField.stringValue, forKey: defaultKeys.nextHopIp)
                savedValues.set(nextHopPortTextField.stringValue, forKey: defaultKeys.nextHopPort)
                savedValues.set(configurationTextView.string, forKey: defaultKeys.hicnFwdConfiguration)
                sourcePortTextField.isEnabled = false
                nextHopPortTextField.isEnabled = false
                nextHopIpTextField.isEnabled = false
                configurationTextView.isEditable = false
				updateIpSource.isEnabled = false
				sourceIpPopUpButton.isEnabled = false
                var forwarderConfigurationText = configurationTextView.string
                forwarderConfigurationText = forwarderConfigurationText.replacingOccurrences(of: "%%source_ip%%", with: addressesMap[sourceIpPopUpButton.indexOfSelectedItem]!)
                forwarderConfigurationText = forwarderConfigurationText.replacingOccurrences(of: "%%source_port%%", with: sourcePortTextField.stringValue)
                forwarderConfigurationText = forwarderConfigurationText.replacingOccurrences(of: "%%nexthop_ip%%", with: nextHopIpTextField.stringValue)
                forwarderConfigurationText = forwarderConfigurationText.replacingOccurrences(of: "%%nexthop_port%%", with: nextHopPortTextField.stringValue)
                let urlString = writeToFile(configuration: forwarderConfigurationText)
                print(forwarderConfigurationText)
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
			sourcePortTextField.isEnabled = true
			nextHopIpTextField.isEnabled = true
			configurationTextView.isEditable = true
			sourceIpPopUpButton.isEnabled = true
			updateIpSource.isEnabled = true
            stopHicnFwd()
			
        }
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

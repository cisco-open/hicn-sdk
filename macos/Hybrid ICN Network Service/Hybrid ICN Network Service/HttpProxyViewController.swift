import Cocoa
import Preferences
import Charts
import Network

final class HttpProxyViewController: NSViewController, PreferencePane, NSApplicationDelegate {

    @IBOutlet weak var prefixTextField: NSTextField!

    @IBOutlet weak var listeningPortTextField: NSTextField!
    
    @IBOutlet weak var hicnHttpProxySwitch: NSSwitch!

	
    let preferencePaneIdentifier = PreferencePane.Identifier.httpProxy
	let preferencePaneTitle = "HttpProxy"
	let toolbarItemIcon = NSImage(named: NSImage.advancedName)!

	let constants = Constants()
    let savedValues = UserDefaults.standard
	
    struct defaultKeys {
        static let prefix = "prefix"
        static let listeningPort = "listeningPort"
    }
	
	override var nibName: NSNib.Name? { "HttpProxyViewController" }

	override func viewDidLoad() {
		super.viewDidLoad()
		initApplication()

		// Setup stuff here
	}

    @IBAction func hicnHttpProxySwitchOnChange(_ sender: Any) {
        print(hicnHttpProxySwitch.state)
        if hicnHttpProxySwitch.state == NSSwitch.StateValue(1) {
            if prefixTextField.stringValue.isEmpty || listeningPortTextField.stringValue.isEmpty {
                dialogOK(question: "Error!", text: "Maybe you have empty or wrong fields!", style: NSAlert.Style.critical)

                hicnHttpProxySwitch.state = NSSwitch.StateValue(0)
            } else {
                savedValues.set(prefixTextField.stringValue, forKey: defaultKeys.prefix)
                savedValues.set(listeningPortTextField.stringValue, forKey: defaultKeys.listeningPort)
                prefixTextField.isEnabled = false
                listeningPortTextField.isEnabled = false
                let prefix = prefixTextField.stringValue.cString(using: .utf8)
                let listenPort = Int32(listeningPortTextField.stringValue) ?? 0
                DispatchQueue.global(qos: .background).async {
                    print("Starting HttpProxy")
    
                    startHicnProxy(prefix, listenPort)
                    DispatchQueue.main.async {
                        print("HttpProxy is stopped")
                    }
                }
            }
        } else {
            prefixTextField.isEnabled = true
            prefixTextField.isEnabled = true
            stopHicnProxy()
        }
        
    }

    func initApplication() {
        if savedValues.string(forKey: defaultKeys.prefix) != nil {
            prefixTextField.stringValue = savedValues.string(forKey: defaultKeys.prefix)!
        } else {
            prefixTextField.stringValue = constants.DEFAULT_HTTP_PROXY_PREFIX
        }
        if savedValues.string(forKey: defaultKeys.listeningPort) != nil {
            listeningPortTextField.stringValue = savedValues.string(forKey: defaultKeys.listeningPort)!
		} else {
			listeningPortTextField.stringValue = String(constants.DEFAULT_HTTP_PROXY_LISTENING_PORT)
		}
    }
    
    func dialogOK(question: String, text: String, style: NSAlert.Style) {
        let alert: NSAlert = NSAlert()
        alert.messageText = question
        alert.informativeText = text
        alert.alertStyle = style
        alert.addButton(withTitle: "Close")
        alert.runModal()
    }
}

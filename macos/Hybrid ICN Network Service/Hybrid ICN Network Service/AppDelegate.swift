import Cocoa
import Preferences

extension PreferencePane.Identifier {
	static let forwarder = Identifier("forwarder")
	static let hiperf = Identifier("hiperf")
	static let httpProxy = Identifier("http-proxy")
	static let cli = Identifier("cli")
}

@NSApplicationMain
final class AppDelegate: NSObject, NSApplicationDelegate {
	@IBOutlet private var window: NSWindow!

	var preferencesStyle: PreferencesStyle {
		get { PreferencesStyle.preferencesStyleFromUserDefaults() }
		set {
			newValue.storeInUserDefaults()
		}
	}

	lazy var forwarderViewController = ForwarderViewController()
	lazy var hiperfViewController = HiperfViewController()
	lazy var httpProxyViewController = HttpProxyViewController()
	lazy var cliViewController = CLIViewController()

	lazy var hicnPanelsWindowController = PreferencesWindowController(
		preferencePanes: [
			forwarderViewController,
			httpProxyViewController,
			hiperfViewController,
			cliViewController
		],
		style: preferencesStyle,
		animated: true,
		hidesToolbarForSingleItem: true
	)

	func applicationWillFinishLaunching(_ notification: Notification) {
		window.orderOut(self)
	}

	func applicationDidFinishLaunching(_ notification: Notification) {
		hicnPanelsWindowController.show(preferencePane: .forwarder)
		
	}
	
	@IBAction func showForwarder(_ sender: Any) {
		hicnPanelsWindowController.show(preferencePane: .forwarder)
	}
	
	@IBAction func showHIperf(_ sender: Any) {
		hicnPanelsWindowController.show(preferencePane: .hiperf)
	}

	@IBAction func showHttpProxy(_ sender: Any) {
		hicnPanelsWindowController.show(preferencePane: .httpProxy)
	}

	@IBAction func showCLI(_ sender: Any) {
		hicnPanelsWindowController.show(preferencePane: .cli)
	}
	
	//func applicationShouldTerminateAfterLastWindowClosed(_ sender: NSApplication) -> Bool {
		//forwarderViewController.test()
		
		//return false
	//}
	
}

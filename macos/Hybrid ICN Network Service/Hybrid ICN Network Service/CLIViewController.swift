import Cocoa
import Preferences
import Charts
import Network

final class CLIViewController: NSViewController, PreferencePane, NSApplicationDelegate, NSTextViewDelegate {
    
    @IBOutlet var terminalTextView: NSTextView!
    var line = 0
    var currentLine = 0
    var column = 0
    var currentColumn = 0

    let preferencePaneIdentifier = PreferencePane.Identifier.cli
	let preferencePaneTitle = "CLI"
	let toolbarItemIcon = NSImage(named: NSImage.advancedName)!

	let constants = Constants()
    let savedValues = UserDefaults.standard
	
    struct defaultKeys {
        static let prefix = "prefix"
        static let listeningPort = "listeningPort"
    }
	
	override var nibName: NSNib.Name? { "CLIViewController" }

	override func viewDidLoad() {
		super.viewDidLoad()
		initApplication()
        
		// Setup stuff here
	}
    
    override func viewDidAppear() {
        super.viewWillAppear()
        terminalTextView.window?.makeFirstResponder(terminalTextView)
        terminalTextView.setSelectedRange(NSRange(location: terminalTextView.string.count, length: 0))
    }
    
    
    func textDidChange(_ notification: Notification) {
      
        guard let textView = notification.object as? NSTextView else { return }
        let insertionPointIndex = textView.selectedRanges.first?.rangeValue.location ?? 0
        let text = textView.string
        let char = Array(text)[insertionPointIndex - 1]
        if Character(extendedGraphemeClusterLiteral: char).asciiValue == 10 {
            //print(text.count, insertionPointIndex)
            var newText = "";
            if (text.count != insertionPointIndex) {
                if ( insertionPointIndex > 1) {
                    let prefix = String(text.prefix(insertionPointIndex-1))
                    let suffix = String(text.suffix(text.count - insertionPointIndex))
                    newText = prefix + suffix + "\n"
                }
            } else {
                newText = textView.string
            }
            let lines = newText.split(whereSeparator: \.isNewline)
            let lastLine = lines[lines.count - 1].suffix(lines[lines.count - 1].count - 1)
            //lastLine = "cli" + lastLine
            print(lastLine)
             
            let result = execCommand(strdup(String(lastLine)))
            textView.string = newText + String(cString: result! ) + "\n> "
            
            
        } else {
            if self.column == 1 {
                textView.string = textView.string + " "
            }
        }
    }
    
    func textViewDidChangeSelection(_ notification: Notification) {
        guard let textView = notification.object as? NSTextView else { return }
        let insertionPointIndex = textView.selectedRanges.first?.rangeValue.location
        let currentPosition = textView.string.characterRowAndLineAt(position: insertionPointIndex!)
        let currentLine = currentPosition?.line
        let currentColumn = currentPosition?.column
        self.currentLine = (currentLine == nil ? 0: currentLine)!
        self.currentColumn = (currentColumn == nil ? 0: currentColumn)!
        
        let totalPosition = textView.string.characterRowAndLineAt(position: textView.string.count)
        let line = totalPosition?.line
        let column = totalPosition?.column
        self.line = (line == nil ? 0: line)!
        self.column = (column == nil ? 0: column)!
     //   print(totalPosition?.lastLine)
        if currentLine != line {
            textView.isEditable = false
        } else {
            textView.isEditable = true
        }
        
        
    }

    func initApplication() {
        
        terminalTextView.delegate = self
        terminalTextView.enabledTextCheckingTypes = 0
        terminalTextView.string = "\n> "
        
        terminalTextView.font = NSFont.monospacedSystemFont(ofSize: 12, weight: .regular)
        terminalTextView.becomeFirstResponder()
        
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

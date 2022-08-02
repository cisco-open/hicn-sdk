import Cocoa
import Preferences
import Charts

final class HiperfViewController: NSViewController, PreferencePane {
	let preferencePaneIdentifier = PreferencePane.Identifier.hiperf
	let preferencePaneTitle = "hIperf"
	
	enum InterfaceStyle : String {
	   case Dark, Light

	   init() {
		  let type = UserDefaults.standard.string(forKey: "AppleInterfaceStyle") ?? "Light"
		  self = InterfaceStyle(rawValue: type)!
		}
	}

    struct defaultsKeys {
        static let hicnPrefix = "hicnPrefix"
        static let beta = "beta"
        static let dropFactor = "dropFactor"
        static let lifeTime = "lifeTime"
        static let rtcProtocol = "rtcProtocol"
        static let fixedWindowSize = "fixedWindowSize"
        static let windowSize = "windowSize"
    }
    var chartDataEntryArray = [ChartDataEntry]()
    var values = [Int]()
    var index = Int(0)
    let constants = Constants()
    let savedValues = UserDefaults.standard
	
	var timer = RepeatingTimer(timeInterval: 1  )

	@IBOutlet weak var hicnPrefix: NSTextField!

	@IBOutlet weak var lineChartView: LineChartView!

	@IBOutlet weak var betaFactor: NSTextField!
	
	@IBOutlet weak var dropFactor: NSTextField!

	@IBOutlet weak var lifeTime: NSTextField!

	@IBOutlet weak var rtcProtocolSwitch: NSSwitch!

	@IBOutlet weak var fixedWindowSizeSwitch: NSSwitch!

	@IBOutlet weak var windowSize: NSTextField!

	@IBOutlet weak var startButton: NSButton!

	@IBOutlet weak var stopButton: NSButton!

	override var nibName: NSNib.Name? { "HiperfViewController" }

	override func viewDidLoad() {
		super.viewDidLoad()
			
		if savedValues.string(forKey: defaultsKeys.hicnPrefix) == nil {

		   hicnPrefix.stringValue = String(constants.DEFAULT_HICNPREFIX)
		} else {
		   hicnPrefix.stringValue = savedValues.string(forKey: defaultsKeys.hicnPrefix)!
		}

		if savedValues.string(forKey: defaultsKeys.beta) == nil {
			betaFactor.stringValue = String(constants.DEFAULT_BETA)
		} else {
			betaFactor.stringValue = savedValues.string(forKey: defaultsKeys.beta)!
		}
		
		if savedValues.string(forKey: defaultsKeys.dropFactor) == nil {
			dropFactor.stringValue = String(constants.DEFAULT_DROP_FACTOR)
		} else {
			dropFactor.stringValue = savedValues.string(forKey: defaultsKeys.dropFactor)!
		}
		
		if savedValues.string(forKey: defaultsKeys.lifeTime) == nil {
			lifeTime.stringValue = String(constants.DEFAULT_LIFETIME)
		} else {
			lifeTime.stringValue = savedValues.string(forKey: defaultsKeys.lifeTime)!
		}
		
		if savedValues.object(forKey: defaultsKeys.rtcProtocol) == nil {
			rtcProtocolSwitch.state = NSSwitch.StateValue(constants.DEFAULT_RTCPROTOCOL)
		} else {
			rtcProtocolSwitch.state = NSSwitch.StateValue(savedValues.integer(forKey: defaultsKeys.rtcProtocol))
		}
		
		if savedValues.object(forKey: defaultsKeys.fixedWindowSize) == nil {
			fixedWindowSizeSwitch.state = NSSwitch.StateValue(constants.DEFAULT_FIXEDWINDOWSIZE)
			windowSize.isEnabled = 	Bool(truncating: constants.DEFAULT_FIXEDWINDOWSIZE as NSNumber)
		} else {
			fixedWindowSizeSwitch.state = NSSwitch.StateValue(savedValues.integer(forKey: defaultsKeys.fixedWindowSize))
			windowSize.isEnabled = Bool(truncating: fixedWindowSizeSwitch.state as NSNumber)
		}
		
		if savedValues.string(forKey: defaultsKeys.windowSize) == nil {
			windowSize.stringValue = String(constants.DEFAULT_WINDOW_SIZE)
		} else {
			windowSize.stringValue = savedValues.string(forKey: defaultsKeys.windowSize)!
		}
		
		initGraph()
	}
	
	@IBAction func fixedWindowSizeOnChange(_ sender: Any) {
	
		if self.fixedWindowSizeSwitch.state == NSSwitch.StateValue(0) {
			self.windowSize.isEnabled = false
		} else {
			
			self.windowSize.isEnabled = true
		}
		
	}
	@IBAction func startButtonOnClick(_ sender: Any) {
		startButton.isEnabled = false
		stopButton.isEnabled = true
		
		savedValues.set(hicnPrefix.stringValue, forKey: defaultsKeys.hicnPrefix)
		savedValues.set(betaFactor.stringValue, forKey: defaultsKeys.beta)
		savedValues.set(dropFactor.stringValue, forKey: defaultsKeys.dropFactor)
		savedValues.set(lifeTime.stringValue, forKey: defaultsKeys.lifeTime)
		savedValues.set(rtcProtocolSwitch.state.rawValue, forKey: defaultsKeys.rtcProtocol)
		savedValues.set(fixedWindowSizeSwitch.state.rawValue, forKey: defaultsKeys.fixedWindowSize)
		savedValues.set(windowSize.stringValue, forKey: defaultsKeys.windowSize)
		timer = RepeatingTimer(timeInterval: 1  )
		
		var hicnPrefixCChar: [CChar]?
        let betaFactorFloat = Float(betaFactor.stringValue) ?? 0
        let dropFactorFloat = Float(dropFactor.stringValue) ?? 0
        var windowSizeInt = 0
		if self.fixedWindowSizeSwitch.state.rawValue == 1 {
            windowSizeInt = Int(windowSize.stringValue) ?? 0
        }
        
		let rtcProtocol = rtcProtocolSwitch.state.rawValue
        let interestLifetimeInt = Int(lifeTime.stringValue) ?? 0
        let queue = DispatchQueue(label: "hiperf")
        hicnPrefixCChar = self.hicnPrefix.stringValue.cString(using: .utf8)
        queue.async {
            startHiperf(hicnPrefixCChar, betaFactorFloat, dropFactorFloat, Int32(windowSizeInt),1000, rtcProtocol, interestLifetimeInt)
        }
        self.index = 0
        
        DispatchQueue.main.async {
                   
            self.initGraph()
                       
        }
        
        timer.eventHandler = {
            DispatchQueue.main.async {
                let value = getValue()
                self.addData(Int(value)) //self.index%100)
                self.index = self.index + 1
                
            }
            
        }
        timer.resume()
	}
	
	@IBAction func stopButtonOnClick(_ sender: Any) {
		timer.suspend()
        
        stopHiperf()
        
        startButton.isEnabled = true
        stopButton.isEnabled = false
	}
	
	func addData(_ value: Int) {
		print("index " + String(self.index))
		if chartDataEntryArray.count > 30 {
		   chartDataEntryArray.remove(at: 0)
		   values.remove(at: 0)
		}
		chartDataEntryArray.append(ChartDataEntry(x: Double(index), y: Double(value)))
		values.append(value)
		let maxValue = values.max()
		lineChartView.xAxis.axisMinimum = Double(self.index - 30)
		lineChartView.xAxis.axisMaximum = Double(self.index + 1)

		let set1 = LineChartDataSet(entries: chartDataEntryArray)

		set1.drawIconsEnabled = false

		set1.lineDashLengths = [5, 2.5]
		set1.highlightLineDashLengths = [5, 2.5]
		set1.lineWidth = 1
		set1.circleRadius = 3
		set1.drawCircleHoleEnabled = false
		set1.valueFont = .systemFont(ofSize: 9)
		set1.formLineDashLengths = [5, 2.5]
		set1.formLineWidth = 1
		set1.formSize = 15

		let gradientColors = [ChartColorTemplates.colorFromString("#00ff0000").cgColor,
							 ChartColorTemplates.colorFromString("#ffff0000").cgColor]
		let gradient = CGGradient(colorsSpace: nil, colors: gradientColors as CFArray, locations: nil)!

		set1.fillAlpha = 1
		set1.fill = Fill(linearGradient: gradient, angle: 90)
		set1.drawFilledEnabled = true

		let data = LineChartData(dataSet: set1)
		let leftAxis = lineChartView.leftAxis
		leftAxis.removeAllLimitLines()
		if maxValue! == 0 {
			leftAxis.axisMaximum = 100
		} else {
			leftAxis.axisMaximum = Double(maxValue!) + Double(maxValue!)*0.10
		}
		leftAxis.axisMinimum = 0
		lineChartView.data = data
		lineChartView.data?.notifyDataChanged()
		lineChartView.notifyDataSetChanged()
		lineChartView.animate(xAxisDuration: 0)
		lineChartView.rightAxis.enabled = false
		lineChartView.legend.enabled = false
		
		if InterfaceStyle() == InterfaceStyle.Dark {
			set1.colors = [NSUIColor.white]
			set1.valueColors = [NSUIColor.white]
			leftAxis.labelTextColor = NSUIColor.white
		} else {
			set1.colors = [NSUIColor.black]
			set1.valueColors = [NSUIColor.black]
			leftAxis.labelTextColor = NSUIColor.black
		}
	   
   }
	
	func initGraph() {
        chartDataEntryArray = [ChartDataEntry]()
        values = [Int]()
        index = 0
        
        let set1 = LineChartDataSet(entries: chartDataEntryArray)
        set1.drawIconsEnabled = false
        set1.lineDashLengths = [5, 2.5]
        set1.highlightLineDashLengths = [5, 2.5]

        
        set1.lineWidth = 1
        set1.circleRadius = 3
        set1.drawCircleHoleEnabled = false
        set1.valueFont = .systemFont(ofSize: 9)
        set1.formLineDashLengths = [5, 2.5]
        set1.formLineWidth = 1
        set1.formSize = 15
		let leftAxis = lineChartView.leftAxis
        leftAxis.removeAllLimitLines()
        leftAxis.axisMaximum = 100
        leftAxis.axisMinimum = 0
		lineChartView.rightAxis.enabled = false
        
        
        let gradientColors = [ChartColorTemplates.colorFromString("#00ff0000").cgColor,
                              ChartColorTemplates.colorFromString("#ffff0000").cgColor]
        let gradient = CGGradient(colorsSpace: nil, colors: gradientColors as CFArray, locations: nil)!
        
        set1.fillAlpha = 1
        set1.fill = Fill(linearGradient: gradient, angle: 90) //.linearGradient(gradient, angle: 90)
        set1.drawFilledEnabled = true
		lineChartView.xAxis.axisMinimum = Double(-30)
        lineChartView.xAxis.axisMaximum = Double(1)
		lineChartView.pinchZoomEnabled = false
		lineChartView.chartDescription?.enabled = false
		lineChartView.dragEnabled = false
		lineChartView.setScaleEnabled(false)
		lineChartView.xAxis.enabled = false
        
        let data = LineChartData(dataSet: set1)
        
        lineChartView.data = data
		lineChartView.legend.enabled = false
		
		if InterfaceStyle() == InterfaceStyle.Dark {
			set1.colors = [NSUIColor.white]
			set1.valueColors = [NSUIColor.white]
			leftAxis.labelTextColor = NSUIColor.white
		} else {
			set1.colors = [NSUIColor.black]
			set1.valueColors = [NSUIColor.black]
			leftAxis.labelTextColor = NSUIColor.black
		}
    }
	
	
	
}

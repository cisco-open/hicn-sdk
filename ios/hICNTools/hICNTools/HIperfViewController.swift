//
//  HIperfViewController.swift
//  hICNTools
//
//  Created by manangel on 3/12/20.
//  Copyright © 2020 manangel. All rights reserved.
//

import UIKit
import Charts
import Panels

class HIperfViewController: BaseViewController {
    lazy var panelManager = Panels(target: self)
    lazy var panel = UIStoryboard.instantiatePanel(identifier: "PanelDetails")
    var panelConfiguration: PanelConfiguration!
    var chartDataEntryArray = [ChartDataEntry]()
    var values = [Int]()
    
    @IBOutlet weak var fixedWindowSizeSwitch: UISwitch!
    @IBOutlet weak var rtcProtocolSwitch: UISwitch!
    @IBOutlet weak var lifeTime: UITextField!
    @IBOutlet weak var dropFactor: UITextField!
    @IBOutlet weak var betaFactor: UITextField!
    @IBOutlet weak var hicnPrefix: UITextField!
    @IBOutlet weak var windowSize: UITextField!
    @IBOutlet var chartView: LineChartView!
    @IBOutlet weak var startButton: UIButton!
    @IBOutlet weak var stopButton: UIButton!
    
    var index = Int(0)
    let constants = Constants()
    let savedValues = UserDefaults.standard
    
    var timer = RepeatingTimer(timeInterval: 1  )
    
    
    struct defaultsKeys {
        static let hicnPrefix = "hicnPrefix"
        static let beta = "beta"
        static let dropFactor = "dropFactor"
        static let lifeTime = "lifeTime"
        static let rtcProtocol = "rtcProtocol"
        static let fixedWindowSize = "fixedWindowSize"
        static let windowSize = "windowSize"

    }
    
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        self.title = "hIperf"
        chartView.delegate = self
        
        chartView.chartDescription?.enabled = false
        chartView.dragEnabled = true
        chartView.setScaleEnabled(true)
        chartView.pinchZoomEnabled = true
        
        chartView.xAxis.gridLineDashLengths = [10, 10]
        chartView.xAxis.gridLineDashPhase = 0
        
    
        let leftAxis = chartView.leftAxis
        leftAxis.removeAllLimitLines()
        leftAxis.axisMaximum = 100
        leftAxis.axisMinimum = 0
        leftAxis.gridLineDashLengths = [5, 5]
        leftAxis.drawLimitLinesBehindDataEnabled = true
        
        chartView.rightAxis.enabled = false
        chartView.xAxis.enabled = false
        //[_chartView.viewPortHandler setMaximumScaleY: 2.f];
        //[_chartView.viewPortHandler setMaximumScaleX: 2.f];

        let marker = BalloonMarker(color: UIColor(white: 180/255, alpha: 1),
                                   font: .systemFont(ofSize: 12),
                                   textColor: .white,
                                   insets: UIEdgeInsets(top: 8, left: 8, bottom: 20, right: 8))
        marker.chartView = chartView
        marker.minimumSize = CGSize(width: 80, height: 40)
        chartView.marker = marker
        chartView.legend.enabled = false
        
        initGraph()
        
        if savedValues.string(forKey: defaultsKeys.hicnPrefix) == nil {
           hicnPrefix.text = String(constants.DEFAULT_HICNPREFIX)
        } else {
           hicnPrefix.text = savedValues.string(forKey: defaultsKeys.hicnPrefix)
        }

        if savedValues.string(forKey: defaultsKeys.beta) == nil {
            betaFactor.text = String(constants.DEFAULT_BETA)
        } else {
            betaFactor.text = savedValues.string(forKey: defaultsKeys.beta)
        }
        
        if savedValues.string(forKey: defaultsKeys.dropFactor) == nil {
            dropFactor.text = String(constants.DEFAULT_DROP_FACTOR)
        } else {
            dropFactor.text = savedValues.string(forKey: defaultsKeys.dropFactor)
        }
        
        if savedValues.string(forKey: defaultsKeys.lifeTime) == nil {
            lifeTime.text = String(constants.DEFAULT_LIFETIME)
        } else {
            lifeTime.text = savedValues.string(forKey: defaultsKeys.lifeTime)
        }
        
        if savedValues.object(forKey: defaultsKeys.rtcProtocol) == nil {
            rtcProtocolSwitch.isOn = constants.DEFAULT_RTCPROTOCOL
        } else {
            rtcProtocolSwitch.isOn = savedValues.bool(forKey: defaultsKeys.rtcProtocol)
        }
        
        if savedValues.object(forKey: defaultsKeys.fixedWindowSize) == nil {
            fixedWindowSizeSwitch.isOn = constants.DEFAULT_FIXEDWINDOWSIZE
            windowSize.isEnabled = constants.DEFAULT_FIXEDWINDOWSIZE
        } else {
            fixedWindowSizeSwitch.isOn = savedValues.bool(forKey: defaultsKeys.fixedWindowSize)
            windowSize.isEnabled = fixedWindowSizeSwitch.isOn
        }
        
        if savedValues.string(forKey: defaultsKeys.windowSize) == nil {
            windowSize.text = String(constants.DEFAULT_WINDOW_SIZE)
        } else {
            windowSize.text = savedValues.string(forKey: defaultsKeys.windowSize)
        }
    
    }

    override func updateChartData() {
        if self.shouldHideData {
        chartView.data = nil
            return
        }
        
        //self.setDataCount(Int(sliderX.value), range: UInt32(sliderY.value))
        self.initGraph()
    }
    
    func initGraph() {
        chartDataEntryArray = [ChartDataEntry]()
        values = [Int]()
        index = 0
        
        let set1 = LineChartDataSet(entries: chartDataEntryArray)
        set1.drawIconsEnabled = false
        
        set1.lineDashLengths = [5, 2.5]
        set1.highlightLineDashLengths = [5, 2.5]
        set1.setColor(.black)
        set1.setCircleColor(.black)
        set1.lineWidth = 1
        set1.circleRadius = 3
        set1.drawCircleHoleEnabled = false
        set1.valueFont = .systemFont(ofSize: 9)
        set1.formLineDashLengths = [5, 2.5]
        set1.formLineWidth = 1
        set1.formSize = 15
        let leftAxis = chartView.leftAxis
        leftAxis.removeAllLimitLines()
        leftAxis.axisMaximum = 100
        leftAxis.axisMinimum = 0
        
        
        let gradientColors = [ChartColorTemplates.colorFromString("#00ff0000").cgColor,
                              ChartColorTemplates.colorFromString("#ffff0000").cgColor]
        let gradient = CGGradient(colorsSpace: nil, colors: gradientColors as CFArray, locations: nil)!
        
        set1.fillAlpha = 1
        set1.fill = Fill(linearGradient: gradient, angle: 90) //.linearGradient(gradient, angle: 90)
        set1.drawFilledEnabled = true
        chartView.xAxis.axisMinimum = Double(-30)
        chartView.xAxis.axisMaximum = Double(1)
        
        let data = LineChartData(dataSet: set1)
        
        chartView.data = data
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
        chartView.xAxis.axisMinimum = Double(self.index - 30)
        chartView.xAxis.axisMaximum = Double(self.index + 1)

        let set1 = LineChartDataSet(entries: chartDataEntryArray)
        set1.drawIconsEnabled = false
        
        set1.lineDashLengths = [5, 2.5]
        set1.highlightLineDashLengths = [5, 2.5]
        set1.setColor(.black)
        set1.setCircleColor(.black)
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
        set1.fill = Fill(linearGradient: gradient, angle: 90) //.linearGradient(gradient, angle: 90)
        set1.drawFilledEnabled = true
        
        let data = LineChartData(dataSet: set1)
        let leftAxis = chartView.leftAxis
        leftAxis.removeAllLimitLines()
        leftAxis.axisMaximum = Double(maxValue! + 20)
        leftAxis.axisMinimum = 0
        chartView.data = data
        chartView.data?.notifyDataChanged()
        chartView.notifyDataSetChanged()
        chartView.animate(xAxisDuration: 0)
        
    }

    @IBAction func startButton(_ sender: Any) {
        
        startButton.isEnabled = false
        stopButton.isEnabled = true
        
        savedValues.set(hicnPrefix.text, forKey: defaultsKeys.hicnPrefix)
        savedValues.set(betaFactor.text, forKey: defaultsKeys.beta)
        savedValues.set(dropFactor.text, forKey: defaultsKeys.dropFactor)
        savedValues.set(lifeTime.text, forKey: defaultsKeys.lifeTime)
        savedValues.set(rtcProtocolSwitch.isOn, forKey: defaultsKeys.rtcProtocol)
        savedValues.set(fixedWindowSizeSwitch.isOn, forKey: defaultsKeys.fixedWindowSize)
        savedValues.set(windowSize.text, forKey: defaultsKeys.windowSize)
        timer = RepeatingTimer(timeInterval: 1  )
        print(hicnPrefix.text!)
        var hicnPrefixCChar: [CChar]?
        let betaFactorFloat = Float(betaFactor.text!) ?? 0
        let dropFactorFloat = Float(dropFactor.text!) ?? 0
        var windowSizeInt = 0
        if self.fixedWindowSizeSwitch.isOn == true {
            windowSizeInt = Int(windowSize.text!) ?? 0
        }
        
        let rtcProtocol = rtcProtocolSwitch.isOn == true ? 1 : 0
        let interestLifetimeInt = Int(lifeTime.text!) ?? 0
        let queue = DispatchQueue(label: "hiperf")
        hicnPrefixCChar = self.hicnPrefix.text?.cString(using: .utf8)
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
    
    
    @IBAction func stopButton(_ sender: Any) {
        timer.suspend()
        
        stopHiperf()
        
        startButton.isEnabled = true
        stopButton.isEnabled = false
    }
    @IBAction func rtcProtocolOnChange(_ sender: Any) {
        if self.rtcProtocolSwitch.isOn {
            print("rtcProtocolOn")
        } else {
            print("rtcProtocolOff")
        }
        
    }
    @IBAction func fixedWindowSizeOnChange(_ sender: Any) {
        if self.fixedWindowSizeSwitch.isOn {
            self.windowSize.isEnabled = true
        } else {
            self.windowSize.isEnabled = false
        }
    }
    
}

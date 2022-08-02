//
//  File.swift
//  Hybrid ICN Network Service
//

import Foundation

extension String {
    func characterRowAndLineAt(position: Int) -> (character: String, line: Int, column:Int, lastLine: String)? {
        var lineNumber = 0
        var characterPosition = 0
        for line in components(separatedBy: .newlines) {
            lineNumber += 1
            var columnNumber = 0
            for column in line {
                characterPosition += 1
                columnNumber += 1
                if characterPosition == position {
                    return (String(column), lineNumber, columnNumber, (line.hasPrefix("> ") ? String(line.dropFirst(2)) : line) )
                }
            }
            characterPosition += 1
            if characterPosition == position {
                return ("\n", lineNumber, columnNumber+1, (line.hasPrefix("> ") ? String(line.dropFirst(2)) : line) )
            }
        }
        return nil
    }

    func condenseWhitespace() -> String {
        let components = self.components(separatedBy: NSCharacterSet.whitespacesAndNewlines)
        return components.filter { !$0.isEmpty }.joined(separator: " ")
    }

}

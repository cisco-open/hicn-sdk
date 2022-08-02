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
//  Constants.swift
//  HiGet4iOS
//

import Foundation

public class Constants {
    let DEFAULT_NEXT_HOP_PORT = 11111
    let DEFAULT_SOURCE_PORT = 11111
    let DEFAULT_NEXT_HOP_IP = "10.60.17.139"
    let CONFIGURATION_FILE_NAME = "config.cfg"
    let FORWARDER_DISABLED = "Forwarder Disabled"
    let FORWARDER_ENABLED = "Forwarder Enabled"
    let DEFAULT_BETA = 0.99
    let DEFAULT_HICNPREFIX = "b001::1"
    let DEFAULT_DROP_FACTOR = 0.03
    let DEFAULT_LIFETIME = 1000
    let DEFAULT_WINDOW_SIZE = 100
    let DEFAULT_RTCPROTOCOL = false
    let DEFAULT_FIXEDWINDOWSIZE = true
}


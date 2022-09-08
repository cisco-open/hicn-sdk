/*
 * Copyright (c) 2019 Cisco and/or its affiliates.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.fd.hicn.hicntools.service;

public class Constants {

    //HiPing Constants
    public static final String HIPING = "HiPing";
    public static String DEFAULT_HIPING_HICN_NAME = "b001::1";
    public static int DEFAULT_HIPING_SOURCE_PORT = 9695;
    public static int DEFAULT_HIPING_DESTINATION_PORT = 8080;
    public static int DEFAULT_HIPING_TTL = 64;
    public static int DEFAULT_HIPING_PING_INTERVAL = 1000000; //us
    public static int DEFAULT_HIPING_MAX_PINGS = 100;
    public static int DEFAULT_HIPING_LIFETIME = 500; //ms
    public static boolean DEFAULT_HIPING_OPEN_TCP_CONNECTION = true;
    public static boolean DEFAULT_HIPING_SEND_SYN_MESSAGE = false;
    public static boolean DEFAULT_HIPING_SEND_ACK_MESSAGE = false;
    public static boolean DEFAULT_HIPING_AUTOSCROLL = true;
    public static int MAX_HIPING_TIME_LINECHART_XAXIS = 30;


    //HiPerf Constants

    public static final String HIPERF = "HiPerf";
    public static String DEFAULT_HIPERF_HICN_NAME = "b001::1";
    public static double DEFAULT_HIPERF_RAAQM_BETA_PARAMETER = 0.99;
    public static double DEFAULT_HIPERF_RAAQM_DROP_FACTOR_PARAMETER = 0.003;
    public static boolean DEFAULT_HIPERF_FIXED_WINDOW_SIZE = false;
    public static int DEFAULT_HIPERF_WINDOW_SIZE = 10;
    public static int DEFAULT_HIPERF_STATS_INTERVAL = 1000;
    public static boolean DEFAULT_HIPERF_RTC_PROTOCOL = false;
    public static String HIPERF_FORMAT_DATA = "yyyy-MM-dd hh:mm:ss";

    //HiGet Constants

    public static final String HIGET = "HiGet";
    public static String DEFAULT_HIGET_URL = "http://webserver/sintel/mpd";
    public static String DEFAULT_HIGET_DOWNLOAD_PATH = "/sdcard/HiGetAndroid";
    public static String HIGET_UNDERSCORE = "_";
    public static String HIGET_DASH = "-";
    public static String HIGET_FORMAT_DATA = "yyyy-MM-dd hh:mm:ss";

}

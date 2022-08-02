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

#include <jni.h>
#include <unistd.h>
#include <stdio.h>
#include <event2/event.h>
#include <android/log.h>
#include <stdbool.h>
#include <hicn/core/forwarder.h>
#include <parc/security/parc_Security.h>
#include <parc/security/parc_IdentityFile.h>

#include <parc/algol/parc_Memory.h>
#include <parc/algol/parc_SafeMemory.h>
#include <parc/algol/parc_List.h>
#include <parc/algol/parc_ArrayList.h>
#include <hicn/core/dispatcher.h>
#include <parc/algol/parc_FileOutputStream.h>
#include <parc/logging/parc_LogLevel.h>
#include <parc/logging/parc_LogReporterFile.h>
#include <parc/logging/parc_LogReporterTextStdout.h>

#include <parc/assert/parc_Assert.h>

#include <hicn/facemgr.h>
#include <hicn/policy.h>

#include <hicn/util/ip_address.h>
#include <hicn/util/log.h>

#include <hicn/facemgr/cfg.h>
#include <hicn/facemgr/api.h>
#include <hicn/facemgr/loop.h>
#include <event2/event.h>

static bool _isRunning = false;
static JNIEnv *_env;
static jobject *_instance;
//forwarder
Forwarder *hicnFwd = NULL;


JNIEXPORT jboolean JNICALL
Java_com_cisco_hicn_hicnforwarderlibrary_supportlibrary_ForwarderLibrary_isRunningForwarder(JNIEnv *env,
                                                                          jobject instance) {
    return _isRunning;
}

static void _setLogLevelToLevel(int logLevelArray[LoggerFacility_END],
                                LoggerFacility facility,
                                const char *levelString) {
    PARCLogLevel level = parcLogLevel_FromString(levelString);

    if (level < PARCLogLevel_All) {
        // we have a good facility and level
        logLevelArray[facility] = level;
    } else {
        printf("Invalid log level string %s\n", levelString);
    }
}

/**
 * string: "facility=level"
 * Set the right thing in the logger
 */
static void _setLogLevel(int logLevelArray[LoggerFacility_END],
                         const char *string) {
    char *tofree = parcMemory_StringDuplicate(string, strlen(string));
    char *p = tofree;

    char *facilityString = strtok(p, "=");
    if (facilityString) {
        char *levelString = strtok(NULL, "=");

        if (strcasecmp(facilityString, "all") == 0) {
            for (LoggerFacility facility = 0; facility < LoggerFacility_END;
                 facility++) {
                _setLogLevelToLevel(logLevelArray, facility, levelString);
            }
        } else {
            LoggerFacility facility;
            for (facility = 0; facility < LoggerFacility_END; facility++) {
                if (strcasecmp(facilityString, logger_FacilityString(facility)) == 0) {
                    break;
                }
            }

            if (facility < LoggerFacility_END) {
                _setLogLevelToLevel(logLevelArray, facility, levelString);
            } else {
                printf("Invalid facility string %s\n", facilityString);
            }
        }
    }

    parcMemory_Deallocate((void **) &tofree);
}

JNIEXPORT void JNICALL
Java_com_cisco_hicn_hicnforwarderlibrary_supportlibrary_ForwarderLibrary_startForwarder(JNIEnv *env,
                                                                      jobject instance,
                                                                      jint capacity,
                                                                      jint logLevel) {


    if (!_isRunning) {
        _isRunning = true;
        __android_log_print(ANDROID_LOG_DEBUG, "HicnFwdWrap", "starting HicnFwd...");
        _env = env;
        _instance = &instance;

        Logger *logger = NULL;

        PARCLogReporter *stdoutReporter = parcLogReporterTextStdout_Create();
        logger = logger_Create(stdoutReporter, parcClock_Wallclock());
        parcLogReporter_Release(&stdoutReporter);
        int logLevelArray[LoggerFacility_END];

        switch ((int) logLevel) {
            case 0:
                _setLogLevel(logLevelArray, "all=off");
                break;
            case 1:
                _setLogLevel(logLevelArray, "all=all");
                break;
            case 2:
                _setLogLevel(logLevelArray, "all=emergency");
                break;
            case 3:
                _setLogLevel(logLevelArray, "all=alert");
                break;
            case 4:
                _setLogLevel(logLevelArray, "all=critical");
                break;
            case 5:
                _setLogLevel(logLevelArray, "all=error");
                break;
            case 6:
                _setLogLevel(logLevelArray, "all=warning");
                break;
            case 7:
                _setLogLevel(logLevelArray, "all=notice");
                break;
            case 8:
                _setLogLevel(logLevelArray, "all=info");
                break;
            case 9:
                _setLogLevel(logLevelArray, "all=debug");
                break;
            default:
                _setLogLevel(logLevelArray, "all=off");
        }

        for (int i = 0; i < LoggerFacility_END; i++) {
            if (logLevelArray[i] > -1) {
                logger_SetLogLevel(logger, i, logLevelArray[i]);
            }
        }


        hicnFwd = forwarder_Create(logger);
        Configuration *configuration = forwarder_GetConfiguration(hicnFwd);
        if (capacity >= 0) {
            configuration_SetObjectStoreSize(configuration, capacity);
        }
        forwarder_SetupLocalListeners(hicnFwd, PORT_NUMBER);
        Dispatcher *dispatcher = forwarder_GetDispatcher(hicnFwd);
        dispatcher_Run(dispatcher);
        __android_log_print(ANDROID_LOG_DEBUG, "HicnFwdWrap", "HicnFwd stopped...");
    }
}

JNIEXPORT void JNICALL
Java_com_cisco_hicn_hicnforwarderlibrary_supportlibrary_ForwarderLibrary_stopForwarder(JNIEnv *env,
                                                                     jobject instance) {
    if (_isRunning) {
        _isRunning = false;
        __android_log_print(ANDROID_LOG_DEBUG, "HicnFwdWrap", "stopping HicnFwd...");
        dispatcher_Stop(forwarder_GetDispatcher(hicnFwd));
        sleep(2);
        forwarder_Destroy(&hicnFwd);
    }
}

static bool bindSocketWrap(JNIEnv *env, jobject instance, int sock, const char *ifname) {
    jclass clazz = (*env)->GetObjectClass(env, instance);
    jmethodID methodID = (*env)->GetMethodID(env, clazz, "bindSocket", "(ILjava/lang/String;)Z");
    bool ret = false;
    if (methodID) {
        jstring ifnameStr = (*env)->NewStringUTF(env, ifname);
        ret = (*env)->CallBooleanMethod(env, instance, methodID, sock, ifnameStr);
    }
    return ret;
}

int bindSocket(int sock, const char *ifname) {
    if (!_env || !_instance) {
        __android_log_print(ANDROID_LOG_ERROR, "HicnFwdWrap",
                            "Call bindSocket, but JNI env/instance variables are not initialized.");
        return -1;
    }
    return bindSocketWrap(_env, *_instance, sock, ifname) ? 0 : -1;
}

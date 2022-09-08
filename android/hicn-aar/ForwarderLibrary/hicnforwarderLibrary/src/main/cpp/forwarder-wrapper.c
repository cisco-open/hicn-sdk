/*
 * Copyright (c) 2022 Cisco and/or its affiliates.
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

//#include <android/log.h>
#include <hicn/base/loop.h>
#include <hicn/config/configuration.h>
#include <hicn/core/forwarder.h>
#include <hicn/util/log.h>
#include <hicn/transport/config.h>
#include <jni.h>
#include <stdio.h>
#include <unistd.h>

static bool _isRunning = false;
static JNIEnv *_env;
static jobject *_instance;

forwarder_t *hicnFwd = NULL;
configuration_t *configuration;

JNIEXPORT jboolean JNICALL
Java_com_cisco_hicn_hicnforwarderlibrary_supportlibrary_ForwarderLibrary_isRunningForwarder(
    JNIEnv *env, jobject instance) {
    return _isRunning;
}

JNIEXPORT void JNICALL
Java_com_cisco_hicn_hicnforwarderlibrary_supportlibrary_ForwarderLibrary_startForwarder(
    JNIEnv *env, jobject instance, jint capacity, jint logLevel) {
    if (_isRunning) return;

    _isRunning = true;
    DEBUG("Starting HicnFwd...");
    _env = env;
    _instance = &instance;

    MAIN_LOOP = loop_create();
    if (!MAIN_LOOP) {
        ERROR("Could not create forwarder main loop");
        return;
    }

    configuration_t *configuration = configuration_create();
    // configuration_set_loglevel(configuration, logLevel);
    log_conf.log_level = logLevel;
    hicnFwd = forwarder_create(configuration);
    if (!hicnFwd) {
        ERROR("Forwarder initialization failed");
        return;
    }

    forwarder_setup_local_listeners(hicnFwd,
                                    configuration_get_port(configuration));

    if (loop_dispatch(MAIN_LOOP) < 0) {
        ERROR("Failed to run forwarder main loop");
        forwarder_free(hicnFwd);
        return;
    }

    _isRunning = false;

    if (loop_undispatch(MAIN_LOOP) < 0) {
        ERROR("Failed to undispatch main loop");
    }

    forwarder_free(hicnFwd);
    loop_free(MAIN_LOOP);
    MAIN_LOOP = NULL;
    DEBUG("Forwarder stopped...");
}

JNIEXPORT void JNICALL
Java_com_cisco_hicn_hicnforwarderlibrary_supportlibrary_ForwarderLibrary_stopForwarder(
    JNIEnv *env, jobject instance) {
    if (!_isRunning) return;

    _isRunning = false;
    DEBUG("Stopping forwarder");

    loop_break(MAIN_LOOP);
}

static bool bindSocketWrap(JNIEnv *env, jobject instance, int sock,
                           const char *ifname) {
    jclass clazz = (*env)->GetObjectClass(env, instance);
    jmethodID methodID =
        (*env)->GetMethodID(env, clazz, "bindSocket", "(ILjava/lang/String;)Z");
    bool ret = false;
    if (methodID) {
        jstring ifnameStr = (*env)->NewStringUTF(env, ifname);
        ret =
            (*env)->CallBooleanMethod(env, instance, methodID, sock, ifnameStr);
    }
    return ret;
}

int bindSocket(int sock, const char *ifname) {
    if (!_env || !_instance) {
        ERROR("Call bindSocket with uninitialized JNI variables");
        return -1;
    }
    return bindSocketWrap(_env, *_instance, sock, ifname) ? 0 : -1;
}

JNIEXPORT jstring JNICALL
Java_com_cisco_hicn_hicnforwarderlibrary_supportlibrary_ForwarderLibrary_getForwarderVersion(
        JNIEnv *env, jobject thiz) {
    char version[10];
    sprintf(version, "v%s.%s.%s", HICNTRANSPORT_VERSION_MAJOR, HICNTRANSPORT_VERSION_MINOR, HICNTRANSPORT_VERSION_PATCH);
    return (*env)->NewStringUTF(env,version); ;
}
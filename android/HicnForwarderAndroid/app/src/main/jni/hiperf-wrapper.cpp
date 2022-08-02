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

#include <jni.h>
#include <string>
#include <android/log.h>

#include "hi_perf.h"


#include <hicn/transport/http/client_connection.h>

#if UINTPTR_MAX == 0xffffffffffffffffULL
# define BUILD_64   1
#endif


#define FACEMGR_ANDROID_UTILITY_CLASS "com/cisco/hicn/forwarder/supportlibrary/AndroidUtility"
static JavaVM *g_VM;
JNIEnv *j_env;

transport::interface::HIperfClient *hiperfClient = nullptr;

extern "C"
JNIEXPORT void JNICALL
Java_com_cisco_hicn_forwarder_supportlibrary_Hiperf_startHiPerf(JNIEnv *env, jobject thiz,
                                                                jstring hicn_name,
                                                                jdouble beta_parameter,
                                                                jdouble drop_factor_parameter,
                                                                jint window_size,
                                                                jlong stats_interval,
                                                                jboolean rtc_protocol,
                                                                jlong interest_lifetime) {
    const char *hicnName = env->GetStringUTFChars(hicn_name, 0);


    JavaVM *jvm = NULL;
    env->GetJavaVM(&jvm);
    jclass cls = env->FindClass(FACEMGR_ANDROID_UTILITY_CLASS);
    transport::interface::ClientConfiguration client_configuration;
    client_configuration.name = std::string(hicnName);
    client_configuration.rtc_ = (bool) rtc_protocol;
    client_configuration.beta = (double) beta_parameter;
    client_configuration.drop_factor = (double) drop_factor_parameter;
    client_configuration.jvm = jvm;
    client_configuration.cls = reinterpret_cast<jclass>(env->NewGlobalRef(cls));
    client_configuration.interest_lifetime_ = (long) interest_lifetime;
    if ((int) window_size >= 0) {
        client_configuration.window = (int) window_size;
    }
    client_configuration.report_interval_milliseconds_ = (long) stats_interval;

    if (hiperfClient) {
        delete (hiperfClient);
        hiperfClient = nullptr;
    }
    hiperfClient = new transport::interface::HIperfClient(client_configuration);

    if (hiperfClient->setup() != ERROR_SETUP) {
        hiperfClient->run();
    }

    env->ReleaseStringUTFChars(hicn_name, hicnName);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_cisco_hicn_forwarder_supportlibrary_Hiperf_stopHiPerf(JNIEnv *env, jobject thiz) {

    if (hiperfClient)
        hiperfClient->reset();

}

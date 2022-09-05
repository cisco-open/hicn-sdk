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
#include <string>
#include <unistd.h>
#include <android/log.h>


#include "hi_ping.h"
#include "hi_perf.h"


#include <hicn/transport/http/client_connection.h>

#if UINTPTR_MAX == 0xffffffffffffffffULL
# define BUILD_64   1
#endif

static JavaVM *g_VM;
JNIEnv *j_env;
std::unique_ptr<transport::core::ping::Client> ping;

transport::interface::HIperfClient *hiperfClient = nullptr;

transport::http::HTTPClientConnection connection;

extern "C"
JNIEXPORT void JNICALL
Java_io_fd_hicn_hicntools_ui_fragments_HiPingFragment_stopPing(JNIEnv *env, jobject instance) {

    if (ping) {
        ping->afterSignal();
    }

}

extern "C"
JNIEXPORT void JNICALL
Java_io_fd_hicn_hicntools_ui_fragments_HiPingFragment_startPing(JNIEnv *env, jobject instance,
                                                                jstring hicnName_,
                                                                jshort sourcePort, jshort destPort,
                                                                jlong ttl, jlong pingInterval,
                                                                jlong maxPing, jlong lifeTime,
                                                                jboolean openTcpConnection,
                                                                jboolean sendSynMessage,
                                                                jboolean sendAckMessage) {
    const char *hicnName = env->GetStringUTFChars(hicnName_, 0);
    jclass cls = env->GetObjectClass(instance);
    jmethodID hipingLogCallback = env->GetMethodID(cls, "hipingLogCallback",
                                                   "(Ljava/lang/String;)V");
    jmethodID hipingUpdateGraphCallback = env->GetMethodID(cls, "hipingUpdateGraphCallback",
                                                           "(I)V");
    if (ping) {
        ping.reset();
    }
    transport::core::ping::Configuration *c = new transport::core::ping::Configuration();
    c->ttl_ = (uint8_t) ttl;
    c->pingInterval_ = (long) pingInterval;
    c->maxPing_ = (long) maxPing;
    c->srcPort_ = (short) sourcePort;
    c->dstPort_ = (short) destPort;
    c->name_ = std::string(hicnName);
    c->interestLifetime_ = (long) lifeTime;
    c->verbose_ = true;
    c->dump_ = false;
    c->always_syn_ = (bool) sendSynMessage;
    c->always_ack_ = (bool) sendAckMessage;
    c->open_ = (bool) openTcpConnection;
    c->quiet_ = true;
    c->hipingLogCallback = hipingLogCallback;
    c->hipingUpdateGraphCallback = hipingUpdateGraphCallback;
    c->instance = instance;
    c->env = env;
    if (ping) {
        ping.reset();
    } else {
        ping = std::make_unique<transport::core::ping::Client>(c);
    }
    auto t0 = std::chrono::steady_clock::now();

    ping->ping();
    auto t1 = std::chrono::steady_clock::now();
    __android_log_print(ANDROID_LOG_INFO, TAG_HIPING, "Elapsed time: %lld",
                        std::chrono::duration_cast<std::chrono::microseconds>(t1 - t0).count());

    std::stringstream ss;
    ss << "Stop ping";
    ss << "Sent: " << ping->getSent() << " Received: " << ping->getReceived()
       << " Timeouts: " << ping->getTimeout();
    env->CallVoidMethod(instance, hipingLogCallback, env->NewStringUTF(ss.str().c_str()));
    __android_log_print(ANDROID_LOG_INFO, TAG_HIPING, "%s", ss.str().c_str());

    env->ReleaseStringUTFChars(hicnName_, hicnName);
}

extern "C"
JNIEXPORT void JNICALL
Java_io_fd_hicn_hicntools_ui_fragments_HiPerfFragment_startHiPerf(JNIEnv *env, jobject instance,
                                                                  jstring hicnName_,
                                                                  jdouble betaParameter,
                                                                  jdouble dropFactorParameter,
                                                                  jint windowSize,
                                                                  jlong statsInterval,
                                                                  jboolean rtcProtocol) {
    const char *hicnName = env->GetStringUTFChars(hicnName_, 0);
    jclass cls = env->GetObjectClass(instance);
    //jmethodID hiperfLogCallback = env->GetMethodID(cls, "hiperfLogCallback",
    //                                             "(Ljava/lang/String;)V");
    //jmethodID hiperfUpdateGraphCallback = env->GetMethodID(cls, "hiperfUpdateGraphCallback",
    //                                                   "(I)V");
    //jclass cls2 = env->FindClass("io/fd/hicn/hicntools/ui/fragments/HiPerfFragment");

    JavaVM *jvm = NULL;
    env->GetJavaVM(&jvm);
    //JNIEnv *env2;
    //int result = jvm->AttachCurrentThread(&env2,NULL);
    //jclass cls3 = env2->FindClass("io/fd/hicn/hicntools/ui/fragments/HiPerfFragment");


    transport::interface::ClientConfiguration client_configuration;
    client_configuration.name = std::string(hicnName);
    client_configuration.rtc_ = (bool) rtcProtocol;
    client_configuration.beta = (double) betaParameter;
    client_configuration.drop_factor = (double) dropFactorParameter;
//    client_configuration.hiperfLogCallback = hiperfLogCallback;
    //  client_configuration.hiperfUpdateGraphCallback = hiperfUpdateGraphCallback;
    // client_configuration.instance = instance;
    //client_configuration.env = env;
    //client_configuration.env2 = j_env;
    client_configuration.jvm = jvm;
    client_configuration.cls = reinterpret_cast<jclass>(env->NewGlobalRef(cls));
    if ((int) windowSize >= 0) {
        client_configuration.window = (int) windowSize;
    }
    client_configuration.report_interval_milliseconds_ = (long) statsInterval;

    if (hiperfClient) {
        delete (hiperfClient);
        hiperfClient = nullptr;
    }
    hiperfClient = new transport::interface::HIperfClient(client_configuration);

    if (hiperfClient->setup() != ERROR_SETUP) {
        hiperfClient->run();
    }

    env->ReleaseStringUTFChars(hicnName_, hicnName);
}

extern "C"
JNIEXPORT void JNICALL
Java_io_fd_hicn_hicntools_ui_fragments_HiPerfFragment_stopHiPerf(JNIEnv *env, jobject instance) {

    if (hiperfClient)
        hiperfClient->reset();

}


extern "C"
JNIEXPORT jbyteArray JNICALL
Java_io_fd_hicn_hicntools_ui_fragments_HiGetFragment_downloadFile(JNIEnv *env, jobject instance,
                                                                  jstring path_) {

    __android_log_print(ANDROID_LOG_INFO, "HI_GET_ANDROID", "Downloading...");
    const char *path = env->GetStringUTFChars(path_, 0);
    std::string name(path);
    env->ReleaseStringUTFChars(path_, path);
    connection.get(name);
    transport::http::HTTPPayload payload = connection.response()->getPayload();
#ifdef BUILD_64
    __android_log_print(ANDROID_LOG_INFO, "HI_GET_ANDROID", "Response packet size: %lu",
                        payload->length());
#else
    __android_log_print(ANDROID_LOG_INFO, "HI_GET_ANDROID","Response packet size: %u", response->length());
#endif
    if (payload->length() == 0) {
        jbyte temp[] = {};
        jbyteArray ret = env->NewByteArray(0);
        env->SetByteArrayRegion(ret, 0, 0, temp);
        return ret;
    }
    jbyteArray ret = env->NewByteArray(payload->length());
    env->SetByteArrayRegion(ret, 0, payload->length(),
                            (jbyte *) (payload->data()));
    return ret;
}

extern "C"
JNIEXPORT void JNICALL
Java_io_fd_hicn_hicntools_ui_fragments_HiGetFragment_stopDownload(JNIEnv *env, jobject instance) {

    connection.stop();

}

JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env;
    if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return -1;
    }
    g_VM = vm;
    j_env = env;
    // Get jclass with env->FindClass.
    // Register methods with env->RegisterNatives.

    return JNI_VERSION_1_6;
}
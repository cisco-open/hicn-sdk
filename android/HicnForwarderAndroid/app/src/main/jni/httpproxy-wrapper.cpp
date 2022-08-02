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



#include <hicn/http-proxy/http_proxy.h>

using namespace transport;
transport::HTTPProxy *proxy = nullptr;
static bool _isRunning = false;
struct Params : HTTPProxy::ClientParams, HTTPProxy::ServerParams {
    void printParams() override {
        if (client) {
            HTTPProxy::ClientParams::printParams();
        } else if (server) {
            HTTPProxy::ServerParams::printParams();
        } else {
            throw std::runtime_error(
                    "Proxy configured as client and server at the same time.");
        }

        std::cout << "\t"
                  << "N Threads: " << n_thread << std::endl;
    }

    HTTPProxy* instantiateProxyAsValue() {
        if (client) {
            HTTPProxy::ClientParams* p = dynamic_cast<HTTPProxy::ClientParams*>(this);
            return new transport::HTTPProxy(*p, n_thread);
        } else if (server) {
            HTTPProxy::ServerParams* p = dynamic_cast<HTTPProxy::ServerParams*>(this);
            return new transport::HTTPProxy(*p, n_thread);
        } else {
            throw std::runtime_error(
                    "Proxy configured as client and server at the same time.");
        }
    }

    bool client = false;
    bool server = false;
    std::uint16_t n_thread = 1;
};


extern "C"
JNIEXPORT void JNICALL
Java_com_cisco_hicn_forwarder_supportlibrary_HttpProxy_startHttpProxy(JNIEnv *env, jobject thiz,
        jstring prefix, jint listen_port) {
    const char *prefix_char = env->GetStringUTFChars(prefix, 0);
    __android_log_print(ANDROID_LOG_INFO, "HttpProxy_startHttpProxy", "%s", prefix_char);

    Params params;
    params.prefix = std::string(prefix_char);
    params.origin_address = "127.0.0.1";
    params.cache_size = "50000";
    params.mtu = "1500";
    params.content_lifetime = "7200;";  // seconds
    params.manifest = false;
    params.tcp_listen_port = listen_port;
    params.client = true;
    std::cout<<"prefix: " << params.first_ipv6_word  << " listen_port: " << listen_port << std::endl;
    proxy = params.instantiateProxyAsValue();
    _isRunning = true;
    proxy->run();
    delete proxy;
    env->ReleaseStringUTFChars(prefix, prefix_char);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_cisco_hicn_forwarder_supportlibrary_HttpProxy_stopHttpProxy(JNIEnv *env, jobject thiz) {
    if (proxy) {
        proxy->stop();
    }
}

extern "C"
JNIEXPORT jboolean JNICALL
        Java_com_cisco_hicn_forwarder_supportlibrary_HttpProxy_isRunningHttpProxy(JNIEnv *env,
                                                                                  jobject thiz) {
    return _isRunning;
}
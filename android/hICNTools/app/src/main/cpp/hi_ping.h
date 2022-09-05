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

#ifndef HICNTOOLS_HI_PING_H
#define HICNTOOLS_HI_PING_H


#include <jni.h>
#include <string>
#include <unistd.h>
#include <android/log.h>
#include <hicn/transport/core/global_object_pool.h>
#include <hicn/transport/core/interest.h>
#include <hicn/transport/interfaces/portal.h>
#include <hicn/transport/auth/verifier.h>
#include <hicn/transport/utils/log.h>

#define ASIO_STANDALONE
#include <asio/signal_set.hpp>
#include <asio/steady_timer.hpp>
#include <chrono>
#include <map>

#define TAG_HIPING "HiPing"
#define SYN_STATE 1
#define ACK_STATE 2

namespace transport {

    namespace core {

        namespace ping {
            typedef std::map<uint64_t, uint64_t> SendTimeMap;
            typedef auth::AsymmetricVerifier Verifier;

            class Configuration {
            public:
                uint64_t interestLifetime_;
                uint64_t pingInterval_;
                uint64_t maxPing_;
                uint64_t first_suffix_;
                std::string name_;
                std::string certificate_;
                uint16_t srcPort_;
                uint16_t dstPort_;
                bool verbose_;
                bool dump_;
                bool jump_;
                bool open_;
                bool always_syn_;
                bool always_ack_;
                bool quiet_;
                uint32_t jump_freq_;
                uint32_t jump_size_;
                uint8_t ttl_;
                jmethodID hipingLogCallback;
                jmethodID hipingUpdateGraphCallback;
                jobject instance;
                JNIEnv *env;

                Configuration();
            };

            class Client : interface::Portal::ConsumerCallback {
            public:
                Client(Configuration *c);

                ~Client();

                void setConfiguration(Configuration *c);

                void ping();

                void onError(std::error_code ec) override;

                void doPing();

                void afterSignal();

                void onContentObject(Interest &interest, ContentObject &object)  override;

                void onTimeout(Interest::Ptr &&interest) override;

                void reset();

                uint32_t getReceived();

                uint32_t getSent();

                uint32_t getTimeout();

            private:
                SendTimeMap send_timestamps_;
                interface::Portal portal_;
                asio::signal_set signals_;
                uint64_t sequence_number_;
                uint64_t last_jump_;
                uint64_t processed_;
                uint32_t state_;
                uint32_t sent_;
                uint32_t received_;
                uint32_t timeout_;
                std::unique_ptr<asio::steady_timer> timer_;
                Configuration *config_;
                Verifier verifier_;
                PARCKeyId *key_id_;

            };
        }
    }
}
#endif //HICNTOOLS_HI_PING_H

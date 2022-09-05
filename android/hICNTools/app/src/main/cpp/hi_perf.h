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

#ifndef HICNTOOLS_HI_PERF_H
#define HICNTOOLS_HI_PERF_H

#include <hicn/transport/config.h>
#include <hicn/transport/core/content_object.h>
#include <hicn/transport/core/interest.h>
#include <hicn/transport/interfaces/global_conf_interface.h>
#include <hicn/transport/interfaces/p2psecure_socket_consumer.h>
#include <hicn/transport/interfaces/p2psecure_socket_producer.h>
#include <hicn/transport/interfaces/socket_consumer.h>
#include <hicn/transport/interfaces/socket_producer.h>
#include <hicn/transport/auth/identity.h>
#include <hicn/transport/auth/signer.h>
#include <hicn/transport/utils/chrono_typedefs.h>
#include <hicn/transport/utils/literals.h>
#define ASIO_STANDALONE
#include <asio.hpp>
#include <cmath>
#include <fstream>
#include <iomanip>
#include <unordered_set>
#include <android/log.h>

#include <asio/signal_set.hpp>
#include <jni.h>

#define TAG_HIPERF "HiPerf"

namespace transport {

    namespace interface {

#ifndef ERROR_SUCCESS
#define ERROR_SUCCESS 0
#endif
#define ERROR_SETUP -5

/**
 * Container for command line configuration for hiperf client.
 */
        struct ClientConfiguration {
            ClientConfiguration();

            Name name;
            bool verify;
            double beta;
            double drop_factor;
            double window;
            bool virtual_download;
            std::shared_ptr<utils::MemBuf> receive_buffer;
            std::size_t download_size;
            std::uint32_t report_interval_milliseconds_;
            TransportProtocolAlgorithms transport_protocol_;
            bool rtc_;
            bool test_mode_;
            jclass cls;
            JavaVM *jvm;

        };

        class Rate {
        public:
            Rate();

            Rate(const std::string &rate);

            Rate(const Rate &other);

            Rate &operator=(const std::string &rate);

            std::chrono::microseconds getMicrosecondsForPacket(std::size_t packet_size);

        private:
            float rate_kbps_;
        };

        class RTCCallback;

        class Callback;

        class HIperfClient {
            typedef std::chrono::time_point<std::chrono::steady_clock> Time;
            typedef std::chrono::microseconds TimeDuration;

            friend class RTCCallback;

            friend class Callback;

        public:
            HIperfClient(const ClientConfiguration &conf);

            ~HIperfClient();

            void checkReceivedRtcContent(ConsumerSocket &c,
                                         const ContentObject &contentObject);

            bool verifyData(ConsumerSocket &c, const ContentObject &contentObject);

            void processLeavingInterest(ConsumerSocket &c, const Interest &interest);

            void handleTimerExpiration(ConsumerSocket &c,
                                       const TransportStatistics &stats);

            int setup();

            int run();

            void reset();

        private:
            class RTCCallback : public ConsumerSocket::ReadCallback {
                static constexpr std::size_t mtu = 1500;

            public:
                RTCCallback(HIperfClient
                            &hiperf_client);

                bool isBufferMovable() noexcept override;

                void getReadBuffer(uint8_t **application_buffer,
                                   size_t *max_length) override;

                void readDataAvailable(std::size_t length) noexcept override;

                size_t maxBufferSize() const override;

                void readError(const std::error_code ec) noexcept override;

                void readSuccess(std::size_t total_size) noexcept override;


            private:
                HIperfClient &client_;

            };

            class Callback : public ConsumerSocket::ReadCallback {
                static constexpr std::size_t read_size = 16 * 1024;
            public:

                Callback(HIperfClient
                         &hiperf_client);

                bool isBufferMovable() noexcept override;

                void getReadBuffer(uint8_t **application_buffer,
                                   size_t *max_length) override;

                void readDataAvailable(std::size_t length) noexcept override;

                void readBufferAvailable(
                        std::unique_ptr<utils::MemBuf> &&buffer) noexcept override;

                size_t maxBufferSize() const override;

                void readError(const std::error_code ec) noexcept override;

                void readSuccess(std::size_t total_size) noexcept override;

            private:
                HIperfClient &client_;
            };

            ClientConfiguration configuration_;
            Time t_stats_;
            Time t_download_;
            uint32_t total_duration_milliseconds_;
            uint64_t old_bytes_value_;
            asio::io_service io_service_;
            asio::signal_set signals_;
            uint32_t expected_seg_;
            std::unordered_set<uint32_t> lost_packets_;
            RTCCallback *rtc_callback_;
            Callback *callback_;
            std::unique_ptr<ConsumerSocket> consumer_socket_;
        };
    }
}


#endif //HICNTOOLS_HI_PERF_H

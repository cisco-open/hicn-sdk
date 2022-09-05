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

#include "hi_perf.h"

namespace transport {

    namespace interface {
        ClientConfiguration::ClientConfiguration()
                : name("b001::abcd", 0),
                  verify(false),
                  beta(-1.f),
                  drop_factor(-1.f),
                  window(-1),
                  virtual_download(true),
                  receive_buffer(nullptr),
                  download_size(0),
                  report_interval_milliseconds_(1000),
                  rtc_(false),
                  test_mode_(false) {}


        Rate::Rate() : rate_kbps_(0) {}

        Rate::Rate(const std::string &rate) {
            std::size_t found = rate.find("kbps");
            if (found != std::string::npos) {
                rate_kbps_ = std::stof(rate.substr(0, found));
            } else {
                throw std::runtime_error("Format " + rate + " not correct");
            }
        }

        Rate::Rate(const Rate &other) : rate_kbps_(other.rate_kbps_) {}

        Rate &Rate::operator=(const std::string &rate) {
            std::size_t found = rate.find("kbps");
            if (found != std::string::npos) {
                rate_kbps_ = std::stof(rate.substr(0, found));
            } else {
                throw std::runtime_error("Format " + rate + " not correct");
            }

            return *this;
        }

        std::chrono::microseconds Rate::getMicrosecondsForPacket(std::size_t packet_size) {
            return std::chrono::microseconds(
                    (uint32_t) std::round(packet_size * 1000.0 * 8.0 / (double) rate_kbps_));
        }

        HIperfClient::HIperfClient(const ClientConfiguration &conf)
                : configuration_(conf),
                  total_duration_milliseconds_(0),
                  old_bytes_value_(0),
                  signals_(io_service_, SIGINT),
                  expected_seg_(0),
                  lost_packets_(std::unordered_set<uint32_t>()),
                  rtc_callback_(configuration_.rtc_ ? new RTCCallback(*this) : nullptr),
                  callback_(configuration_.rtc_ ? nullptr : new Callback(*this)) {}


        HIperfClient::~HIperfClient() {
            if (callback_)
                delete (callback_);
            if (rtc_callback_)
                delete (rtc_callback_);

        }

        void HIperfClient::checkReceivedRtcContent(ConsumerSocket &c,
                                                   const ContentObject &contentObject) {
            if (!configuration_.test_mode_) return;

            JNIEnv *env;
            configuration_.jvm->AttachCurrentThread(&env, NULL);

            jmethodID hiperfPrintLog = env->GetStaticMethodID(configuration_.cls, "hioerfPrintLog",
                                                              "(Ljava/lang/String;)V");
            uint32_t receivedSeg = contentObject.getName().getSuffix();
            auto payload = contentObject.getPayload();

            if ((uint32_t) payload->length() == 8) {  // 8 is the size of the NACK
                // payload
                uint32_t *payloadPtr = (uint32_t *) payload->data();
                uint32_t productionSeg = *(payloadPtr);
                uint32_t productionRate = *(++payloadPtr);

                if (productionRate == 0) {
                    std::stringstream ss;
                    ss << "[STOP] producer is not producing content" << std::endl;
                    __android_log_print(ANDROID_LOG_INFO, TAG_HIPERF, "%s", ss.str().c_str());
                    env->CallStaticVoidMethod(configuration_.cls, hiperfPrintLog,
                                              env->NewStringUTF(ss.str().c_str()));
                    return;
                }

                if (receivedSeg < productionSeg) {
                    std::stringstream ss;
                    ss << "[OUT OF SYNCH] received NACK for " << receivedSeg
                       << ". Next expected packet " << productionSeg + 1
                       << std::endl;
                    __android_log_print(ANDROID_LOG_INFO, TAG_HIPERF, "%s", ss.str().c_str());
                    env->CallStaticVoidMethod(configuration_.cls, hiperfPrintLog,
                                              env->NewStringUTF(ss.str().c_str()));

                    expected_seg_ = productionSeg;
                } else if (receivedSeg > productionSeg) {

                    std::stringstream ss;
                    ss << "[WINDOW TO LARGE] received NACK for " << receivedSeg
                       << ". Next expected packet " << productionSeg << std::endl;
                    __android_log_print(ANDROID_LOG_INFO, TAG_HIPERF, "%s", ss.str().c_str());
                    env->CallStaticVoidMethod(configuration_.cls, hiperfPrintLog,
                                              env->NewStringUTF(ss.str().c_str()));
                }
                return;
            }

            if (receivedSeg > expected_seg_) {
                for (uint32_t i = expected_seg_; i < receivedSeg; i++) {
                    std::stringstream ss;
                    ss << "[LOSS] lost packet " << i << std::endl;
                    __android_log_print(ANDROID_LOG_INFO, TAG_HIPERF, "%s", ss.str().c_str());
                    env->CallStaticVoidMethod(configuration_.cls, hiperfPrintLog,
                                              env->NewStringUTF(ss.str().c_str()));

                    lost_packets_.insert(i);
                }
                expected_seg_ = receivedSeg + 1;
                return;
            } else if (receivedSeg < expected_seg_) {
                auto it = lost_packets_.find(receivedSeg);
                if (it != lost_packets_.end()) {

                    std::stringstream ss;
                    ss << "[RECOVER] recovered packet " << receivedSeg << std::endl;
                    __android_log_print(ANDROID_LOG_INFO, TAG_HIPERF, "%s", ss.str().c_str());
                    env->CallStaticVoidMethod(configuration_.cls, hiperfPrintLog,
                                              env->NewStringUTF(ss.str().c_str()));

                    lost_packets_.erase(it);
                } else {
                    std::stringstream ss;
                    ss << "[OUT OF ORDER] recevied " << receivedSeg << " expedted "
                       << expected_seg_ << std::endl;
                    __android_log_print(ANDROID_LOG_INFO, TAG_HIPERF, "%s", ss.str().c_str());
                    env->CallStaticVoidMethod(configuration_.cls, hiperfPrintLog,
                                              env->NewStringUTF(ss.str().c_str()));

                }
                return;
            }
            expected_seg_ = receivedSeg + 1;
        }

        bool HIperfClient::verifyData(ConsumerSocket &c, const ContentObject &contentObject) {
            JNIEnv *env;
            configuration_.jvm->AttachCurrentThread(&env, NULL);

            jmethodID hiperfPrintLog = env->GetStaticMethodID(configuration_.cls, "hioerfPrintLog",
                                                              "(Ljava/lang/String;)V");
            if (contentObject.getPayloadType() == PayloadType::DATA) {
                std::stringstream ss;
                ss << "VERIFY CONTENT" << std::endl;
                __android_log_print(ANDROID_LOG_INFO, TAG_HIPERF, "%s", ss.str().c_str());
                env->CallStaticVoidMethod(configuration_.cls, hiperfPrintLog,
                                          env->NewStringUTF(ss.str().c_str()));

            } else if (contentObject.getPayloadType() == PayloadType::MANIFEST) {
                std::stringstream ss;
                ss << "VERIFY MANIFEST" << std::endl;
                __android_log_print(ANDROID_LOG_INFO, TAG_HIPERF, "%s", ss.str().c_str());
                env->CallStaticVoidMethod(configuration_.cls, hiperfPrintLog,
                                          env->NewStringUTF(ss.str().c_str()));

            }

            return true;
        }

        void HIperfClient::processLeavingInterest(ConsumerSocket &c, const Interest &interest) {}

        void HIperfClient::handleTimerExpiration(ConsumerSocket &c,
                                                 const TransportStatistics &stats) {
            const char separator = ' ';
            const int width = 20;

            utils::TimePoint t2 = utils::SteadyClock::now();
            auto exact_duration =
                    std::chrono::duration_cast<utils::Milliseconds>(t2 - t_stats_);

            std::stringstream interval;
            interval << total_duration_milliseconds_ / 1000 << "-"
                     << total_duration_milliseconds_ / 1000 +
                        exact_duration.count() / 1000;

            std::stringstream bytes_transferred;
            bytes_transferred << std::fixed << std::setprecision(3)
                              << (stats.getBytesRecv() - old_bytes_value_) / 1000000.0
                              << std::setfill(separator) << "[MBytes]";

            std::stringstream bandwidth;
            bandwidth << ((stats.getBytesRecv() - old_bytes_value_) * 8) /
                         (exact_duration.count()) / 1000.0
                      << std::setfill(separator) << "[Mbps]";
            float bandwidthFloat = ((stats.getBytesRecv() - old_bytes_value_) * 8) /
                                   (exact_duration.count());

            std::stringstream window;
            window << stats.getAverageWindowSize() << std::setfill(separator)
                   << "[Interest]";

            std::stringstream avg_rtt;
            avg_rtt << stats.getAverageRtt() << std::setfill(separator) << "[us]";
            std::stringstream ss;
            ss << "Interval: " << interval.str() << std::endl;
            ss << "Transfer: " << bytes_transferred.str() << std::endl;
            ss << "Bandwidth: " << bandwidth.str() << std::endl;
            ss << "Retr: " << stats.getRetxCount() << std::endl;
            ss << "Cwnd: " << window.str() << std::endl;
            ss << "AvgRtt: " << avg_rtt.str() << std::endl;
            __android_log_print(ANDROID_LOG_INFO, TAG_HIPERF, "%s", ss.str().c_str());
            JNIEnv *env;
            configuration_.jvm->AttachCurrentThread(&env, NULL);

            jmethodID hiperfPrintLog = env->GetStaticMethodID(configuration_.cls, "hioerfPrintLog",
                                                              "(Ljava/lang/String;)V");


            env->CallStaticVoidMethod(configuration_.cls, hiperfPrintLog,
                                      env->NewStringUTF(ss.str().c_str()));

            jmethodID hiperfUpdateGraphCallback = env->GetStaticMethodID(configuration_.cls,
                                                                         "hiperfUpdateGraphCallback",
                                                                         "(I)V");
            env->CallStaticVoidMethod(configuration_.cls, hiperfUpdateGraphCallback,
                                      (int) bandwidthFloat);

            total_duration_milliseconds_ += (uint32_t) exact_duration.count();
            old_bytes_value_ = stats.getBytesRecv();
            t_stats_ = utils::SteadyClock::now();
        }

        int HIperfClient::setup() {
            int ret;

            // Set the transport algorithm
            TransportProtocolAlgorithms transport_protocol;

            if (configuration_.rtc_) {
                transport_protocol = RTC;
            } else if (configuration_.window < 0) {
                transport_protocol = RAAQM;
            } else {
                transport_protocol = CBR;
            }

            consumer_socket_ = std::make_unique<ConsumerSocket>(transport_protocol);


            if (consumer_socket_->setSocketOption(CURRENT_WINDOW_SIZE,
                                                  configuration_.window) ==
                SOCKET_OPTION_NOT_SET) {
                std::stringstream ss;
                ss << "ERROR -- Impossible to set the size of the window." << std::endl;
                __android_log_print(ANDROID_LOG_ERROR, TAG_HIPERF, "%s", ss.str().c_str());

//                configuration_.env->CallVoidMethod(configuration_.instance,
                //                                                 configuration_.hiperfLogCallback,
                //                                               configuration_.env->NewStringUTF(
                //                                                     ss.str().c_str()));

                return ERROR_SETUP;
            }

            if (transport_protocol == RAAQM && configuration_.beta != -1.f) {
                if (consumer_socket_->setSocketOption(RaaqmTransportOptions::BETA_VALUE,
                                                      configuration_.beta) ==
                    SOCKET_OPTION_NOT_SET) {
                    return ERROR_SETUP;
                }
            }

            if (transport_protocol == RAAQM && configuration_.drop_factor != -1.f) {
                if (consumer_socket_->setSocketOption(RaaqmTransportOptions::DROP_FACTOR,
                                                      configuration_.drop_factor) ==
                    SOCKET_OPTION_NOT_SET) {
                    return ERROR_SETUP;
                }
            }

            if (consumer_socket_->setSocketOption(
                    GeneralTransportOptions::VERIFIER, configuration_.verify) ==
                SOCKET_OPTION_NOT_SET) {
                return ERROR_SETUP;
            }

            ret = consumer_socket_->setSocketOption(
                    ConsumerCallbacksOptions::INTEREST_OUTPUT,
                    (ConsumerInterestCallback) std::bind(
                            &HIperfClient::processLeavingInterest, this, std::placeholders::_1,
                            std::placeholders::_2));

            if (ret == SOCKET_OPTION_NOT_SET) {
                return ERROR_SETUP;
            }

            if (!configuration_.rtc_) {
                ret = consumer_socket_->setSocketOption(
                        ConsumerCallbacksOptions::READ_CALLBACK, callback_);
            } else {
                ret = consumer_socket_->setSocketOption(
                        ConsumerCallbacksOptions::READ_CALLBACK, rtc_callback_);
            }

            if (ret == SOCKET_OPTION_NOT_SET) {
                return ERROR_SETUP;
            }

            if (configuration_.rtc_) {
                ret = consumer_socket_->setSocketOption(
                        ConsumerCallbacksOptions::CONTENT_OBJECT_INPUT,
                        (ConsumerContentObjectCallback) std::bind(
                                &HIperfClient::checkReceivedRtcContent, this,
                                std::placeholders::_1, std::placeholders::_2));
                if (ret == SOCKET_OPTION_NOT_SET) {
                    return ERROR_SETUP;
                }
            }

            ret = consumer_socket_->setSocketOption(
                    ConsumerCallbacksOptions::STATS_SUMMARY,
                    (ConsumerTimerCallback) std::bind(&HIperfClient::handleTimerExpiration,
                                                      this, std::placeholders::_1,
                                                      std::placeholders::_2));

            if (ret == SOCKET_OPTION_NOT_SET) {
                return ERROR_SETUP;
            }

            if (consumer_socket_->setSocketOption(
                    GeneralTransportOptions::STATS_INTERVAL,
                    configuration_.report_interval_milliseconds_) ==
                SOCKET_OPTION_NOT_SET) {
                return ERROR_SETUP;
            }

            consumer_socket_->connect();

            return ERROR_SUCCESS;
        }

        int HIperfClient::run() {
            std::stringstream ss;
            ss << "Starting download of " << configuration_.name << std::endl;
            __android_log_print(ANDROID_LOG_INFO, TAG_HIPERF, "%s", ss.str().c_str());
            JNIEnv *env;
            configuration_.jvm->AttachCurrentThread(&env, NULL);

            jmethodID hiperfPrintLog = env->GetStaticMethodID(configuration_.cls, "hioerfPrintLog",
                                                              "(Ljava/lang/String;)V");


            env->CallStaticVoidMethod(configuration_.cls, hiperfPrintLog,
                                      env->NewStringUTF(ss.str().c_str()));
            signals_.async_wait([this](const std::error_code &, const int &) {
                consumer_socket_->stop();
                io_service_.stop();
            });
            t_download_ = t_stats_ = std::chrono::steady_clock::now();
            consumer_socket_->asyncConsume(configuration_.name);
            io_service_.run();

            return ERROR_SUCCESS;
        }

        void HIperfClient::reset() {
            consumer_socket_->stop();
            io_service_.stop();
        }


        HIperfClient::RTCCallback::RTCCallback(HIperfClient &hiperf_client) : client_(
                hiperf_client) {
            client_.configuration_.receive_buffer = utils::MemBuf::create(mtu);
        }

        bool HIperfClient::RTCCallback::isBufferMovable() noexcept { return false; }

        void HIperfClient::RTCCallback::getReadBuffer(uint8_t **application_buffer,
                                                      size_t *max_length) {
            *application_buffer =
                    client_.configuration_.receive_buffer->writableData();
            *max_length = mtu;
        }

        void HIperfClient::RTCCallback::readDataAvailable(std::size_t length) noexcept {
            // Do nothing
            return;
        }

        size_t HIperfClient::RTCCallback::maxBufferSize() const { return mtu; }

        void HIperfClient::RTCCallback::readError(const std::error_code ec) noexcept {
            std::stringstream ss;
            ss << "Error while reading from RTC socket" << std::endl;
            __android_log_print(ANDROID_LOG_INFO, TAG_HIPERF, "%s", ss.str().c_str());
            JNIEnv *env;
            client_.configuration_.jvm->AttachCurrentThread(&env, NULL);

            jmethodID hiperfPrintLog = env->GetStaticMethodID(client_.configuration_.cls,
                                                              "hioerfPrintLog",
                                                              "(Ljava/lang/String;)V");


            env->CallStaticVoidMethod(client_.configuration_.cls, hiperfPrintLog,
                                      env->NewStringUTF(ss.str().c_str()));
            client_.io_service_.stop();
        }

        void HIperfClient::RTCCallback::readSuccess(std::size_t total_size) noexcept {
            std::stringstream ss;
            ss << "Data successfully read" << std::endl;
            __android_log_print(ANDROID_LOG_INFO, TAG_HIPERF, "%s", ss.str().c_str());
            JNIEnv *env;
            client_.configuration_.jvm->AttachCurrentThread(&env, NULL);

            jmethodID hiperfPrintLog = env->GetStaticMethodID(client_.configuration_.cls,
                                                              "hioerfPrintLog",
                                                              "(Ljava/lang/String;)V");


            env->CallStaticVoidMethod(client_.configuration_.cls, hiperfPrintLog,
                                      env->NewStringUTF(ss.str().c_str()));
        }

        HIperfClient::Callback::Callback(HIperfClient
                                         &hiperf_client) :
                client_(hiperf_client) {}

        bool HIperfClient::Callback::isBufferMovable() noexcept {
            return true;
        }

        void HIperfClient::Callback::getReadBuffer(uint8_t **application_buffer,
                                                   size_t *max_length) {
            // Not used
        }

        void HIperfClient::Callback::readDataAvailable(std::size_t length) noexcept {
            // Do nothing
            return;
        }

        void HIperfClient::Callback::readBufferAvailable(
                std::unique_ptr<utils::MemBuf> &&buffer) noexcept {
            if (client_.configuration_.receive_buffer) {
                client_.configuration_.receive_buffer->prependChain(std::move(buffer));
            } else {
                client_.configuration_.receive_buffer = std::move(buffer);
            }
        }

        size_t HIperfClient::Callback::maxBufferSize() const { return read_size; }

        void HIperfClient::Callback::readError(const std::error_code ec) noexcept {
            std::stringstream ss;
            ss << "Error " << ec.message() << " while reading from socket"
               << std::endl;
            __android_log_print(ANDROID_LOG_ERROR, TAG_HIPERF, "%s", ss.str().c_str());
            JNIEnv *env;
            client_.configuration_.jvm->AttachCurrentThread(&env, NULL);

            jmethodID hiperfPrintLog = env->GetStaticMethodID(client_.configuration_.cls,
                                                              "hioerfPrintLog",
                                                              "(Ljava/lang/String;)V");


            env->CallStaticVoidMethod(client_.configuration_.cls, hiperfPrintLog,
                                      env->NewStringUTF(ss.str().c_str()));
            client_.io_service_.stop();
        }

        void HIperfClient::Callback::readSuccess(std::size_t total_size) noexcept {
            Time t2 = std::chrono::steady_clock::now();
            TimeDuration dt =
                    std::chrono::duration_cast<TimeDuration>(t2 - client_.t_download_);
            long usec = (long) dt.count();

            std::stringstream ss;
            ss << "Content retrieved. Size: " << total_size << " [Bytes]"
               << std::endl;
            __android_log_print(ANDROID_LOG_INFO, TAG_HIPERF, "%s", ss.str().c_str());
            ss << "Elapsed Time: " << usec / 1000000.0 << " seconds -- "
               << (total_size * 8) * 1.0 / usec * 1.0 << " [Mbps]"
               << std::endl;
            __android_log_print(ANDROID_LOG_ERROR, TAG_HIPERF, "%s", ss.str().c_str());
            JNIEnv *env;
            client_.configuration_.jvm->AttachCurrentThread(&env, NULL);

            jmethodID hiperfPrintLog = env->GetStaticMethodID(client_.configuration_.cls,
                                                              "hioerfPrintLog",
                                                              "(Ljava/lang/String;)V");


            env->CallStaticVoidMethod(client_.configuration_.cls, hiperfPrintLog,
                                      env->NewStringUTF(ss.str().c_str()));
            client_.io_service_.stop();
        }

    }


}

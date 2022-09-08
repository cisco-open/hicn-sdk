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

#include "hi_ping.h"


namespace transport {

    namespace core {

        namespace ping {

            Configuration::Configuration() {
                interestLifetime_ = 500; // ms
                pingInterval_ = 1000000; // us
                maxPing_ = 10;           // number of interests
                first_suffix_ = 0;
                name_ = "b001::1"; // string
                srcPort_ = 9695;
                dstPort_ = 8080;
                verbose_ = false;
                dump_ = false;
                jump_ = false;
                open_ = false;
                always_syn_ = false;
                always_ack_ = false;
                quiet_ = false;
                jump_freq_ = 0;
                jump_size_ = 0;
                ttl_ = 64;
            }

            Client::Client(Configuration *c)
                    : portal_(), signals_(portal_.getIoService(), SIGINT) {
                // Let the main thread to catch SIGINT
                portal_.connect();
                portal_.setConsumerCallback(this);

                signals_.async_wait(std::bind(&Client::afterSignal, this));

                timer_.reset(new asio::steady_timer(portal_.getIoService()));
                config_ = c;
                sequence_number_ = config_->first_suffix_;
                last_jump_ = 0;
                processed_ = 0;
                state_ = SYN_STATE;
                sent_ = 0;
                received_ = 0;
                timeout_ = 0;
                if (!c->certificate_.empty()) {
                    verifier_.setCertificate(c->certificate_);
                }
            }

            Client::~Client() {

            }

            void Client::setConfiguration(Configuration *c) {
                config_ = c;
            }

            void Client::ping() {
                std::stringstream ss;
                ss << "start ping" << std::endl;
                config_->env->CallVoidMethod(config_->instance, config_->hipingLogCallback,
                                             config_->env->NewStringUTF(ss.str().c_str()));
                __android_log_print(ANDROID_LOG_INFO, TAG_HIPING, "%s", ss.str().c_str());
                doPing();
                portal_.runEventsLoop();
            }

            void Client::onContentObject(Interest &interest, ContentObject &object) {

                uint64_t rtt = 0;

                if (!config_->certificate_.empty()) {
                    auto t0 = std::chrono::steady_clock::now();
                    std::stringstream ss;
                    if (verifier_.verifyPacket(&object)) {
                        auto t1 = std::chrono::steady_clock::now();
                        auto dt =
                                std::chrono::duration_cast<std::chrono::microseconds>(t1 - t0);
                        ss << "Verification time: " << dt.count() << std::endl
                           << "<<< Signature Ok." << std::endl;

                    } else {
                        ss << "<<< Signature verification failed!" << std::endl;
                    }
                    config_->env->CallVoidMethod(config_->instance, config_->hipingLogCallback,
                                                 config_->env->NewStringUTF(ss.str().c_str()));
                    __android_log_print(ANDROID_LOG_INFO, TAG_HIPING, "%s", ss.str().c_str());
                }

                auto it = send_timestamps_.find(interest.getName().getSuffix());
                if (it != send_timestamps_.end()) {
                    rtt = std::chrono::duration_cast<std::chrono::microseconds>(
                            std::chrono::steady_clock::now().time_since_epoch())
                                  .count() -
                          it->second;
                    send_timestamps_.erase(it);
                }


                std::stringstream ss;
                ss << "<<< recevied object. " << std::endl;
                ss << "<<< interest name: " << interest.getName()
                   << " src port: " << interest.getSrcPort()
                   << " dst port: " << interest.getDstPort()
                   << " flags: " << interest.printFlags() << std::endl;
                ss << "<<< object name: " << object.getName()
                   << " src port: " << object.getSrcPort()
                   << " dst port: " << object.getDstPort()
                   << " flags: " << object.printFlags() << " path label "
                   << object.getPathLabel() << " ("
                   << (object.getPathLabel() >> 24) << ")"
                   << " TTL: " << (int) object.getTTL() << std::endl;
                ss << "<<< round trip: " << rtt << " [us]" << std::endl;
                ss << "<<< interest name: " << interest.getName() << std::endl;
                ss << "<<< content object size: "
                   << object.payloadSize() + object.headerSize() << " [bytes]"
                   << std::endl;
                config_->env->CallVoidMethod(config_->instance, config_->hipingLogCallback,
                                             config_->env->NewStringUTF(ss.str().c_str()));
                config_->env->CallVoidMethod(config_->instance, config_->hipingUpdateGraphCallback,
                                             rtt);

                if (!config_->always_syn_) {
                    if (object.testSyn() && object.testAck() && state_ == SYN_STATE) {
                        state_ = ACK_STATE;
                    }
                }

                received_++;
                processed_++;
                if (processed_ >= config_->maxPing_) {
                    afterSignal();
                }
            }


            void Client::onTimeout(Interest::Ptr &&interest) {
                std::stringstream ss;
                ss << "### timeout for " << interest->getName().toString()
                   << " src port: " << interest->getSrcPort() << " dst port: "
                   << interest->getDstPort()
                   << " flags: " << interest->printFlags() << std::endl;
                config_->env->CallVoidMethod(config_->instance, config_->hipingLogCallback,
                                             config_->env->NewStringUTF(ss.str().c_str()));
                config_->env->CallVoidMethod(config_->instance, config_->hipingUpdateGraphCallback,
                                             0);

                __android_log_print(ANDROID_LOG_INFO, TAG_HIPING, "%s", ss.str().c_str());
                timeout_++;
                processed_++;
                if (processed_ >= config_->maxPing_) {
                    afterSignal();

                }
            }

            void Client::onError(std::error_code ec) {

            }

            void Client::doPing() {
                Name interest_name(config_->name_, (uint32_t) sequence_number_);
                hicn_format_t format;
                if (interest_name.getAddressFamily() == AF_INET) {
                    format = HF_INET_TCP;
                } else {
                    format = HF_INET6_TCP;
                }


                auto interest = std::make_shared<Interest>(interest_name, format);
                interest->setLifetime(uint32_t(config_->interestLifetime_));

                interest->resetFlags();

                if (config_->open_ || config_->always_syn_) {
                    if (state_ == SYN_STATE) {
                        interest->setSyn();
                    } else if (state_ == ACK_STATE) {
                        interest->setAck();
                    }
                } else if (config_->always_ack_) {
                    interest->setAck();
                }

                interest->setSrcPort(config_->srcPort_);
                interest->setDstPort(config_->dstPort_);
                interest->setTTL(config_->ttl_);

                std::stringstream ss;
                ss << ">>> send interest " << interest->getName()
                   << " src port: " << interest->getSrcPort()
                   << " dst port: " << interest->getDstPort()
                   << " flags: " << interest->printFlags()
                   << " TTL: " << (int) interest->getTTL() << std::endl;
                config_->env->CallVoidMethod(config_->instance, config_->hipingLogCallback,
                                             config_->env->NewStringUTF(ss.str().c_str()));
                __android_log_print(ANDROID_LOG_INFO, TAG_HIPING, "%s", ss.str().c_str());

                send_timestamps_[sequence_number_] =
                        std::chrono::duration_cast<std::chrono::microseconds>(
                                std::chrono::steady_clock::now().time_since_epoch())
                                .count();

                portal_.sendInterest(std::move(interest));

                sequence_number_++;
                sent_++;

                if (sent_ < config_->maxPing_) {
                    this->timer_->expires_from_now(
                            std::chrono::microseconds(config_->pingInterval_));
                    this->timer_->async_wait([this](const std::error_code e) { doPing(); });
                }
            }


            void Client::afterSignal() {
                portal_.stopEventsLoop();
            }

            void Client::reset() {
                timer_.reset(new asio::steady_timer(portal_.getIoService()));
                sequence_number_ = config_->first_suffix_;
                last_jump_ = 0;
                processed_ = 0;
                state_ = SYN_STATE;
                sent_ = 0;
                received_ = 0;
                timeout_ = 0;
            }

            uint32_t Client::getSent() {
                return sent_;
            }

            uint32_t Client::getReceived() {
                return received_;
            }

            uint32_t Client::getTimeout() {
                return timeout_;
            }

        }
    }
}

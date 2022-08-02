#ifndef HICNTOOLS_HIPERF_H
#define HICNTOOLS_HIPERF_H

#include <hicn/transport/config.h>
#include <hicn/transport/core/content_object.h>
#include <hicn/transport/core/interest.h>
#include <hicn/transport/interfaces/rtc_socket_producer.h>
#include <hicn/transport/interfaces/socket_consumer.h>
#include <hicn/transport/interfaces/socket_producer.h>
#include <hicn/transport/security/identity.h>
#include <hicn/transport/security/signer.h>
#include <hicn/transport/utils/chrono_typedefs.h>
#include <hicn/transport/utils/literals.h>
#define ASIO_STANDALONE
#include <asio.hpp>
#include <cmath>
#include <fstream>
#include <iomanip>
#include <unordered_set>

#include <asio/signal_set.hpp>


#define TAG_HIPERF "HiPerf"
extern float value;
extern utils::TimePoint when;

extern "C" {
    float hiperfGetValue();
}
namespace transport {

    namespace interface {

#ifndef ERROR_SUCCESS
#define ERROR_SUCCESS 0
#endif
#define ERROR_SETUP -5

        using CryptoSuite = utils::CryptoSuite;
        using Identity = utils::Identity;

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
            uint32_t interest_lifetime_;
            //JavaVM *jvm;
            //jclass cls;

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

#endif /* hiperf_hpp */

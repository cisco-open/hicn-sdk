//
//  hiperfWrapper.cpp
//  Hybrid ICN Network Service
//

#include "hiperfWrapper.hpp"
#include <stdio.h>
#include "hiperf.hpp"
#include <unistd.h>
extern int execute;

transport::interface::HIperfClient *hiperfClient = nullptr;


void startHiperf(const char *hicn_name,
                 float beta_parameter,
                 float drop_factor_parameter,
                 int window_size,
                 long stats_interval,
                 long rtc_protocol,
                 long interest_lifetime) {
    transport::interface::ClientConfiguration client_configuration;
    client_configuration.name = std::string(hicn_name);
    client_configuration.rtc_ = (bool) rtc_protocol;
    client_configuration.beta = (double) beta_parameter;
    client_configuration.drop_factor = (double) drop_factor_parameter;
    client_configuration.interest_lifetime_ = (uint32_t) interest_lifetime;
    client_configuration.virtual_download = false;
    if ((int) window_size >= 0) {
        client_configuration.window = (int) window_size;
    }
    client_configuration.report_interval_milliseconds_ = (uint32_t) stats_interval;

    if (hiperfClient) {
        delete (hiperfClient);
        hiperfClient = nullptr;
    }
    hiperfClient = new transport::interface::HIperfClient(client_configuration);

    int setup = hiperfClient->setup();
    if (setup != ERROR_SETUP) {
        hiperfClient->run();
    }
}

int stopHiperf() {
    if (hiperfClient)
        hiperfClient->reset();
    return 0;
}

float getValue() {
    return hiperfGetValue();
}

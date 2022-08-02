//
//  hiperfWrapper.hpp
//  Hybrid ICN Network Service
//


#ifndef hiperfWrapper_hpp
#define hiperfWrapper_hpp

#include <stdio.h>
int execute = 1;
#ifdef __cplusplus
extern "C" {
#endif
void startHiperf(const char *hicn_name,
            float beta_parameter,
            float drop_factor_parameter,
            int window_size,
            long stats_interval,
            long rtc_protocol,
            long interest_lifetime);

int stopHiperf();

float getValue();

#ifdef __cplusplus
}
#endif
#endif /* hiperfWrapper_hpp */

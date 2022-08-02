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


//
//  hiperf.hpp
//  hICNTools
//
//  Created by manangel on 3/13/20.
//  Copyright © 2020 manangel. All rights reserved.
//

#ifndef hiperf_hpp
#define hiperf_hpp


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


#endif /* hiperf_hpp */

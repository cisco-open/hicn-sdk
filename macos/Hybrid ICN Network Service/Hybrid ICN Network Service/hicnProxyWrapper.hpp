//
//  hicnProxyWrapper.hpp
//  Hybrid ICN Network Service
//

#ifndef hicnProxyWrapper_hpp
#define hicnProxyWrapper_hpp

#include <stdio.h>
#ifdef __cplusplus
extern "C" {
#endif

void startHicnProxy(const char *prefix,
            int listenPort);

void stopHicnProxy();
#ifdef __cplusplus
}
#endif

#endif /* hicnProxyWrapper_hpp */

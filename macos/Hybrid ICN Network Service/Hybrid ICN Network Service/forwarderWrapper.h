//
//  forwarderWrapper.h
//  Hybrid ICN Network Service
//

#ifndef forwarderWrapper_h
#define forwarderWrapper_h


#include <stdio.h>
int isRunning(void);
void stopHicnFwd(void);
void startHicnFwd(const char *path, size_t pathSize);

#endif /* forwarderWrapper_h */

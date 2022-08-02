//
//  hicnControllerrapper.h
//  Hybrid ICN Network Service
//

#ifndef hicnControllerrapper_h
#define hicnControllerrapper_h


#include <hicn/hicn-light/config.h>
#include <hicn/utils/utils.h>

#include <arpa/inet.h>
#include <getopt.h>
#include <netinet/in.h>
#include <sys/socket.h>
#include <sys/uio.h>
#include <unistd.h>
#include <arpa/inet.h>
#include <stdbool.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>

#include <parc/assert/parc_Assert.h>
#include <string.h>

#include <parc/security/parc_IdentityFile.h>
#include <parc/security/parc_Security.h>

#include <parc/algol/parc_ArrayList.h>
#include <parc/algol/parc_List.h>
#include <parc/algol/parc_Memory.h>
#include <parc/algol/parc_SafeMemory.h>

#include <hicn/core/dispatcher.h>
#include <hicn/core/forwarder.h>

#include <errno.h>
#include <hicn/config/controlRoot.h>
#include <hicn/config/controlState.h>

#include <hicn/utils/commands.h>
size_t commandOutputLen = 0;


static int payloadLengthController[LAST_COMMAND_VALUE] = {
    sizeof(add_listener_command),
    sizeof(add_connection_command),
    sizeof(list_connections_command),  // needed when get response from FWD
    sizeof(add_route_command),
    sizeof(list_routes_command),  // needed when get response from FWD
    sizeof(remove_connection_command),
    sizeof(remove_listener_command),
    sizeof(remove_route_command),
    sizeof(cache_store_command),
    sizeof(cache_serve_command),
    0,  // cache clear
    sizeof(set_strategy_command),
    sizeof(set_wldr_command),
    sizeof(add_punting_command),
    sizeof(list_listeners_command),  // needed when get response from FWD
    sizeof(mapme_activator_command),
    sizeof(mapme_activator_command),
    sizeof(mapme_timing_command),
    sizeof(mapme_timing_command),
    sizeof(mapme_send_update_command),
    sizeof(connection_set_admin_state_command)
};

typedef struct controller_main_state {
  ControlState *controlState;
} ControlMainState;


char* displayForwarderLogo(void);
char *execCommand(char *line);
#endif /* hicnControllerrapper_h */

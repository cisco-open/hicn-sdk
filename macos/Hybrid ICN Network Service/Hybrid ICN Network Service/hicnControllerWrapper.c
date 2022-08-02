//
//  hicnControllerrapper.c
//  Hybrid ICN Network Service
//

#include "hicnControllerWrapper.h"

char output[8192];


static PARCList *_controlState_ParseStringIntoTokens(
    const char *originalString) {
    PARCList *list = parcList(parcArrayList_Create(parcArrayList_StdlibFreeFunction),
               PARCArrayListAsPARCList);

    char *token = NULL;

    char *tofree =
        parcMemory_StringDuplicate(originalString, strlen(originalString) + 1);
    char *string = tofree;
    token = strtok(string, " \t\n");
    while (token != NULL) {
        if (strlen(token) > 0) {
            parcList_Add(list, strdup(token));
        }
        token = strtok(NULL, " \t\n");
    }

    parcMemory_Deallocate((void **)&tofree);

    return list;
}




struct iovec *_writeAndReadMessage(ControlState *state, struct iovec *msg) {
    parcAssertNotNull(msg, "Parameter msg must be non-null");
    int sockfd = controlState_GetSockfd(state);

    // check if request has a payload
    if (((header_control_message *)msg[0].iov_base)->length > 0) {
        // command with payload
        // write header + payload (compatibility issue: two write needed instead of
        // the writev)
        if (write(sockfd, msg[0].iov_base, (unsigned int)msg[0].iov_len) < 0 ||
            write(sockfd, msg[1].iov_base, (unsigned int)msg[1].iov_len) < 0) {

            printf("\nError while sending the Message: cannot write on socket \n");
            exit(EXIT_FAILURE);
        }
        parcMemory_Deallocate(&msg[1].iov_base);
    } else {  // command without payload, e.g. 'list'
        // write header only
        if (write(sockfd, msg[0].iov_base, msg[0].iov_len) < 0) {
            printf("\nError while sending the Message: cannot write on socket \n");
            exit(EXIT_FAILURE);
        }
    }
    parcMemory_Deallocate(&msg[0].iov_base);

    // ======= RECEIVE =======

    header_control_message *headerResponse =
        (header_control_message *)parcMemory_AllocateAndClear(sizeof(header_control_message));
    if (recv(sockfd, (char *)headerResponse, sizeof(header_control_message), 0) < 0) {
        printf("\nError in Receiving the Message \n");
        return NULL;
    }

    if (headerResponse->messageType < RESPONSE_LIGHT ||
        headerResponse->messageType >= LAST_MSG_TYPE_VALUE) {
        char *checkFinMsg = parcMemory_Reallocate(headerResponse, 32);
        if (recv(sockfd, checkFinMsg, sizeof(checkFinMsg),
                 MSG_PEEK | MSG_DONTWAIT) == 0) {

            // if recv returns zero, that means the connection has been closed:
            close(sockfd);
            printf("\nConnection terminated by the Daemon. Exiting... \n");
            return NULL;
        } else {
            printf("\nError: Unrecognized message type received \n");
            return NULL;
        }
    }

    void *payloadResponse = NULL;

    if ((commandOutputLen = headerResponse->length) > 0) {
        payloadResponse = parcMemory_AllocateAndClear(
        payloadLengthController[headerResponse->commandID] *
        headerResponse->length);

        if (recv(sockfd, payloadResponse,
            payloadLengthController[headerResponse->commandID] *
                 headerResponse->length,
                    0) < 0) {
            printf("\nError in Receiving the Message \n");
            return NULL;
        }
    }

    struct iovec *response =
    parcMemory_AllocateAndClear(sizeof(struct iovec) * 2);

    response[0].iov_base = headerResponse;
    response[0].iov_len = sizeof(header_control_message);
    response[1].iov_base = payloadResponse;
    response[1].iov_len = payloadLengthController[headerResponse->commandID] *
    headerResponse->length;

    return response;
}

char *execCommand(char *line) {
    char *server_ip = SRV_CTRL_IP;
    uint16_t server_port = SRV_CTRL_PORT;

    PARCList *commands = _controlState_ParseStringIntoTokens(line);

    ControlMainState mainState;
    mainState.controlState = controlState_Create(&mainState, _writeAndReadMessage, true, server_ip, server_port);
    if (mainState.controlState == NULL) {
        printf("aaaaa\n");
        return "Forwarder not connected";
    }
    controlState_RegisterCommand(mainState.controlState, controlRoot_HelpCreate(mainState.controlState));
    controlState_RegisterCommand(mainState.controlState, controlRoot_Create(mainState.controlState));

    controlState_SetInteractiveFlag(mainState.controlState, false);
    if (mainState.controlState != NULL) {
        controlState_DispatchCommand(mainState.controlState, commands, output, sizeof(output));
        parcList_Release(&commands);
    }

    controlState_Destroy(&mainState.controlState);
    return output;
}




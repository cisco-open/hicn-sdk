
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
//  metis_wrapper.c
//  Metis4iOs
//
d

#include "hicnFwd.h"
#include <unistd.h>
#include <parc/security/parc_Security.h>
#include <parc/security/parc_IdentityFile.h>

#include <parc/algol/parc_Memory.h>
#include <parc/algol/parc_SafeMemory.h>
#include <parc/algol/parc_List.h>
#include <parc/algol/parc_ArrayList.h>
#include <hicn/core/dispatcher.h>
#include <parc/algol/parc_FileOutputStream.h>
#include <parc/logging/parc_LogLevel.h>
#include <parc/logging/parc_LogReporterFile.h>
#include <parc/logging/parc_LogReporterTextStdout.h>
#include <hicn/core/forwarder.h>
static bool _isRunning = false;
static Forwarder *hicnFwd = NULL;
static Logger *logger = NULL;

void startHicnFwd(const char *path, size_t pathSize) {
    char *pathTemp = (char *)malloc(sizeof(char) * (1 + pathSize - 7));
    strcpy(pathTemp, path + 7);
    printf("%s\n", pathTemp);
    if (!_isRunning) {

        logger = NULL;
        int logLevelArray[LoggerFacility_END];
        for (int i = 0; i < LoggerFacility_END; i++) {
            logLevelArray[i] = -1;
        }
        PARCLogReporter *stdoutReporter = parcLogReporterTextStdout_Create();
        logger = logger_Create(stdoutReporter, parcClock_Wallclock());
        parcLogReporter_Release(&stdoutReporter);
        for (int i = 0; i < LoggerFacility_END; i++) {
            if (logLevelArray[i] > -1) {
                logger_SetLogLevel(logger, i, logLevelArray[i]);
            }
        }

        hicnFwd = forwarder_Create(logger);
        Configuration *hicnFwdConfiguration = forwarder_GetConfiguration(hicnFwd);
        configuration_SetObjectStoreSize(hicnFwdConfiguration, 0);
        forwarder_SetupLocalListeners(hicnFwd, PORT_NUMBER);
        if (pathTemp != NULL && strlen(pathTemp) >0) {
            forwarder_SetupFromConfigFile(hicnFwd, pathTemp);
        }
        _isRunning = true;
        dispatcher_Run(forwarder_GetDispatcher(hicnFwd));
    }
    free(pathTemp);
}

void stopHicnFwd() {
    if(_isRunning) {
        dispatcher_Stop(forwarder_GetDispatcher(hicnFwd));

        sleep(1);
        forwarder_Destroy(&hicnFwd);
        sleep(2);
        logger_Release(&logger);
        _isRunning = false;
    }
}

int isRunning() {
    return _isRunning;
}



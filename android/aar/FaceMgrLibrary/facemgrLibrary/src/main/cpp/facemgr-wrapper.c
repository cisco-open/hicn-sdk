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

#include <android/log.h>
#include <hicn/facemgr/api.h>
#include <hicn/facemgr/cfg.h>
#include <hicn/facemgr/loop.h>
#include <hicn/policy.h>
#include <hicn/util/ip_address.h>
#include <hicn/util/log.h>
#include <hicn/transport/config.h>
#include <jni.h>
#include <stdbool.h>
#include <stdio.h>
#include <unistd.h>

static facemgr_cfg_t *facemgr_cfg;
static bool _isRunningFacemgr = false;
static JNIEnv *_env;
// facemgr
static loop_t *loop;
facemgr_t *facemgr;

JNIEXPORT jboolean JNICALL
Java_com_cisco_hicn_facemgrlibrary_supportlibrary_FacemgrLibrary_isRunningFacemgr(
    JNIEnv *env, jobject thiz) {
    return _isRunningFacemgr;
}

JNIEXPORT void JNICALL
Java_com_cisco_hicn_facemgrlibrary_supportlibrary_FacemgrLibrary_stopFacemgr(
    JNIEnv *env, jobject thiz) {
    if (_isRunningFacemgr) {
        _isRunningFacemgr = false;

        loop_break(loop);
    }
}

JNIEXPORT void JNICALL
Java_com_cisco_hicn_facemgrlibrary_supportlibrary_FacemgrLibrary_startFacemgr(
    JNIEnv *env, jobject thiz) {
    DEBUG("Starting face manager");
    if (_isRunningFacemgr) return;

    _isRunningFacemgr = true;

    facemgr_face_type_t face_type = FACEMGR_FACE_TYPE_OVERLAY_UDP;
    facemgr_cfg_set_face_type(facemgr_cfg, &face_type);
    facemgr = facemgr_create_with_config(facemgr_cfg);

    JavaVM *jvm = NULL;
    (*env)->GetJavaVM(env, &jvm);
    facemgr_set_jvm(facemgr, jvm);

    loop = loop_create();
    if (!loop) {
        ERROR("Could not create main loop");
        goto ERR_LOOP;
    }

    facemgr_set_callback(facemgr, loop, (void *)loop_callback);

    if (facemgr_bootstrap(facemgr) < 0) {
        ERROR("Could not bootstrap facemgr");
        goto ERR_BOOTSTRAP;
    }

    if (loop_dispatch(loop) < 0) {
        ERROR("Failed to run main loop");
        goto ERR_DISPATCH;
    }

    facemgr_stop(facemgr);
    _isRunningFacemgr = false;
    loop_undispatch(loop);
    facemgr_free(facemgr);
    loop_free(loop);
    DEBUG("Face manager is stopped");

    return;

ERR_DISPATCH:
ERR_BOOTSTRAP:
ERR_LOOP:
    ERROR("Could not start facemgr");
}

JNIEXPORT void JNICALL
Java_com_cisco_hicn_facemgrlibrary_supportlibrary_FacemgrLibrary_initConfig(
    JNIEnv *env, jobject thiz) {
    facemgr_cfg = facemgr_cfg_create();
}

JNIEXPORT void JNICALL
Java_com_cisco_hicn_facemgrlibrary_supportlibrary_FacemgrLibrary_updateInterfaceIPv4(
    JNIEnv *env, jobject thiz, jint interface_type, jint source_port,
    jstring next_hop_ip, jint next_hop_port) {
    netdevice_type_t netdevice_interface_type =
        (netdevice_type_t)interface_type;

    ip_address_t remote_addr;
    ip_address_t *next_hop_ip_p;
    const char *next_hop_ip_string =
        (*env)->GetStringUTFChars(env, next_hop_ip, 0);
    ip_address_pton(next_hop_ip_string, &remote_addr);
    (*env)->ReleaseStringUTFChars(env, next_hop_ip, next_hop_ip_string);
    next_hop_ip_p = &remote_addr;

    facemgr_cfg_rule_t *rule;
    facemgr_cfg_get_rule(facemgr_cfg, NULL, netdevice_interface_type, &rule);
    if (!rule) {
        rule = facemgr_cfg_rule_create();
        facemgr_cfg_rule_set_match(rule, NULL, netdevice_interface_type);

        facemgr_cfg_rule_set_overlay(rule, AF_INET, NULL, source_port,
                                     next_hop_ip_p, next_hop_port);
        facemgr_cfg_add_rule(facemgr_cfg, rule);
    } else {
        facemgr_cfg_rule_set_overlay(rule, AF_INET, NULL, source_port,
                                     next_hop_ip_p, next_hop_port);
    }
}

JNIEXPORT void JNICALL
Java_com_cisco_hicn_facemgrlibrary_supportlibrary_FacemgrLibrary_updateInterfaceIPv6(
    JNIEnv *env, jobject thiz, jint interface_type, jint source_port,
    jstring next_hop_ip, jint next_hop_port) {
    netdevice_type_t netdevice_interface_type =
        (netdevice_type_t)interface_type;

    ip_address_t remote_addr;
    ip_address_t *next_hop_ip_p;
    const char *next_hop_ip_string =
        (*env)->GetStringUTFChars(env, next_hop_ip, 0);
    ip_address_pton(next_hop_ip_string, &remote_addr);
    (*env)->ReleaseStringUTFChars(env, next_hop_ip, next_hop_ip_string);
    next_hop_ip_p = &remote_addr;

    facemgr_cfg_rule_t *rule;
    facemgr_cfg_get_rule(facemgr_cfg, NULL, netdevice_interface_type, &rule);
    if (!rule) {
        rule = facemgr_cfg_rule_create();
        facemgr_cfg_rule_set_match(rule, NULL, netdevice_interface_type);

        facemgr_cfg_rule_set_overlay(rule, AF_INET6, NULL, source_port,
                                     next_hop_ip_p, next_hop_port);
        facemgr_cfg_add_rule(facemgr_cfg, rule);

    } else {
        facemgr_cfg_rule_set_overlay(rule, AF_INET6, NULL, source_port,
                                     next_hop_ip_p, next_hop_port);
    }
}

JNIEXPORT void JNICALL
Java_com_cisco_hicn_facemgrlibrary_supportlibrary_FacemgrLibrary_unsetInterfaceIPv4(
    JNIEnv *env, jobject thiz, jint interface_type) {
    netdevice_type_t netdevice_interface_type =
        (netdevice_type_t)interface_type;
    facemgr_cfg_rule_t *rule;
    facemgr_cfg_get_rule(facemgr_cfg, NULL, netdevice_interface_type, &rule);
    if (rule) {
        facemgr_rule_unset_overlay(rule, AF_INET);
    }
}

JNIEXPORT void JNICALL
Java_com_cisco_hicn_facemgrlibrary_supportlibrary_FacemgrLibrary_unsetInterfaceIPv6(
    JNIEnv *env, jobject thiz, jint interface_type) {
    netdevice_type_t netdevice_interface_type =
        (netdevice_type_t)interface_type;
    facemgr_cfg_rule_t *rule;
    facemgr_cfg_get_rule(facemgr_cfg, NULL, netdevice_interface_type, &rule);
    if (rule) {
        facemgr_rule_unset_overlay(rule, AF_INET6);
    }
}

JNIEXPORT void JNICALL
Java_com_cisco_hicn_facemgrlibrary_supportlibrary_FacemgrLibrary_enableDiscovery(
    JNIEnv *env, jobject thiz, jboolean enable_discovery) {
    facemgr_cfg_set_discovery(facemgr_cfg, enable_discovery);
}

JNIEXPORT void JNICALL
Java_com_cisco_hicn_facemgrlibrary_supportlibrary_FacemgrLibrary_enableIPv6(
    JNIEnv *env, jobject thiz, jint enable_ipv6) {
    int enableIPv6 = enable_ipv6;
    facemgr_cfg_set_ipv6(facemgr_cfg, enableIPv6);
}

JNIEXPORT void JNICALL
Java_com_cisco_hicn_facemgrlibrary_supportlibrary_FacemgrLibrary_enableIPv4(
    JNIEnv *env, jobject thiz, jint enable_ipv4) {
    int enableIPv4 = enable_ipv4;

    facemgr_cfg_set_ipv4(facemgr_cfg, enableIPv4);
}

JNIEXPORT jstring JNICALL
Java_com_cisco_hicn_facemgrlibrary_supportlibrary_FacemgrLibrary_getListFacelets(
    JNIEnv *env, jobject thiz) {
    jstring jstrBuffer = NULL;

    if (facemgr != NULL) {
        char *buffer;

        facemgr_list_facelets_json(facemgr, &buffer);
        jstrBuffer = (*env)->NewStringUTF(env, buffer);
        free(buffer);
    }
    return jstrBuffer;
}

JNIEXPORT void JNICALL
Java_com_cisco_hicn_facemgrlibrary_supportlibrary_FacemgrLibrary_setLogLevel(
    JNIEnv *env, jobject thiz, jint facemgr_log_level) {
    switch ((int)facemgr_log_level) {
        case 0:
            log_conf.log_level = LOG_FATAL;
            break;
        case 1:
            log_conf.log_level = LOG_ERROR;
            break;
        case 2:
            log_conf.log_level = LOG_WARN;
            break;
        case 3:
            log_conf.log_level = LOG_INFO;
            break;
        case 4:
            log_conf.log_level = LOG_DEBUG;
            break;
        case 5:
            log_conf.log_level = LOG_TRACE;
            break;
        default:
            log_conf.log_level = LOG_INFO;
    }
}

JNIEXPORT void JNICALL
Java_com_cisco_hicn_facemgrlibrary_supportlibrary_FacemgrLibrary_onNetworkEvent(
    JNIEnv *env, jobject thiz, jstring interface_name, jint netdevice_type,
    jboolean up, int family, jstring ip_address) {
    const char *interface_name_s =
        (*env)->GetStringUTFChars(env, interface_name, 0);
    const char *ip_address_s = (ip_address ? (*env)->GetStringUTFChars(env, ip_address, 0) : NULL);
    facemgr_on_android_callback(facemgr, interface_name_s, netdevice_type, up,
                                family, ip_address_s);
    if (ip_address)
        (*env)->ReleaseStringUTFChars(env, ip_address, ip_address_s);
    (*env)->ReleaseStringUTFChars(env, interface_name, interface_name_s);
}

JNIEXPORT jstring JNICALL
Java_com_cisco_hicn_facemgrlibrary_supportlibrary_FacemgrLibrary_getFaceMgrVersion(JNIEnv *env,
                                                                                   jobject thiz) {
    char version[10];
    sprintf(version, "v%s.%s.%s", HICNTRANSPORT_VERSION_MAJOR, HICNTRANSPORT_VERSION_MINOR, HICNTRANSPORT_VERSION_PATCH);
    return (*env)->NewStringUTF(env,version);
}

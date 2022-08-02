#include <jni.h>
#include <string>
#include <android/log.h>

#define WITH_START_STOP

#include <hicn/hproxy/proxy/proxy.h>

#include <arpa/inet.h>
#include <net/if.h>
#include <linux/if.h>
#include <linux/if_tun.h>
#include <netinet/ether.h>
#include <linux/if_packet.h>
#include <sys/ioctl.h>

#define HPROXY_ATTRIBUTE "mProxyPtr"

#define HPROXY_TAG "HproxyWrap"

using HicnProxy = hproxy::HicnProxy;

struct JniContext {
    JniContext() : env(nullptr), instance(nullptr) {}

    JNIEnv *env;
    jobject *instance;
};


// Get pointer field straight from `JavaClass`
jfieldID getPtrFieldId(JNIEnv *env, jobject obj, std::string attribute_name) {
    static jfieldID ptrFieldId = 0;

    if (!ptrFieldId) {
        jclass c = env->GetObjectClass(obj);
        ptrFieldId = env->GetFieldID(c, attribute_name.c_str(), "J");
        env->DeleteLocalRef(c);
    }

    return ptrFieldId;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_cisco_hicn_hproxylibrary_supportlibrary_HProxyLibrary_isRunning(JNIEnv *env, jobject instance) {
    HicnProxy *proxy = (HicnProxy *) env->GetLongField(instance, getPtrFieldId(env, instance,
                                                                               HPROXY_ATTRIBUTE));
    if (proxy) {
        return jboolean(proxy->isRunning());
    }

    return JNI_FALSE;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_cisco_hicn_hproxylibrary_supportlibrary_HProxyLibrary_getTunFd(JNIEnv *env, jobject instance,
                                                     jstring device_name) {

    const int fd = open("/dev/tun", O_RDWR | O_NONBLOCK);
    if (fd != -1) {
        struct ifreq ifr;

        memset(&ifr, 0, sizeof(ifr));
        ifr.ifr_flags = IFF_TUN | IFF_NO_PI;

        const char *_device_name = env->GetStringUTFChars(device_name, 0);
        __android_log_print(ANDROID_LOG_INFO, HPROXY_TAG,
                            "Opened device %s. PID: %d", _device_name, getpid());

        strncpy(ifr.ifr_name, _device_name, IFNAMSIZ);

        if (::ioctl(fd, TUNSETIFF, &ifr) < 0) {
            __android_log_print(ANDROID_LOG_ERROR, HPROXY_TAG,
                                "FD of tun device not retrieved.");
            __android_log_print(ANDROID_LOG_ERROR, HPROXY_TAG, "ioctl failed and returned errno %s",
                                strerror(errno));
        }

        __android_log_print(ANDROID_LOG_INFO, HPROXY_TAG,
                            "TUN device allocated successfully. FD: %d", fd);
    } else {
        __android_log_print(ANDROID_LOG_INFO, HPROXY_TAG,
                            "Device not opened.");
    }

    return fd;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_cisco_hicn_hproxylibrary_supportlibrary_HProxyLibrary_start(JNIEnv *env, jobject instance,
    jboolean disableSD, jstring server, int port) {

    JniContext *context = new JniContext();
    context->env = env;
    context->instance = &instance;

    uint64_t secret = 12345678910;
    hproxy::ProxyConfiguration configuration;
    configuration.setSecret(secret);

    configuration.setServiceDiscoveryDisabled(disableSD);
    const char *_server = env->GetStringUTFChars(server, 0);
    configuration.setServerAddress(_server);
    configuration.setPort(port);

    /*
     * Create an instance of the proxy object and release its ownership. Caller
     * has to destroy it
     */
    auto proxy = HicnProxy::create(configuration).release();
    proxy->setJniContext(context);
    env->SetLongField(instance, getPtrFieldId(env, instance, HPROXY_ATTRIBUTE), reinterpret_cast<jlong>(proxy));
    proxy->run();
}

static int attachHicnServiceWrap(JNIEnv *env, jobject instance, bool flag)
{
    jclass clazz = env->GetObjectClass(instance);
    jmethodID methodID = env->GetMethodID(clazz, flag ? "attachHicnService" : "detachHicnService", "()I");
    if (!methodID)
        return -1;
    return env->CallIntMethod(instance, methodID);
}

extern "C"
int attachHicnService(void * context, bool flag)
{
    JniContext *jni_context = (JniContext *) (context);

    if (!jni_context->env || !jni_context->instance) {
        __android_log_print(ANDROID_LOG_ERROR, "HProxyWrap",
                            "Call createTunDevice, but _env and _instance variables are not initialized.");
        return -1;
    }

    return attachHicnServiceWrap(jni_context->env, *jni_context->instance, flag);
}



static int
createTunDeviceWrap(JNIEnv *env, jobject instance, const char *vpn_address, uint16_t prefix_length,
                    const char *route_address,
                    uint16_t route_prefix_length, const char *dns) {
    jclass clazz = env->GetObjectClass(instance);
    jmethodID methodID = env->GetMethodID(clazz, "createTunDevice",
                                          "(Ljava/lang/String;ILjava/lang/String;ILjava/lang/String;)I");

    int ret = -1;
    if (methodID) {
        jstring vpnAddress = env->NewStringUTF(vpn_address);
        jint prefixLength(prefix_length);
        jstring routeAddress = env->NewStringUTF(route_address);
        jint routePrefixLength(route_prefix_length);
        jstring dnsAddress = env->NewStringUTF(dns);
        ret = env->CallIntMethod(instance, methodID, vpnAddress, prefixLength, routeAddress,
                                 routePrefixLength, dnsAddress);
    }

    return ret;
}

extern "C" int createTunDevice(const char *vpn_address, uint16_t prefix_length,
                               const char *route_address,
                               uint16_t route_prefix_length, const char *dns, void *context) {
    JniContext *jni_context = (JniContext *) (context);

    if (!jni_context->env || !jni_context->instance) {
        __android_log_print(ANDROID_LOG_ERROR, HPROXY_TAG,
                            "Call createTunDevice, but _env and _instance variables are not initialized.");
        return -1;
    }

    return createTunDeviceWrap(jni_context->env, *jni_context->instance, vpn_address, prefix_length,
                               route_address,
                               route_prefix_length, dns);
}

static int
closeTunDeviceWrap(JNIEnv *env, jobject instance) {
    jclass clazz = env->GetObjectClass(instance);
    jmethodID methodID = env->GetMethodID(clazz, "closeTunDevice", "()I");

    int ret = -1;
    if (methodID) {
        ret = env->CallIntMethod(instance, methodID);
    }

    return ret;
}

extern "C" int closeTunDevice(void *context) {
    JniContext *jni_context = (JniContext *) (context);
    if (!jni_context->env || !jni_context->instance) {
        __android_log_print(ANDROID_LOG_ERROR, HPROXY_TAG,
                            "Call createTunDevice, but _env and _instance variables are not initialized.");
        return -1;
    }

    return closeTunDeviceWrap(jni_context->env, *jni_context->instance);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_cisco_hicn_hproxylibrary_supportlibrary_HProxyLibrary_stop(JNIEnv *env, jobject instance) {

    HicnProxy *proxy = (HicnProxy *) env->GetLongField(instance, getPtrFieldId(env, instance,
                                                                               HPROXY_ATTRIBUTE));
    if (proxy) {
        proxy->stop();
        env->SetLongField(instance, getPtrFieldId(env, instance, HPROXY_ATTRIBUTE), NULL);
    }

}

extern "C"
JNIEXPORT void JNICALL
Java_com_cisco_hicn_hproxylibrary_supportlibrary_HProxyLibrary_destroy(JNIEnv *env, jobject instance) {
    HicnProxy *proxy = (HicnProxy *) env->GetLongField(instance, getPtrFieldId(env, instance,
                                                                               HPROXY_ATTRIBUTE));
    JniContext *jni_context = (JniContext *) proxy->getJniContext();
    delete jni_context;
    delete proxy;

}


extern "C"
JNIEXPORT jboolean JNICALL
Java_com_cisco_hicn_hproxylibrary_supportlibrary_HProxyLibrary_isHProxyEnabled(JNIEnv *env, jclass clazz) {
    return JNI_TRUE;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_cisco_hicn_hproxylibrary_supportlibrary_HProxyLibrary_getHicnServiceName(JNIEnv *env, jclass thiz) {

    return env->NewStringUTF(HicnProxy::getHicnServiceName());

}


extern "C"
JNIEXPORT void JNICALL
Java_com_cisco_hicn_hproxylibrary_supportlibrary_HProxyLibrary_onHicnServiceAvailable(JNIEnv *env,
                                                                                  jobject instance,
                                                                                  jboolean flag) {
    HicnProxy *proxy = (HicnProxy *) env->GetLongField(instance,
                                                       getPtrFieldId(env, instance, HPROXY_ATTRIBUTE));
    if (proxy) {
        proxy->onHicnServiceAvailable(flag);
        return; // JNI_TRUE;
    }


    return; // JNI_FALSE;
}

/*
 * Global variable used to track Java internals IDs for the PuntingSpec object
 */
typedef struct _JNI_PUNTINGSPEC {
    jclass cls;
    jmethodID ctorID;
    jfieldID appNameID;
    jfieldID androidPackageID;
    jfieldID protocolsID;
    jfieldID portsID;
    jfieldID puntByDefaultID;
} JNI_PUNTINGSPEC;

JNI_PUNTINGSPEC * jniPuntingSpec = NULL;

/*
 * Populate the global variable
 */
void loadJniPuntingSpec(JNIEnv * env) {
    // XXX Removed for tests XXX
    //if (jniPuntingSpec != NULL)
    //    return;
    jniPuntingSpec = new JNI_PUNTINGSPEC;
    jniPuntingSpec->cls = env->FindClass("com/cisco/hicn/hproxylibrary/supportlibrary/PuntingSpec");
    jniPuntingSpec->ctorID = env->GetMethodID(jniPuntingSpec->cls, "<init>", "()V");
    jniPuntingSpec->appNameID = env->GetFieldID(jniPuntingSpec->cls, "appName", "Ljava/lang/String;");
    jniPuntingSpec->androidPackageID = env->GetFieldID(jniPuntingSpec->cls, "androidPackage", "Ljava/lang/String;");
    jniPuntingSpec->protocolsID = env->GetFieldID(jniPuntingSpec->cls, "protocols", "[I");
    jniPuntingSpec->portsID = env->GetFieldID(jniPuntingSpec->cls, "ports", "[I");
    jniPuntingSpec->puntByDefaultID = env->GetFieldID(jniPuntingSpec->cls, "puntByDefault", "Z");
}


#define MIN(x, y) (x < y ? x : y)

void fillJavaPuntingSpecValues(JNIEnv * env, jobject jPuntingSpec,
                               punting_spec_t cPuntingSpec)
{
    env->SetObjectField(jPuntingSpec, jniPuntingSpec->appNameID,
                        env->NewStringUTF(cPuntingSpec.app_name));
    env->SetObjectField(jPuntingSpec, jniPuntingSpec->androidPackageID,
                        env->NewStringUTF(cPuntingSpec.android_package));

    int numProtocols, numPorts;
    jintArray jProtocolsArray;
    jintArray jPortsArray;

    // use array_len = sizeof(x)/sizeof(*x);

    for (numProtocols = 0; cPuntingSpec.protocols[numProtocols]; numProtocols++)
        ;
    for (numPorts = 0; cPuntingSpec.protocols[numPorts]; numPorts++)
        ;
    int size = MIN(numProtocols, numPorts);
    jProtocolsArray =  env->NewIntArray(size);
    if (jProtocolsArray == NULL)
        goto ERR;
    env->SetIntArrayRegion(jProtocolsArray, 0, size, cPuntingSpec.protocols);

    jPortsArray =  env->NewIntArray(size);
    if (jProtocolsArray == NULL)
        goto ERR;
    env->SetIntArrayRegion(jPortsArray, 0, size, cPuntingSpec.ports);

    //jintArray jProtocolsArray = (jintArray)env->GetObjectField(jPuntingSpec, jniPuntingSpec->protocolsID);
    //jintArray jPortsArray = (jintArray)env->GetObjectField(jPuntingSpec, jniPuntingSpec->protocolsID);
    //env->SetIntArrayElement(jProtocolsArray, i, value);


    // XXX allocation needed first ?
    env->SetObjectField(jPuntingSpec, jniPuntingSpec->protocolsID, jProtocolsArray); //cPuntingSpec.protocols);
    env->SetObjectField(jPuntingSpec, jniPuntingSpec->portsID, jPortsArray); //cPuntingSpec.ports);

    env->SetBooleanField(jPuntingSpec, jniPuntingSpec->puntByDefaultID, cPuntingSpec.punt_by_default);
    ERR:
    return;
}


jobjectArray
getPuntingSpecArray(JNIEnv *env, jclass thiz)
{
    std::vector<punting_spec_t> specs;

    HicnProxy *proxy = (HicnProxy *) env->GetLongField(instance,
            getPtrFieldId(env, instance, HPROXY_ATTRIBUTE));
    if (proxy)
        specs = proxy->getPuntingSpecs();


    jobjectArray jPuntingSpecArray = env->NewObjectArray(specs->size,
            jniPuntingSpec->cls, NULL);

    size_t pos = 0;
    for(auto spec : specs) {
        jobject jPuntingSpec = env->NewObject(jniPuntingSpec->cls, jniPuntingSpec->ctorID);
        fillJavaPuntingSpecValues(env, jPuntingSpec, spec);
        env->SetObjectArrayElement(jPuntingSpecArray, pos++, jPuntingSpec);
    }

    return jPuntingSpecArray;

}


extern "C" JNIEXPORT jobjectArray JNICALL
Java_com_cisco_hicn_hproxylibrary_supportlibrary_HProxyLibrary_getPuntingSpecs(JNIEnv
        *env, jclass thiz)
//jobject obj) // https://coderanch.com/t/273468/java/JNI-structures
{
    loadJniPuntingSpec(env);

    return getPuntingSpecArray(env, thiz);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_cisco_hicn_hproxylibrary_supportlibrary_HProxyLibrary_initConfig(JNIEnv *env, jobject thiz) {
    // TODO: implement initConfig()
}
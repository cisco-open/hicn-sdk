//
//  hicnProxyWrapper.cpp
//  Hybrid ICN Network Service
//

#include "hicnProxyWrapper.hpp"

#include <hicn/http-proxy/http_proxy.h>


using namespace transport;


transport::HTTPProxy *proxy = nullptr;

struct Params : HTTPProxy::ClientParams, HTTPProxy::ServerParams {
  void printParams() override {
    if (client) {
      HTTPProxy::ClientParams::printParams();
    } else if (server) {
      HTTPProxy::ServerParams::printParams();
    } else {
      throw std::runtime_error(
          "Proxy configured as client and server at the same time.");
    }

    std::cout << "\t"
              << "N Threads: " << n_thread << std::endl;
  }

  HTTPProxy* instantiateProxyAsValue() {
    if (client) {
      HTTPProxy::ClientParams* p = dynamic_cast<HTTPProxy::ClientParams*>(this);
      return new transport::HTTPProxy(*p, n_thread);
    } else if (server) {
      HTTPProxy::ServerParams* p = dynamic_cast<HTTPProxy::ServerParams*>(this);
      return new transport::HTTPProxy(*p, n_thread);
    } else {
      throw std::runtime_error(
          "Proxy configured as client and server at the same time.");
    }
  }

  bool client = false;
  bool server = false;
  std::uint16_t n_thread = 1;
};



void startHicnProxy(const char *prefix,
                    int listenPort) {

    Params params;

    params.prefix = std::string(prefix);
    params.origin_address = "127.0.0.1";
    params.cache_size = "50000";
    params.mtu = "1500";
    params.first_ipv6_word = std::string(prefix);
    params.content_lifetime = "7200;";  // seconds
    params.manifest = false;
    params.tcp_listen_port = listenPort;
    params.client = true;
    std::cout<<"prefix: " << params.first_ipv6_word  << " listenPort: " << listenPort<< std::endl;
    proxy = params.instantiateProxyAsValue();
    proxy->run();
    delete proxy;
}

void stopHicnProxy() {
    proxy->stop();
}

#!/bin/bash
vpp -c /etc/vpp/startup.conf
source /usr/lib/frr/frrcommon.sh
/usr/lib/frr/watchfrr $(daemon_list)
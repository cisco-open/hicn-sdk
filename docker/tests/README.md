# Functional test description


## Network topology

Host - H
Router - R
Linux Bridge - LB
VPP Router - VR

LB1 - 10.1.0.0/16
LB2 - 10.2.0.0/16
LB3 - 10.3.0.0/16

### L2 Topology
H1 -- LB1 -- R1 -- LB2 - R2 -- LB3 -- H2

### L3 Topology
Host1 -- Router1 -- Router2 -- Host2

### Interfaces addressing at the linux bridge
* LB1  - 10.1.0.1/16
* LB2  - 10.2.0.1/16
* LB3  - 10.3.0.1/16
* H1-1 - 10.1.0.2/16
* R1-1 - 10.1.0.3/16
* R1-2 - 10.2.0.2/16
* R2-1 - 10.2.0.3/16
* R2-2 - 10.3.0.2/16
* H2-1 - 10.3.0.3/16

### VPP interfaces addresses
* H1-1 - 10.11.0.2/16
* R1-1 - 10.11.0.3/16
* R1-2 - 10.22.0.2/16
* R2-1 - 10.22.0.3/16
* R2-2 - 10.33.0.2/16
* H2-1 - 10.33.0.3/16

#!/bin/bash

cp acuman.service /usr/lib/systemd/system/
mkdir -p /var/log/acuman/
systemctl daemon-reload
systemctl enable acuman

chown ubuntu:ubuntu /var/log/acuman
chown ubuntu:ubuntu /usr/lib/systemd/system/acuman.service

# use LB, don't need iptables routing
# iptables -A PREROUTING -t nat -i eth0 -p tcp --dport 80 -j REDIRECT --to-port 4567

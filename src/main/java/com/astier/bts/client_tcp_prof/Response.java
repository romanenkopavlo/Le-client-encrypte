package com.astier.bts.client_tcp_prof;

import java.net.InetAddress;

public record Response(InetAddress url, int port_TCP, int port_UDP) {
}
package com.alibaba.langengine.openmanus.tool.support;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class IpUtils {
    private static String LOCAL_IP = "127.0.0.1";

    public IpUtils() {
    }

    public static String getLocalIp() {
        return LOCAL_IP;
    }

    static {
        try {
            Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();

            label38:
            while(true) {
                NetworkInterface ni;
                do {
                    do {
                        if (!nis.hasMoreElements()) {
                            break label38;
                        }

                        ni = (NetworkInterface)nis.nextElement();
                    } while(ni.isLoopback());
                } while(ni.isVirtual());

                Enumeration<InetAddress> addresss = ni.getInetAddresses();

                while(addresss.hasMoreElements()) {
                    InetAddress address = (InetAddress)addresss.nextElement();
                    if (address instanceof Inet4Address) {
                        LOCAL_IP = address.getHostAddress();
                    }
                }
            }
        } catch (Throwable var4) {
        }
    }
}

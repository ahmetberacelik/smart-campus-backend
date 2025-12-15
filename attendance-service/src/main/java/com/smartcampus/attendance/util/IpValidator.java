package com.smartcampus.attendance.util;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class IpValidator {

    @Value("${attendance.campus-ip-ranges:10.0.0.0/8,172.16.0.0/12,192.168.0.0/16}")
    private String campusIpRanges;

    @Value("${attendance.ip-validation-enabled:true}")
    private boolean ipValidationEnabled;

    private List<CidrRange> cidrRanges;

    @PostConstruct
    public void init() {
        cidrRanges = new ArrayList<>();
        if (campusIpRanges != null && !campusIpRanges.isEmpty()) {
            String[] ranges = campusIpRanges.split(",");
            for (String range : ranges) {
                try {
                    cidrRanges.add(new CidrRange(range.trim()));
                } catch (Exception e) {
                    log.warn("Invalid CIDR range: {}", range);
                }
            }
        }
        log.info("Campus IP validation initialized with {} ranges, enabled: {}", cidrRanges.size(), ipValidationEnabled);
    }

    public boolean isIpValidationEnabled() {
        return ipValidationEnabled;
    }

    public boolean isOnCampusNetwork(String ipAddress) {
        if (!ipValidationEnabled) {
            return true;
        }

        if (ipAddress == null || ipAddress.isEmpty()) {
            return false;
        }

        if ("127.0.0.1".equals(ipAddress) || "0:0:0:0:0:0:0:1".equals(ipAddress) || "::1".equals(ipAddress)) {
            return true;
        }

        try {
            for (CidrRange range : cidrRanges) {
                if (range.contains(ipAddress)) {
                    return true;
                }
            }
        } catch (Exception e) {
            log.error("Error validating IP address: {}", ipAddress, e);
            return false;
        }

        return false;
    }

    private static class CidrRange {
        private final int networkAddress;
        private final int networkMask;

        public CidrRange(String cidr) throws UnknownHostException {
            String[] parts = cidr.split("/");
            String ip = parts[0];
            int prefixLength = parts.length > 1 ? Integer.parseInt(parts[1]) : 32;

            this.networkAddress = ipToInt(ip);
            this.networkMask = prefixLength == 0 ? 0 : (-1 << (32 - prefixLength));
        }

        public boolean contains(String ip) {
            try {
                int ipInt = ipToInt(ip);
                return (ipInt & networkMask) == (networkAddress & networkMask);
            } catch (Exception e) {
                return false;
            }
        }

        private static int ipToInt(String ip) throws UnknownHostException {
            InetAddress inetAddress = InetAddress.getByName(ip);
            byte[] bytes = inetAddress.getAddress();
            if (bytes.length != 4) {
                throw new UnknownHostException("Not an IPv4 address: " + ip);
            }
            return ((bytes[0] & 0xFF) << 24) |
                   ((bytes[1] & 0xFF) << 16) |
                   ((bytes[2] & 0xFF) << 8) |
                   (bytes[3] & 0xFF);
        }
    }
}

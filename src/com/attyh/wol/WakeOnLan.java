package com.attyh.wol;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import static java.lang.System.arraycopy;

/**
 * Author: Eric Kurzhals <ek@attyh.com>
 * Date: 05.12.13
 * Time: 10:04
 */
public class WakeOnLan {
    private static int PORT = 7;

    /**
     * An magic Paket is a broadcast frame where the first 6 bytes are 0xff, followed by 16x the destination mac address
     *
     * @param macAddr
     * @throws SocketException
     * @throws WakeOnLanException
     */
    public static void sendMagicPacket(String macAddr) throws SocketException, WakeOnLanException {
        Enumeration<NetworkInterface> nif = NetworkInterface.getNetworkInterfaces();
        UniqueList bcList = new UniqueList();

        while (nif.hasMoreElements()) {
            NetworkInterface ni = nif.nextElement();
            List<InterfaceAddress> interfaceAddresses = ni.getInterfaceAddresses();
            for (InterfaceAddress addr : interfaceAddresses) {
                bcList.add(addr.getBroadcast());
            }
        }

        for (InetAddress addr : bcList) {
            sendMagicPacket(addr, macAddr);
        }
    }

    /**
     * An magic Paket is a broadcast frame where the first 6 bytes are 0xff, followed by 16x the destination mac address
     *
     * @param broadcastAddr
     * @param macAddr
     * @throws UnknownHostException
     * @throws WakeOnLanException
     */
    public static void sendMagicPacket(String broadcastAddr, String macAddr) throws UnknownHostException, WakeOnLanException {
        InetAddress addr = InetAddress.getByName(broadcastAddr);
        sendMagicPacket(addr, macAddr);
    }

    /**
     * An magic Paket is a broadcast frame where the first 6 bytes are 0xff, followed by 16x the destination mac address
     *
     * @param broadcastAddr
     * @param macAddr
     * @throws WakeOnLanException
     */
    public static void sendMagicPacket(InetAddress broadcastAddr, String macAddr) throws WakeOnLanException {
        try {
            byte[] bytes;
            byte[] mac = parseMacAddress(macAddr);
            bytes = new byte[6 + 16 * mac.length];

            // fill bytes with 6bytes payload.
            for (int i = 0; i < 6; i++) {
                bytes[i] = (byte) 0xff;
            }

            // fill bytes with 16x mac address
            int iMax = bytes.length;
            int iIter = mac.length;
            for (int i = 6; i < iMax; i += iIter) {
                arraycopy(mac, 0, bytes, i, iIter);
            }

            // send the frame as datagram to destination
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length, broadcastAddr, PORT);
            DatagramSocket socket = new DatagramSocket();
            socket.send(packet);
            socket.close();
        } catch (IOException e) {
            throw new WakeOnLanException("Failed to send wol-packet.");
        }
    }

    /**
     * parses a string MAC address to an hex-byte array
     *
     * @param macAddr
     * @return
     */
    private static byte[] parseMacAddress(String macAddr) {
        int bytesLength = 6;
        String[] macSplit = macAddr.split(":");
        byte[] bytes = new byte[bytesLength];

        if (macSplit.length != bytesLength) {
            throw new IllegalArgumentException("Invalid MAC address");
        }

        for (int i = 0; i < bytesLength; i++) {
            bytes[i] = (byte) Integer.parseInt(macSplit[i], 16);
        }

        return bytes;
    }

    /**
     * uniqueList to store our Broadcast IP Addresses
     */
    private static class UniqueList extends ArrayList<InetAddress> {

        @Override
        public boolean add(InetAddress object) {
            if (!contains(object) && object != null)
                super.add(object);

            return true;
        }
    }

    public static class WakeOnLanException extends Exception {

        public WakeOnLanException(String msg) {
            super(msg);
        }
    }
}
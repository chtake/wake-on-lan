package com.attyh.wol;

import java.net.SocketException;

public class Main {

    public static void main(String[] args) {
        try {
            WakeOnLan.sendMagicPacket(args[0]);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (WakeOnLan.WakeOnLanException e) {
            e.printStackTrace();
        }
    }
}

package com.zm.emulator.detector;

import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;

/**
 * Author: hzlishang
 * Data: 2017/7/7 上午9:07
 * version:
 */

public class EmuCheckUtil {
    public static boolean getFinalResult(){
        return hasQemuSocket()
                ||hasQemuPipe()
                ||hasQemuTrace()
                ||hasQEmuDrivers()
                ||hasQEmuFiles()
                ||hasAdbInEmulator()
                ||hasGenyFiles()
                ||isEmulatorFromCpu();
    }
    //  qemu模拟器特征
    public static boolean hasQemuSocket() {
        File qemuSocket = new File("/dev/socket/qemud");
        return qemuSocket.exists();
    }

    public static boolean hasQemuPipe() {
        File qemuSocket = new File("/dev/qemu_pipe");
        return qemuSocket.exists();
    }

    public static boolean hasQemuTrace() {
        File qemuSocket = new File("/dev/qemu_trace");
        return qemuSocket.exists();
    }

    /**
     * Reads in the driver file, then checks a list for known QEmu drivers.
     *
     * @return {@code true} if any known drivers where found to exist or {@code false} if not.
     */
    private static String[] known_qemu_drivers = {"goldfish"};
    public static boolean hasQEmuDrivers() {
        for (File drivers_file : new File[]{new File("/proc/tty/drivers"), new File("/proc/cpuinfo")}) {
            if (drivers_file.exists() && drivers_file.canRead()) {
                // We don't care to read much past things since info we care about should be inside here
                byte[] data = new byte[1024];
                try {
                    InputStream is = new FileInputStream(drivers_file);
                    is.read(data);
                    is.close();
                } catch (Exception exception) {
                    exception.printStackTrace();
                }

                String driver_data = new String(data);
                Log.i("zm","driver_data:"+driver_data);
                for (String known_qemu_driver :known_qemu_drivers) {
                    if (driver_data.indexOf(known_qemu_driver) != -1) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Check the existence of known files used by the Android QEmu environment.
     *
     * @return {@code true} if any files where found to exist or {@code false} if not.
     */
    private static String[] known_files = {"/system/lib/libc_malloc_debug_qemu.so", "/sys/qemu_trace",
            "/system/bin/qemu-props"};
    public static boolean hasQEmuFiles() {
        for (String pipe : known_files) {
            File qemu_file = new File(pipe);
            if (qemu_file.exists()) {
                return true;
            }
        }

        return false;
    }
    public static class tcp {

        public int id;
        public long localIp;
        public int localPort;
        public int remoteIp;
        public int remotePort;

        static tcp create(String[] params) {
            return new tcp(params[1], params[2], params[3], params[4], params[5], params[6], params[7], params[8],
                    params[9], params[10], params[11], params[12], params[13], params[14]);
        }

        public tcp(String id, String localIp, String localPort, String remoteIp, String remotePort, String state,
                   String tx_queue, String rx_queue, String tr, String tm_when, String retrnsmt, String uid,
                   String timeout, String inode) {
            this.id = Integer.parseInt(id, 16);
            this.localIp = Long.parseLong(localIp, 16);
            this.localPort = Integer.parseInt(localPort, 16);
        }
    }
    /**
     * This was reversed from a sample someone was submitting to sandboxes for a thesis, can't find paper anymore
     *
     * @throws IOException
     */
    public static boolean  hasAdbInEmulator() {
        boolean adbInEmulator = false;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream("/proc/net/tcp")), 1000);
            String line;
            // Skip column names
            reader.readLine();

            ArrayList<tcp> tcpList = new ArrayList<tcp>();

            while ((line = reader.readLine()) != null) {
                tcpList.add(tcp.create(line.split("\\W+")));
            }

            reader.close();

            // Adb is always bounce to 0.0.0.0 - though the port can change
            // real devices should be != 127.0.0.1
            int adbPort = -1;
            for (tcp tcpItem : tcpList) {
                if (tcpItem.localIp == 0) {
                    adbPort = tcpItem.localPort;
                    break;
                }
            }

            if (adbPort != -1) {
                for (tcp tcpItem : tcpList) {
                    if ((tcpItem.localIp != 0) && (tcpItem.localPort == adbPort)) {
                        adbInEmulator = true;
                    }
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return adbInEmulator;
    }
    /**
     * Check the existence of known files used by the Genymotion environment.
     *
     * @return {@code true} if any files where found to exist or {@code false} if not.
     */
    private static String[] known_geny_files = {"/dev/socket/genyd", "/dev/socket/baseband_genyd"};
    public static boolean hasGenyFiles() {
        for (String file : known_geny_files) {
            File geny_file = new File(file);
            if (geny_file.exists()) {
                return true;
            }
        }

        return false;
    }

    // 查杀比较严格，放在最后，直接pass x86
    public static boolean isEmulatorFromCpu() {
        ShellAdbUtils.CommandResult commandResult = ShellAdbUtils.execCommand("cat /proc/cpuinfo", false);
        String cpuInfo = commandResult == null ? "" : commandResult.successMsg;
        return !TextUtils.isEmpty(cpuInfo) && ((cpuInfo.toLowerCase().contains("intel") || cpuInfo.toLowerCase().contains("amd")));
    }

//    public static String getCpuInfo() {
//        ShellAdbUtils.CommandResult commandResult = ShellAdbUtils.execCommand("cat /proc/cpuinfo", false);
//        return commandResult == null ? "" : commandResult.successMsg;
//    }

//    public static String getQEmuDriverFileString() {
//        File driver_file = new File("/proc/tty/drivers");
//        StringBuilder stringBuilder = new StringBuilder();
//        if (driver_file.exists() && driver_file.canRead()) {
//            try {
//                char[] data = new char[1024];  //(int)driver_file.length()
//                InputStream inStream = new FileInputStream(driver_file);
//                Reader in = new InputStreamReader(inStream, "UTF-8");
//                for (; ; ) {
//                    int rsz = in.read(data, 0, data.length);
//                    if (rsz < 0) {
//                        break;
//                    }
//                    stringBuilder.append(data, 0, rsz);
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            return stringBuilder.toString();
//        }
//        return "";
//    }
}

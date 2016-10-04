package project.pamela.slambench.utils;


import android.content.Context;
import android.os.Environment;
import android.os.StatFs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

/*
 * SLAMBench for Android
 * *********************
 * Author: Bruno Bodin.
 * Copyright (c) 2015 University of Edinburgh.
 * Developed in the PAMELA project, EPSRC Programme Grant EP/K008730/1
 * This code is licensed under the MIT License.
 */

enum SensorType {
    SENSOR_VOLTAGE, SENSOR_CURRENT, SENSOR_TEMP, SENSOR_CHARGE, SENSOR_CAPACITY, SENSOR_FULL_CAPACITY, SENSOR_FAN, GPU_CLOCK
}

class SensorFile {
    final String        filename;
    final double        coef;
    final SensorType    type;

    SensorFile(String filename, double coef, SensorType type) {
        this.filename = filename;
        this.coef = coef;
        this.type = type;
    }
}

public class MobileStats {


    private static int processor_count = 0;

    private static final SensorFile[] SENSOR_FILES = {

            new SensorFile("/sys/module/pvrsrvkm/parameters/sgx_gpu_clk",  1e-0 , SensorType.GPU_CLOCK),  // ODROID
            new SensorFile("/sys/bus/platform/devices/odroidxu-fan/pwm_duty",  100 / 255 , SensorType.SENSOR_FAN),  // ODROID

            new SensorFile( "/sys/devices/power_supply.13/power_supply/battery/voltage_now" ,1e-3,SensorType.SENSOR_VOLTAGE), // ODROID
            new SensorFile( "/sys/class/power_supply/battery/voltage_now" ,1e-6,SensorType.SENSOR_VOLTAGE), // GT2
            new SensorFile( "/sys/class/power_supply/battery/batt_vol_now",1e-3,SensorType.SENSOR_VOLTAGE),

            new SensorFile( "/sys/class/power_supply/battery/current_now",1e-6,SensorType.SENSOR_CURRENT), // Nexus 5, GT2
            new SensorFile( "/sys/class/power_supply/battery/batt_current_now",1e-3,SensorType.SENSOR_CURRENT),

            new SensorFile( "/sys/class/power_supply/battery/temp",1e-1,SensorType.SENSOR_TEMP),
            new SensorFile( "/sys/class/power_supply/battery/batt_temp_now",1e-1,SensorType.SENSOR_TEMP),
            new SensorFile( "/sys/class/thermal/thermal_zone12/temp",1e-0,SensorType.SENSOR_TEMP),
            new SensorFile( "/sys/devices/virtual/thermal/thermal_zone1/temp",1e-0,SensorType.SENSOR_TEMP),  // ODROID

            new SensorFile(  "/sys/class/power_supply/battery/charge_counter", 60 * 60 * 1e-6,SensorType.SENSOR_CHARGE),
            new SensorFile(  "/sys/class/power_supply/battery/capacity",1e-2,SensorType.SENSOR_CAPACITY),
            new SensorFile(  "/sys/class/power_supply/battery/full_bat", 60 * 60 * 1e-6,SensorType.SENSOR_FULL_CAPACITY),

    };

    private static MobileStats instance = null;
    private ArrayList<SensorFile> availableSensors;
    private String voltageFile;
    private String currentFile;
    private String tempFile;
    private String chargeFile;
    private String capacityFile;
    private String fullCapacityFile;
    private double voltageConv;
    private double currentConv;
    private double tempConv;
    private double chargeConv;
    private double capacityConv;
    private double fullCapacityConv;

    private MobileStats() {

        availableSensors = new ArrayList<SensorFile>(SENSOR_FILES.length);

        int i = 0;
        while (new File("/sys/devices/system/cpu/cpu" + i).exists()) {
            i++;
        }
        processor_count = i;

        for (SensorFile SENSOR_FILE : SENSOR_FILES) {
            if (new File(SENSOR_FILE.filename).exists()) {
                switch (SENSOR_FILE.type) {

                    case SENSOR_VOLTAGE:
                        long voltage = readLongFromFile(SENSOR_FILE.filename);
                        if (voltage != -1L) {
                            voltageFile = SENSOR_FILE.filename;
                            voltageConv = SENSOR_FILE.coef;
                            availableSensors.add(SENSOR_FILE);
                            MessageLog.addDebug(String.format("File %s exists.", SENSOR_FILE.filename));
                            voltageConv = Math.pow(10, -(Math.floor(Math.log10(voltage)))); // Voltage should be between 0 and 10
                        }
                        break;
                    case SENSOR_CURRENT:
                        long current = readLongFromFile(SENSOR_FILE.filename);
                        if (current != -1L) {
                            currentFile = SENSOR_FILE.filename;
                            currentConv = SENSOR_FILE.coef;
                            availableSensors.add(SENSOR_FILE);
                            MessageLog.addDebug(String.format("File %s exists.", SENSOR_FILE.filename));
                            currentConv = Math.pow(10, -(Math.floor(Math.log10(current)))); // Current should be between 0 and 10
                        }
                        break;
                    case SENSOR_TEMP:
                        long temp = readLongFromFile(SENSOR_FILE.filename);
                        if (temp != -1L) {
                            tempFile = SENSOR_FILE.filename;
                            tempConv = SENSOR_FILE.coef;
                            availableSensors.add(SENSOR_FILE);
                            MessageLog.addDebug(String.format("File %s exists.", SENSOR_FILE.filename));
                        }
                        tempConv = Math.pow(10, -(Math.floor(Math.log10(temp)) - 1)); // Temperature should be between 10 and 100
                        break;
                    case SENSOR_CHARGE:
                        long charge = readLongFromFile(SENSOR_FILE.filename);
                        if (charge != -1L) {
                            chargeFile = SENSOR_FILE.filename;
                            chargeConv = SENSOR_FILE.coef;
                            availableSensors.add(SENSOR_FILE);
                            MessageLog.addDebug(String.format("File %s exists.", SENSOR_FILE.filename));
                        }
                        break;
                    case SENSOR_CAPACITY:
                        long capa = readLongFromFile(SENSOR_FILE.filename);
                        if (capa != -1L) {
                            capacityFile = SENSOR_FILE.filename;
                            capacityConv = SENSOR_FILE.coef;
                            availableSensors.add(SENSOR_FILE);
                            MessageLog.addDebug(String.format("File %s exists.", SENSOR_FILE.filename));
                        }
                        break;
                    case SENSOR_FULL_CAPACITY:
                        long fcapa = readLongFromFile(SENSOR_FILE.filename);
                        if (fcapa != -1L) {
                            fullCapacityFile = SENSOR_FILE.filename;
                            fullCapacityConv = SENSOR_FILE.coef;
                            availableSensors.add(SENSOR_FILE);
                            MessageLog.addDebug(String.format("File %s exists.", SENSOR_FILE.filename));
                        }
                        break;
                }
            }
        }

    }

    public static MobileStats getInstance() {
        if (instance == null) {
            instance = new MobileStats();
        }
        return instance;
    }

    private static  long readLongFromFile(String file) {


        try {
            RandomAccessFile reader = new RandomAccessFile(file, "r");
            String res = reader.readLine();
            reader.close();
            return Long.valueOf(res);
        } catch (java.io.IOException | NullPointerException e) {
            e.printStackTrace();
        }


        return -1L;
    }
    private static String readLineFromFile(String file) {


        try {
            RandomAccessFile reader = new RandomAccessFile(file, "r");
            String res = reader.readLine();
            reader.close();
            return res;
        } catch (java.io.IOException | NullPointerException e) {
            return null;
        }

    }
    private static  int readIntFromFile(String file) {


        try {
            RandomAccessFile reader = new RandomAccessFile(file, "r");
            String res = reader.readLine();
            reader.close();
            return Integer.valueOf(res);
        } catch (java.io.IOException | NullPointerException e) {
            return 0;
        }

    }

    public boolean hasVoltage() {
        return voltageFile != null;
    }

    public double getPower() {
        return getVoltage() * getCurrent();
    }

    public double getVoltage() {
        if (voltageFile == null) return -1.0;
        long volt = readLongFromFile(voltageFile);
        return volt == -1 ? -1.0 : voltageConv * volt;
    }

    public boolean hasCurrent() {
        return currentFile != null;
    }

    public double getCurrent() {
        if (currentFile == null) return -1.0;
        long curr = readLongFromFile(currentFile);
        return curr == -1 ? -1.0 : currentConv * curr;
    }

    public boolean hasTemp() {
        return tempFile != null;
    }

    public double getTemp() {
        if (tempFile == null) return -1.0;
        long temp = readLongFromFile(tempFile);
        return temp == -1 ? -1.0 : tempConv * temp;
    }

    public boolean hasCharge() {
        return chargeFile != null ||
                hasFullCapacity() && hasCapacity();
    }

    public double getCharge() {
        if (chargeFile == null) {
            double r1 = getCapacity();
            double r2 = getFullCapacity();
            return r1 < 0 || r2 < 0 ? -1.0 : r1 * r2;
        }
        long charge = readLongFromFile(chargeFile);
        return charge == -1 ? -1.0 : chargeConv * charge;
    }

    private boolean hasCapacity() {
        return capacityFile != null;
    }

    public double getCapacity() {
        if (capacityFile == null) return -1.0;
        long cap = readLongFromFile(capacityFile);
        return cap == -1 ? -1.0 : capacityConv * cap;
    }

    private boolean hasFullCapacity() {
        return fullCapacityFile != null;
    }

    public double getFullCapacity() {
        if (fullCapacityFile == null) return -1.0;
        long cap = readLongFromFile(fullCapacityFile);
        return cap == -1 ? -1.0 : fullCapacityConv * cap;
    }

    public int[] getCPUFrequencies() {
        int[] freqs = new int[processor_count];
        for (int i = 0 ; i < processor_count; i++) {
            int f =  0;
            try {
              f = readIntFromFile("/sys/devices/system/cpu/cpu" + i + "/cpufreq/scaling_cur_freq");
            } finally {
             freqs[i] = f;
             }
        }

        return freqs;
    }



    public static long getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return (availableBlocks * blockSize);
    }


    public static long getUsedInternalMemorySize(Context c) {
        long size = 0;
        if (c == null) {
            System.out.println(String.format("Error with null context."));
            return size;
        }
        File[] files = c.getCacheDir().listFiles();
        for (File f:files) {
            size = size + f.length();
        }
        return size;
    }


    public static String getCPUInfo() {

        String s = "";

        if (new File("/proc/cpuinfo").exists()) {

            try {
                BufferedReader br = new BufferedReader(new FileReader(new File("/proc/cpuinfo")));
                String aLine;
                while ((aLine = br.readLine()) != null) {
                    s += aLine + "\n";
                }

                if (br != null) {
                    br.close();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return s;

}

    public static String getCPUGovernor() {
        return readLineFromFile("/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor");
    }




}

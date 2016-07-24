package com.marcinmajkowski.membershipsoftware.barcodescanner;

import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Scanner;

public class BarcodeScannerApplication {

    private static volatile boolean keepRunning = true;

    private final static Thread mainThread = Thread.currentThread();

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                keepRunning = false;
                try {
                    mainThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        CloseableHttpClient httpClient = null;
        SerialPort serialPort = null;
        String scannerPortName = null;
        try {
            Properties config = readConfig();

            String apiEndpointUrl = config.getProperty("api-endpoint-url", "http://localhost:8080");

            httpClient = HttpClients.createDefault();

            RestfulClient restfulClient = new RestfulClient(apiEndpointUrl, httpClient);

            CodeScannedEventHandler codeScannedEventHandler = new CodeScannedEventHandler(restfulClient);

            scannerPortName = config.getProperty("scanner.port", "COM3");

            serialPort = new SerialPort(scannerPortName);

            BarcodeScannerSerialPortEventListener barcodeScannerSerialPortEventListener = new BarcodeScannerSerialPortEventListener(serialPort, codeScannedEventHandler);

            serialPort.openPort();
            serialPort.setParams(
                    SerialPort.BAUDRATE_9600,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE
            );
            serialPort.setEventsMask(SerialPort.MASK_RXCHAR + SerialPort.MASK_DSR);
            serialPort.addEventListener(barcodeScannerSerialPortEventListener);

            run();
        } catch (SerialPortException e) {
            String exceptionType = e.getExceptionType();
            if (exceptionType.equals(SerialPortException.TYPE_PORT_NOT_FOUND)) {
                System.out.println("Serial port " + scannerPortName + " not found.");
                String[] portNames = SerialPortList.getPortNames();
                if (portNames.length > 0) {
                    System.out.print("Available serial ports:");
                    boolean first = true;
                    for (String portName : portNames) {
                        if (!first) {
                            System.out.print(", ");
                        } else {
                            System.out.print(" ");
                        }
                        System.out.print(portName);
                        first = false;
                    }
                    System.out.println(".");
                } else {
                    System.out.println("There are no serial ports available in your system. Check if scanner device is connected and drivers are installed.");
                }
            } else {
                e.printStackTrace();
            }
        } finally {
            if (httpClient != null) {
                try {
                    System.out.print("Closing http client...");
                    httpClient.close();
                    System.out.println(" closed.");
                } catch (IOException e) {
                    System.out.println(" an error occurred.");
                    e.printStackTrace();
                }
            }
            closeSerialPort(serialPort);
        }
    }

    private static Properties readConfig() {
        Properties config = new Properties();
        try (InputStream inputStream = new FileInputStream("config.properties")) {
            config.load(inputStream);
        } catch (FileNotFoundException e) {
            System.out.println("config.properties file not found.");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return config;
    }

    private static void run() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Running...");

        while (keepRunning) {
            if (scanner.hasNextLine()) {
                String command = scanner.nextLine();

                if (command.toLowerCase().equals("exit") || command.toLowerCase().equals("q")) {
                    keepRunning = false;
                }
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Stopping...");
    }

    private static void closeSerialPort(SerialPort serialPort) {
        if (serialPort != null && serialPort.isOpened()) {
            try {
                System.out.print("Closing serial port...");
                serialPort.purgePort(SerialPort.PURGE_TXABORT);
                serialPort.purgePort(SerialPort.PURGE_RXABORT);
                serialPort.closePort();
                System.out.println(" closed.");
            } catch (SerialPortException e) {
                System.out.println(" an error occurred.");
                e.printStackTrace();
            }
        }
    }
}

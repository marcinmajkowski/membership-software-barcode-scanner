package com.marcinmajkowski.membershipsoftware.barcodescanner;

import jssc.SerialPort;
import jssc.SerialPortException;

import javax.json.Json;
import javax.json.JsonObject;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Properties;
import java.util.Scanner;

public class BarcodeScannerApplication {

    private static volatile boolean keepRunning = true;

    private static Thread mainThread = Thread.currentThread();

    private final static String apiEndpointUrl;

    private final static String scannerPort;

    private SerialPort serialPort;

    private final RestfulClient restfulClient;

    static {
        Properties prop = new Properties();
        try (InputStream inputStream = new FileInputStream("config.properties")) {
            prop.load(inputStream);
        } catch (FileNotFoundException e) {
            System.out.println("config.properties file not found");
        } catch (IOException e) {
            e.printStackTrace();
        }

        apiEndpointUrl = prop.getProperty("api-endpoint-url", "http://localhost:8080");
        scannerPort = prop.getProperty("scanner.port", "COM3");
    }

    public BarcodeScannerApplication(RestfulClient restfulClient) {
        this.restfulClient = restfulClient;
    }

    public void run() {
        initializeSerialPort();
        Scanner scanner = new Scanner(System.in);

        System.out.println("Running...");
        System.out.print("> ");

        while (keepRunning) {
            if (scanner.hasNextLine()) {
                String command = scanner.nextLine();

                switch (command.toLowerCase()) {
                    case "exit":
                        keepRunning = false;
                        break;
                    case "url":
                        System.out.println("API url: " + apiEndpointUrl);
                        break;
                    default:
                        JsonObject payload = Json.createObjectBuilder()
                                .add("value", command)
                                .build();
                        try {
                            int statusCode = restfulClient.post(payload);
                            System.out.println("Server responded with " + statusCode);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                }

                System.out.print("> ");
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("\nCleaning up...");
        closeSerialPort();
    }

    private void initializeSerialPort() {
        System.out.println("Connecting " + scannerPort);

        serialPort = new SerialPort(scannerPort);
        try {
            serialPort.openPort();
            serialPort.setParams(
                    SerialPort.BAUDRATE_9600,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE
            );
            int mask = SerialPort.MASK_RXCHAR + SerialPort.MASK_DSR;
            serialPort.setEventsMask(mask);
            serialPort.addEventListener(new BarcodeScannerSerialPortEventListener(serialPort, Collections.singletonList((CodeScannedEventHandler) code -> {
                JsonObject payload = Json.createObjectBuilder()
                        .add("code", code)
                        .build();
                try {
                    int statusCode = restfulClient.post(payload);
                    System.out.println("Server responded with " + statusCode);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            })));
        } catch (SerialPortException e) {
            switch (e.getExceptionType()) {
                case SerialPortException.TYPE_PORT_NOT_FOUND:
                    System.out.println("Port " + scannerPort + " not found");
                    break;
                default:
                    e.printStackTrace();
            }
        }
    }

    private void closeSerialPort() {
        System.out.println("Closing serial port");
        if (serialPort != null && serialPort.isOpened()) {
            try {
                serialPort.purgePort(SerialPort.PURGE_TXABORT);
                serialPort.purgePort(SerialPort.PURGE_RXABORT);
                serialPort.closePort();
            } catch (SerialPortException e) {
                e.printStackTrace();
            }
        }
    }

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

        RestfulClient restfulClient = new RestfulClient(apiEndpointUrl);

        new BarcodeScannerApplication(restfulClient).run();
    }
}

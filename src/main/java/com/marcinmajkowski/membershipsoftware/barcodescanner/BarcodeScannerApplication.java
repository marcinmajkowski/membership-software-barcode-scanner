package com.marcinmajkowski.membershipsoftware.barcodescanner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Scanner;

public class BarcodeScannerApplication {

    private static volatile boolean keepRunning = true;

    private static Thread mainThread = Thread.currentThread();

    private static String apiEndpointUrl;

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
    }

    public void run() {
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
                }

                if ("exit".equalsIgnoreCase(command)) {
                    keepRunning = false;
                }

                System.out.println("Input: " + command);
                System.out.print("> ");
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("\nCleaning up...");
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

        new BarcodeScannerApplication().run();
    }
}

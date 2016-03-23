package com.marcinmajkowski.membershipsoftware.barcodescanner;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import org.apache.commons.codec.Charsets;

import java.util.List;

public class BarcodeScannerSerialPortEventListener implements SerialPortEventListener {

    private final static String LINE_TERMINATOR = "\r\n";

    private StringBuilder receiverCharacters = new StringBuilder();

    private final SerialPort serialPort;

    private final List<CodeScannedEventHandler> handlers;

    public BarcodeScannerSerialPortEventListener(SerialPort serialPort, List<CodeScannedEventHandler> handlers) {
        if (serialPort == null || handlers == null) {
            throw new IllegalArgumentException(new NullPointerException());
        }
        this.serialPort = serialPort;
        this.handlers = handlers;
    }

    @Override
    public void serialEvent(SerialPortEvent serialPortEvent) {
        System.out.println("COM Port event occured");
        // If data is available
        if (serialPortEvent.isRXCHAR()) {
            final int bytesAvailable = serialPortEvent.getEventValue();
            System.out.println(bytesAvailable + " bytes available");
            try {
                byte[] bytes = serialPort.readBytes(bytesAvailable);
                String receivedContent = new String(bytes, Charsets.UTF_8);
                System.out.println("Received: \"" + receivedContent + "\"");
                receiverCharacters.append(receivedContent);
                int lineTerminatorIndex;
                while ((lineTerminatorIndex = receiverCharacters.indexOf(LINE_TERMINATOR)) != -1) {
                    String code = receiverCharacters.substring(0, lineTerminatorIndex);
                    System.out.println("Handling: \"" + code + "\"");
                    for (CodeScannedEventHandler handler : handlers) {
                        handler.handle(code);
                    }
                    receiverCharacters.delete(0, lineTerminatorIndex + LINE_TERMINATOR.length());
                }
            } catch (SerialPortException e) {
                e.printStackTrace();
            }
        } else if (serialPortEvent.isDSR()) {
            System.out.println("DSR - " + (serialPortEvent.getEventValue() == 1 ? "ON" : "OFF"));
        }
    }
}

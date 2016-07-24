package com.marcinmajkowski.membershipsoftware.barcodescanner;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import org.apache.commons.codec.Charsets;

public class BarcodeScannerSerialPortEventListener implements SerialPortEventListener {

    private final static String LINE_TERMINATOR = "\r\n";

    private StringBuilder receiverCharacters = new StringBuilder();

    private final SerialPort serialPort;

    private final CodeScannedEventHandler handler;

    public BarcodeScannerSerialPortEventListener(SerialPort serialPort, CodeScannedEventHandler handler) {
        if (serialPort == null || handler == null) {
            throw new IllegalArgumentException(new NullPointerException());
        }
        this.serialPort = serialPort;
        this.handler = handler;
    }

    @Override
    public void serialEvent(SerialPortEvent serialPortEvent) {
        // If data is available
        if (serialPortEvent.isRXCHAR()) {
            final int bytesAvailable = serialPortEvent.getEventValue();
            try {
                byte[] bytes = serialPort.readBytes(bytesAvailable);
                String receivedContent = new String(bytes, Charsets.UTF_8);
                receiverCharacters.append(receivedContent);
                int lineTerminatorIndex;
                while ((lineTerminatorIndex = receiverCharacters.indexOf(LINE_TERMINATOR)) != -1) {
                    String code = receiverCharacters.substring(0, lineTerminatorIndex);
                    System.out.println("Scanned: \"" + code + "\".");
                    if (code.matches("^\\d{12}$")) {
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

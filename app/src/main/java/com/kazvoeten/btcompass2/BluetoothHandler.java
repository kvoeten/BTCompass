/*     
    This file is part of .PNG Arduino Framework.

    .PNG Arduino Framework is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    .PNG Arduino Framework is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with .PNG Arduino Framework.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.kazvoeten.btcompass2;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class BluetoothHandler extends Thread {
    //Instance
    private static BluetoothHandler instance = null;

    //Bluetooth handling classes
    private BluetoothAdapter mBlueAdapter = null;
    private BluetoothSocket mBlueSocket = null;
    private BluetoothDevice mBlueDevice = null;

    //I/O streams
    private OutputStream mOut;
    private InputStream mIn;

    //Device constants
    private boolean deviceFound = false;
    private boolean connected = false;
    private String deviceName; //Name of the connected device

    //Messages that have arrived
    private List<String> mMessages = new ArrayList<>();

    //Tag constants
    private static final String LOG_TAG = "BluetoothConnector",
            START_TAG = "#",
            END_TAG = "%";


    /**
     * Get instance of the BluetoothHandler
     *
     * @param deviceName name of the external device
     * @return BluetoothHandler instance
     */
    public static BluetoothHandler getInstance(String deviceName) {
        return instance == null ? new BluetoothHandler(deviceName) : instance;
    }

    /**
     * Create a a default adapter with supplied name
     * @param Name - name of the connected BT device
     */
    private BluetoothHandler(String Name) {
        instance = this;
        try {
            deviceName = Name;
            mBlueAdapter = BluetoothAdapter.getDefaultAdapter();
        } catch (Exception e) {
            LogError("[ERROR] Unable to create a bluetooth instance: " + e.getMessage());
        }
    }

    /**
     * Check if the bluetooth is enabled
     *
     * @return boolean isEnabled
     */
    public boolean isBluetoothEnabled() {
        return mBlueAdapter.isEnabled();
    }

    /**
     * Run a thread to read and receive messages
     */
    public void run() {
        if (!connected) {
            if (mBlueAdapter == null) return;
            if (!isBluetoothEnabled()) return;

            //Fetch all connected devices and connect to the device matching the supplied name
            Set<BluetoothDevice> paired = mBlueAdapter.getBondedDevices();
            if (paired.size() > 0) {
                for (BluetoothDevice d : paired) {
                    if (d.getName().equals(deviceName)) {
                        mBlueDevice = d;
                        deviceFound = true;
                        break;
                    }
                }
            }

            if (!deviceFound) return; //Return if the correct device wasn't found

            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
            mBlueSocket = null;

            try {
                mBlueSocket = mBlueDevice.createRfcommSocketToServiceRecord(uuid);
            } catch (Exception e) {Log.e("","Error creating socket");}

            try {
                mBlueSocket.connect();
                Log.e("","[LOG] Bluetooth Connected");
            } catch (IOException e) {
                Log.e("", e.getMessage());
                try {
                    Log.e("", "[LOG] Bluetooth Socket connection failed, trying fallback...");

                    mBlueSocket = (BluetoothSocket) mBlueDevice.getClass().getMethod("createRfcommSocket", new Class[]{int.class}).invoke(mBlueDevice, 1);
                    mBlueSocket.connect();

                    Log.e("", "[LOG] Bluetooth Socket Connected");
                } catch (Exception ex) {
                    CompassActivity.getInstance().showError("Het kompas is niet gevonden, je kunt de app nog wel gebruiken om je dag te plannen.", false);
                    ex.printStackTrace();
                }
            }

            try {
                mOut = mBlueSocket.getOutputStream();
                mIn = mBlueSocket.getInputStream();
            } catch (Exception e) {e.printStackTrace();}

            connected = true;

            LogMessage("Connected! " + mBlueAdapter.getName());
            connected = true;
            return;
        }

        while (true) {
            if (connected) {
                try {
                    byte ch, buffer[] = new byte[1024];//Max buffer/message size defined as 1024.
                    int i = 0;

                    String s = "";
                    while (mIn.available() > 0 && i < buffer.length - 2) {
                        buffer[++i] = (byte) mIn.read();
                        try {
                            super.sleep(10); //Sleep thread for 10 ms for socket to fill
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    //Cast message to string and splice if format is correct
                    String msg = new String(buffer);
                    if ((!msg.contains(START_TAG) || !msg.contains(END_TAG))
                            || msg.indexOf(START_TAG) >= msg.indexOf(END_TAG)
                            || msg.indexOf(START_TAG) == buffer.length
                            || msg.indexOf(END_TAG) == buffer.length) {
                        continue;
                    }

                    msg = msg.substring(msg.indexOf(START_TAG) + 1, msg.indexOf(END_TAG));
                    LogMessage("[Log] Bluetooth Received: " + msg);

                } catch (IOException e) {
                    LogError("->[Error] Failed to receive bluetooth message: " + e.getMessage());
                }
            }
        }
    }

    /**
     * A message was received. Add it to the list
     * @param msg received messaged
     */
    private void messageReceived(String msg) {
        mMessages.add(msg);
    }

    /**
     * Get a message by the position in the List
     * @param i position of the message
     * @return "" if the position is not valid. Otherwise the message
     */
    public String getMessage(int i) {
        if (i >= 0 && i < mMessages.size())
            return mMessages.get(i);
        return "";
    }

    /**
     * Clear the list of received messages
     */
    public void clearMessages() {
        mMessages.clear();
    }

    /**
     * Count the received messages
     * @return int
     */
    public int countMessages() {
        return mMessages.size();
    }

    /**
     * Returns the last message received
     * @return
     */
    public String getLastMessage() {
        if (countMessages() == 0)
            return "";
        return mMessages.get(countMessages() - 1);
    }

    /**
     * Send a message to the connected Arduino
     * @param msg String with the message
     */
    public void sendMessage(String msg) {
        try {
            if (connected) {
                String formatted = START_TAG + msg + END_TAG;
                mOut.write(formatted.getBytes());
            }
        } catch (IOException e) {
            LogError("->[#]Error while sending message: " + e.getMessage());
        }
    }

    /**
     * Log an message
     * @param msg
     */
    private void LogMessage(String msg) {
        Log.d(LOG_TAG, msg);
    }

    /**
     * Log an error
     * @param msg
     */
    private void LogError(String msg) {
        Log.e(LOG_TAG, msg);
    }
}
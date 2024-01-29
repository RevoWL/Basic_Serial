package com.telemedc.d2scanner.utils

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import java.io.IOException

object ArduinoSerial {
    var port: UsbSerialPort? = null
    var connection: UsbDeviceConnection? = null

    @Throws(IOException::class)
    fun findUSBDevice(ctx: Context) {
        // Find all available drivers from attached devices.
        val manager = ctx.getSystemService(Context.USB_SERVICE) as UsbManager
        val availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager)
        if (!availableDrivers.isEmpty()) {
            val driver = availableDrivers[0]
            port = driver.ports[0] // Most devices have just one port (port 0)
            connection = manager.openDevice(driver.device)
            if (connection == null) {
                // add UsbManager.requestPermission(driver.getDevice(), ..) handling here
                val usbManager = ctx.getSystemService(Context.USB_SERVICE) as UsbManager
                val usbDevice = driver.device // Obtain the USB device you want to access
                val permissionIntent = PendingIntent.getBroadcast(ctx, 0, Intent(Context.USB_SERVICE), PendingIntent.FLAG_IMMUTABLE)

                usbManager.requestPermission(usbDevice, permissionIntent)
            }
            while (connection == null) {
                connection = manager.openDevice(driver.device)
            }
            port?.open(connection)
            port?.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)
        }
    }
}

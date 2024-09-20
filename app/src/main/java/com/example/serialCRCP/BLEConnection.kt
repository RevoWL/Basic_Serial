package com.example.serialCRCP

import android.annotation.SuppressLint
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Handler
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import java.util.UUID

class BLEConnection {

    companion object {
        @Volatile
        private var instance: BLEConnection? = null

        fun getInstance() = instance ?: synchronized(this) {
            instance ?: BLEConnection().also { instance = it }
        }
    }

    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothLeScanner: BluetoothLeScanner
    private lateinit var BLEscanCallback: ScanCallback
    private var scanning = false
    private val handler = Handler()
    private val SCAN_PERIOD: Long = 3000
    var bleScanListener: DeviceScanListener? = null
    var BLEGatt: BluetoothGatt? = null
    var manualDisconnect: Boolean = false

    fun init_BLE(context: Context) {
        bluetoothManager = context.getSystemService(Service.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.getAdapter()

        if (!bluetoothAdapter.isEnabled) {
            Toast.makeText(context, "Bluetooth not turned on", Toast.LENGTH_SHORT).show()
        }

    }

    interface DeviceScanListener {
        fun onScanResultsAvailable(
            scanResult: ScanResult?
        )

        fun onBLEConnected(
            connected: Boolean
        )
    }

    fun setDeviceScanListener(listener: DeviceScanListener) {
        bleScanListener = listener
    }

    @SuppressLint("MissingPermission")
    fun scanLeDevice() {

        bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
        val bleDeviceListAdapter = bleDeviceListAdapter()

        BLEscanCallback = object : ScanCallback() {
            override fun onBatchScanResults(results: MutableList<ScanResult>?) {
                super.onBatchScanResults(results)

            }

            override fun onScanFailed(errorCode: Int) {
                super.onScanFailed(errorCode)
            }

            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                super.onScanResult(callbackType, result)
                bleScanListener?.onScanResultsAvailable(result)
            }
        }

        if (!scanning) { // Stops scanning after a pre-defined scan period.
            handler.postDelayed({
                scanning = false
                bluetoothLeScanner.stopScan(BLEscanCallback)
            }, SCAN_PERIOD)

            scanning = true
            bluetoothLeScanner.startScan(BLEscanCallback)

        } else {
            scanning = false
            bluetoothLeScanner.stopScan(BLEscanCallback)
        }
    }

    @SuppressLint("MissingPermission")
    fun connectBLE(ctx: Context, device: BluetoothDevice?) {

        BLEGatt = device?.connectGatt(ctx, false, object : BluetoothGattCallback() {

            override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                super.onConnectionStateChange(gatt, status, newState)
                println(status == BluetoothGatt.GATT_SUCCESS)
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    bleScanListener?.onBLEConnected(true)
                    gatt?.discoverServices()
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    if (!manualDisconnect) {
                        BLEGatt?.connect()
                    }
                    bleScanListener?.onBLEConnected(false)
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                super.onServicesDiscovered(gatt, status)
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    val services = gatt?.services

                    services?.forEach { service ->
                        println(service)
                        // Log the UUID of the service
                        println("Service UUID: ${service.uuid}")

                        // Get the list of characteristics for this service
                        val characteristics = service.characteristics

                        characteristics.forEach { characteristic ->
                            // Log the UUID of the characteristic
                            println("    Characteristic UUID: ${characteristic.uuid}")

                            // Optionally, log the properties of the characteristic
                            val properties = characteristic.properties
                            if ((properties and BluetoothGattCharacteristic.PROPERTY_READ) != 0) {
                                println("        Property: READ")
                            }
                            if ((properties and BluetoothGattCharacteristic.PROPERTY_WRITE) != 0) {
                                println("        Property: WRITE")
                            }
                            if ((properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {
                                println("        Property: NOTIFY")
                                    // Enable notifications on this characteristic
                                    gatt.setCharacteristicNotification(characteristic, true)

                                    // Enable notifications on the BLE server by writing to the descriptor
                                    val descriptor = characteristic.descriptors.get(0)
                                    descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                                    gatt.writeDescriptor(descriptor)
                            }
                        }
                    }
                }
            }

            override fun onServiceChanged(gatt: BluetoothGatt) {
                super.onServiceChanged(gatt)
            }

            override fun onReliableWriteCompleted(gatt: BluetoothGatt?, status: Int) {
                super.onReliableWriteCompleted(gatt, status)
            }

            override fun onReadRemoteRssi(gatt: BluetoothGatt?, rssi: Int, status: Int) {
                super.onReadRemoteRssi(gatt, rssi, status)
            }

            override fun onPhyUpdate(gatt: BluetoothGatt?, txPhy: Int, rxPhy: Int, status: Int) {
                super.onPhyUpdate(gatt, txPhy, rxPhy, status)
            }

            override fun onPhyRead(gatt: BluetoothGatt?, txPhy: Int, rxPhy: Int, status: Int) {
                super.onPhyRead(gatt, txPhy, rxPhy, status)
            }

            override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
                super.onMtuChanged(gatt, mtu, status)
            }

            override fun onDescriptorWrite(
                gatt: BluetoothGatt?,
                descriptor: BluetoothGattDescriptor?,
                status: Int
            ) {
                super.onDescriptorWrite(gatt, descriptor, status)
            }

            override fun onDescriptorRead(
                gatt: BluetoothGatt,
                descriptor: BluetoothGattDescriptor,
                status: Int,
                value: ByteArray
            ) {
                super.onDescriptorRead(gatt, descriptor, status, value)
            }

            override fun onCharacteristicWrite(
                gatt: BluetoothGatt?,
                characteristic: BluetoothGattCharacteristic?,
                status: Int
            ) {
                super.onCharacteristicWrite(gatt, characteristic, status)
            }

            override fun onCharacteristicRead(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                value: ByteArray,
                status: Int
            ) {
                super.onCharacteristicRead(gatt, characteristic, value, status)
            }

            override fun onCharacteristicChanged(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                value: ByteArray
            ) {
                super.onCharacteristicChanged(gatt, characteristic, value)

                println(value.decodeToString())
            }


        })

    }
}
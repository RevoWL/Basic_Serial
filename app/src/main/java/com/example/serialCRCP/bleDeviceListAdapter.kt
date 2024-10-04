package com.example.serialCRCP

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.serialCRCP.databinding.DeviceCardBinding

@Deprecated("Not longer using it for this version")
class bleDeviceListAdapter : RecyclerView.Adapter<bleDeviceListAdapter.ViewHolder>() {


    private var bleDeviceList: MutableList<BluetoothDevice?> = mutableListOf()
    private var deviceSet: MutableSet<BluetoothDevice?> = mutableSetOf()
    private val BLEinstance = BLEConnection.getInstance()
    private lateinit var ctx: Context

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        ctx = parent.context

        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = DeviceCardBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    @SuppressLint("MissingPermission")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.binding.serverId.text = bleDeviceList[position]?.name ?: "-"
        holder.binding.serverInfo.text = bleDeviceList[position]?.address

        holder.binding.deviceCard.setOnClickListener {
            BLEinstance.connectBLE(ctx, bleDeviceList[position])
        }
    }

    override fun getItemCount(): Int {
        return bleDeviceList.size
    }

    @SuppressLint("MissingPermission")
    fun updateDeviceList(device: ScanResult?) {
        if (deviceSet.contains(device?.device)) {
            return
        } else {
            deviceSet.add(device?.device)
        }
        if (device?.scanRecord?.serviceUuids.toString().contains("4fafc201")
        ) {
            println(device)
            bleDeviceList.add(device?.device)
            notifyDataSetChanged()
        } else {
            return
        }

    }

    fun clearList() {
        deviceSet.clear()
        bleDeviceList.clear()
        notifyDataSetChanged()
    }

    class ViewHolder(var binding: DeviceCardBinding) : RecyclerView.ViewHolder(binding.getRoot())

}
package com.example.service

import android.os.Build
import android.telecom.Call
import android.telecom.InCallService
import android.util.Log
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.M)
class SmartInCallService : InCallService() {

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        Log.d("SmartInCallService", "System Call Added: ${call.details?.handle}")
        
        // Notify any active listener (e.g., MainActivity or ViewModel) about the new system call
        listener?.onSystemCallAdded(call)

        // Register state callbacks for this specific call
        call.registerCallback(callCallback)
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        Log.d("SmartInCallService", "System Call Removed")
        
        listener?.onSystemCallRemoved(call)
        call.unregisterCallback(callCallback)
    }

    private val callCallback = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            super.onStateChanged(call, state)
            Log.d("SmartInCallService", "System Call State Changed: $state")
            listener?.onSystemCallStateChanged(call, state)
        }
    }

    companion object {
        private var listener: SystemCallListener? = null

        fun setListener(systemCallListener: SystemCallListener?) {
            listener = systemCallListener
        }
    }

    interface SystemCallListener {
        fun onSystemCallAdded(call: Call)
        fun onSystemCallRemoved(call: Call)
        fun onSystemCallStateChanged(call: Call, state: Int)
    }
}

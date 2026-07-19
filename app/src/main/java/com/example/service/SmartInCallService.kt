package com.example.service

import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.InCallService
import android.util.Log
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.M)
class SmartInCallService : InCallService() {

    override fun onBind(intent: Intent): IBinder? {
        instance = this
        Log.d("SmartInCallService", "SmartInCallService Bound")
        return super.onBind(intent)
    }

    override fun onUnbind(intent: Intent): Boolean {
        instance = null
        Log.d("SmartInCallService", "SmartInCallService Unbound")
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        instance = null
        super.onDestroy()
    }

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
        private var instance: SmartInCallService? = null

        fun setListener(systemCallListener: SystemCallListener?) {
            listener = systemCallListener
        }

        fun toggleSystemMute(muted: Boolean) {
            try {
                instance?.setMuted(muted)
                Log.d("SmartInCallService", "System Mute state set to: $muted")
            } catch (e: Exception) {
                Log.e("SmartInCallService", "Failed to set system mute state", e)
            }
        }

        fun toggleSystemSpeaker(speakerOn: Boolean) {
            try {
                val route = if (speakerOn) {
                    CallAudioState.ROUTE_SPEAKER
                } else {
                    CallAudioState.ROUTE_WIRED_OR_EARPIECE
                }
                instance?.setAudioRoute(route)
                Log.d("SmartInCallService", "System Audio Route set to: $route")
            } catch (e: Exception) {
                Log.e("SmartInCallService", "Failed to set system audio route", e)
            }
        }
    }

    interface SystemCallListener {
        fun onSystemCallAdded(call: Call)
        fun onSystemCallRemoved(call: Call)
        fun onSystemCallStateChanged(call: Call, state: Int)
    }
}

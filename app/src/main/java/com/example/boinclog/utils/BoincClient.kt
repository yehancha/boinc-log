package com.example.boinclog.utils

import com.example.boinclog.boinc.RpcClient

class BoincClient {
    private val rpcClient = RpcClient()

    private fun connect() {
        rpcClient.open("67.205.153.68", 31416)
        rpcClient.authorize("Idon1tknowwhy1")
    }

    private fun disconnect() {
        rpcClient.close()
    }

    fun getMessageCount(): Int {
        connect()
        val count = rpcClient.messageCount
        disconnect()
        return count
    }

    fun getClientStatus(seqNo: Int = 0): BoincClientStatus {
        val status = BoincClientStatus()

        connect()
        status.messageCount = rpcClient.messageCount
        status.messages = rpcClient.getMessages(seqNo)
        status.projects = rpcClient.projectStatus
        disconnect()

        return status
    }
}
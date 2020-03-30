package com.example.boinclog.utils

import com.example.boinclog.boinc.RpcClient

class RpcClientFactory {
    companion object {
        fun getClient(): RpcClient {
            val rpcClient = RpcClient();
            rpcClient.open("67.205.153.68", 31416)
            rpcClient.authorize("Idon1tknowwhy1")
            return rpcClient
        }
    }
}
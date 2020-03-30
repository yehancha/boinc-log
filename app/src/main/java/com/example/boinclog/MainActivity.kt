package com.example.boinclog

import android.os.Bundle
import android.os.StrictMode
import androidx.appcompat.app.AppCompatActivity
import com.example.boinclog.boinc.Message
import com.example.boinclog.utils.LocalData
import com.example.boinclog.utils.NotificationChannelHandler
import com.example.boinclog.utils.RpcClientFactory
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*

private const val EXTRA_PAGE_SIZE = 10
private val FORMAT = SimpleDateFormat("yyyy-MM-dd HH:mm")

class MainActivity : AppCompatActivity() {
    var loadExtra = 0

    lateinit var localDate: LocalData
    lateinit var messages: List<Message>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build()
        )
        NotificationChannelHandler.createNotificationChannel(this)
        localDate = LocalData(this)
        showMessages()
        setListeners()
    }

    private fun setListeners() {
        btnLoadMore.setOnClickListener {
            loadExtra += EXTRA_PAGE_SIZE
            showMessages()
        }

        btnMarkCheckpoint.setOnClickListener {
            localDate.setLastSeqNo(messages[messages.size - 1].seqno)
            loadExtra = 0
            showMessages()
        }
    }

    private fun setButtonEnabled(enable: Boolean) {
        btnLoadMore.isEnabled = enable
        btnMarkCheckpoint.isEnabled = enable
    }

    private fun showMessages() {
        setButtonEnabled(false)
        tv.text = "Loading..."

        Thread(Runnable {
            try {
                val rpcClient = RpcClientFactory.getClient()

                runOnUiThread {
                    val lastSeq = localDate.getLastSeqNo()
                    var fromSeq = lastSeq - loadExtra
                    if (fromSeq < 0) fromSeq = 0

                    messages = rpcClient.getMessages(fromSeq)

                    var text = "Last Read: $lastSeq\n"

                    val lastCheck = localDate.getLastCheck()
                    if (lastCheck > 0)
                        text += "Last Background Sync: " + FORMAT.format(Date(lastCheck)) + "\n"

                    var lastTime = ""
                    var lastProject = ""
                    messages.forEach {
                        val time = FORMAT.format(Date(it.timestamp * 1000))
                        if (time != lastTime) {
                            text += '\n' + time + '\n'
                            lastTime = time
                        }

                        val project = it.project
                        if (project != lastProject) {
                            text += "\n" + project + '\n'
                            lastProject = project
                        }

                        text += "" + it.seqno + ". " + it.body + '\n'
                    }

                    tv.text = text

                    rpcClient.close()

                    setButtonEnabled(true)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    tv.text =
                        "An error occurred. Please go back and come again.\n\nOriginal error:\n" + e.message
                }
            }
        }).start()
    }
}

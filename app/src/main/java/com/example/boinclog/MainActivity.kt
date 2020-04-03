package com.example.boinclog

import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import com.example.boinclog.background.MessageCheckerStarter
import com.example.boinclog.boinc.Message
import com.example.boinclog.utils.BoincClient
import com.example.boinclog.utils.BoincClientStatusFormatter
import com.example.boinclog.utils.LocalData
import com.example.boinclog.utils.NotificationChannelHandler
import kotlinx.android.synthetic.main.activity_main.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

private const val EXTRA_PAGE_SIZE = 10
private val DATE_FORMATER = SimpleDateFormat("yyyy-MM-dd HH:mm")
private val FRACTION_NUMBER_FORMATTER =
    NumberFormat.getInstance().apply { minimumFractionDigits = 2; maximumFractionDigits = 2 }
private val INTEGER_NUMBER_FORMATTER =
    NumberFormat.getInstance().apply { maximumFractionDigits = 0 }

class MainActivity : AppCompatActivity() {
    var loadExtra = 0

    lateinit var localData: LocalData
    lateinit var messages: List<Message>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build()
        )
        NotificationChannelHandler.createNotificationChannel(this)
        localData = LocalData(this)
        showMessages()
        setListeners()
    }

    private fun setListeners() {
        btnLoadMore.setOnClickListener {
            loadExtra += EXTRA_PAGE_SIZE
            showMessages()
        }

        btnMarkCheckpoint.setOnClickListener {
            localData.setLastSeqNo(messages[messages.size - 1].seqno)
            loadExtra = 0
            showMessages()
            startService(Intent(this, MessageCheckerStarter::class.java))
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
                val lastSeq = localData.getLastSeqNo()
                var fromSeq = lastSeq - loadExtra
                if (fromSeq < 0) fromSeq = 0

                val boincClient = BoincClient()
                val status = boincClient.getClientStatus(fromSeq)
                messages = status.messages

                var text = BoincClientStatusFormatter.formatLastSync(lastSeq, localData)
                text += BoincClientStatusFormatter.formatProjects(status.projects)
                text += "<br/>"
                text += BoincClientStatusFormatter.formatMessages(messages)

                runOnUiThread {
                    tv.text = HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_MODE_COMPACT)
                    setButtonEnabled(true)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    tv.text =
                        HtmlCompat.fromHtml("An error occurred. Please go back and come again.<br/><br/>Original error:<br/>" + e.message, HtmlCompat.FROM_HTML_MODE_COMPACT)
                }
            }
        }).start()
    }
}

package com.example.boinclog

import android.content.SharedPreferences
import android.os.Bundle
import android.os.StrictMode
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.example.boinclog.boinc.Message
import com.example.boinclog.boinc.RpcClient
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*

private const val EXTRA_PAGE_SIZE = 10
private const val PREF_KEY_CHECKPOINT_SEQ_NO = "PREF_KEY_CHECKPOINT_SEQ_NO"
private val FORMAT = SimpleDateFormat("yyyy-MM-dd HH:mm")

class MainActivity : AppCompatActivity() {
    var loadExtra = EXTRA_PAGE_SIZE

    lateinit var sharedPreferences: SharedPreferences
    lateinit var messages: List<Message>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build()
        )
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        showMessages()
        setListeners()
    }

    private fun setListeners() {
        btnLoadMore.setOnClickListener {
            loadExtra += EXTRA_PAGE_SIZE
            showMessages()
        }

        btnMarkCheckpoint.setOnClickListener {
            val editor = sharedPreferences.edit()
            editor.putInt(PREF_KEY_CHECKPOINT_SEQ_NO, messages[messages.size - 1].seqno)
            editor.commit()

            loadExtra = EXTRA_PAGE_SIZE
            showMessages()
        }
    }

    private fun showMessages() {
        tv.text = "Loading..."

        try {
            Thread(Runnable {
                val rpcClient = RpcClient();
                rpcClient.open("67.205.153.68", 31416)
                rpcClient.authorize("Idon1tknowwhy1")

                runOnUiThread {
                    val lastSeq = sharedPreferences.getInt(PREF_KEY_CHECKPOINT_SEQ_NO, 0)
                    var fromSeq = lastSeq - loadExtra
                    if (fromSeq < 0) fromSeq = 0

                    messages = rpcClient.getMessages(fromSeq)

                    var text = "Last Read: $lastSeq\n"
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
                }
            }).start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}

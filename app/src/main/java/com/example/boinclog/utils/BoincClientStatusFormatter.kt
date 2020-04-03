package com.example.boinclog.utils

import com.example.boinclog.boinc.Message
import com.example.boinclog.boinc.Project
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

private val DATE_FORMATER = SimpleDateFormat("yyyy-MM-dd HH:mm")
private val FRACTION_NUMBER_FORMATTER =
    NumberFormat.getInstance().apply { minimumFractionDigits = 2; maximumFractionDigits = 2 }
private val INTEGER_NUMBER_FORMATTER =
    NumberFormat.getInstance().apply { maximumFractionDigits = 0 }

class BoincClientStatusFormatter {
    companion object {
        fun formatLastSync(lastSeq: Int, localData: LocalData): String {
            var text = "Last Read: $lastSeq<br/>"

            val lastCheck = localData.getLastCheck()
            if (lastCheck > 0)
                text += "Last Background Sync: " + DATE_FORMATER.format(Date(lastCheck)) + "<br/><br/>"

            return text
        }

        fun formatProjects(projects: Vector<Project>): String {
            var text = ""
            projects.sortedBy { -(it.sched_priority + (if (it.dont_request_more_work) -10000 else 10000)) }.forEach {
                val active = !it.dont_request_more_work

                text += "" + FRACTION_NUMBER_FORMATTER.format(it.sched_priority) + " " +
                        INTEGER_NUMBER_FORMATTER.format(it.resource_share) + " " +
                        (if (active) "A" else "!A") + " " +
                        formatProjectName(it.name, active) + " " +
                        DATE_FORMATER.format(Date(it.last_rpc_time.toLong() * 1000)) + "<br/>"
            }
            return text
        }

        private fun formatProjectName(name: String, active: Boolean) =
            if (active) "<b>$name</b>" else name

        fun formatMessages(messages: List<Message>): String {
            var text = ""
            var lastTime = ""
            var lastProject = ""
            messages.forEach {
                val time = DATE_FORMATER.format(Date(it.timestamp * 1000))

                if (time != lastTime) {
                    text += "<b><i>$time</i></b><br/>"
                    lastTime = time
                }

                val project = it.project
                if (project != lastProject) {
                    text += if (project.isNotEmpty()) "<b><u>$project</u></b><br/>" else ""
                    lastProject = project
                }

                text += "<b>" + it.seqno + ".</b> " + it.body + "<br/>"
            }
            return text
        }
    }
}
package com.example.boinclog.utils

import com.example.boinclog.boinc.Message
import com.example.boinclog.boinc.Project
import com.example.boinclog.boinc.Result
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

private val DATE_FORMATTER = SimpleDateFormat("yyyy-MM-dd HH:mm")
private val FRACTION_NUMBER_FORMATTER =
    NumberFormat.getInstance().apply { minimumFractionDigits = 2; maximumFractionDigits = 2 }
private val INTEGER_NUMBER_FORMATTER =
    NumberFormat.getInstance().apply { maximumFractionDigits = 0 }
private const val SECONDS_PER_MINUTE = 60
private const val SECONDS_PER_HOUR = SECONDS_PER_MINUTE * 60
private const val SECONDS_PER_DAY = SECONDS_PER_HOUR * 24

class BoincClientStatusFormatter {
    companion object {
        fun formatLastSync(lastSeq: Int, localData: LocalData): String {
            var text = "Last Read: $lastSeq<br/>"

            val lastCheck = localData.getLastCheck()
            if (lastCheck > 0)
                text += "Last Background Sync: " + formatRelativeTime(lastCheck) + " ago<br/><br/>"

            return text
        }

        fun formatProjects(projects: Vector<Project>, results: Vector<Result>): String {
            var text = ""
            var number = 0
            projects.sortedBy { -(it.sched_priority + (if (it.dont_request_more_work) -10000 else 10000)) }
                .forEach {
                    number++
                    val active = !it.dont_request_more_work
                    val last_rpc_time_millis = it.last_rpc_time.toLong() * 1000
                    val projectStatus = formatProjectStatus(it)

                    text += "" + number + ". " + FRACTION_NUMBER_FORMATTER.format(it.sched_priority) + " " +
                            INTEGER_NUMBER_FORMATTER.format(it.resource_share) + " " +
                            (if (active) "<b>" + it.name + "</b>" else it.name) + " " +
                            "(" + formatRelativeTime(last_rpc_time_millis) + ")<br/>" +
                            if (projectStatus.isNotEmpty()) "&nbsp;&nbsp;&nbsp;&nbsp;" + formatProjectStatus(it) + "<br/>" else ""


                    val taskText = formatResultsForProject(results, it)
                    if (taskText !== "") text += "$taskText<br/>"
                }
            return text
        }

        private fun formatProjectStatus(project: Project): String {
            var status = ""

            if (project.ended)
                status += "Ended. "
            if (project.detach_when_done)
                status += "Detach when done. "
            if (project.dont_request_more_work)
                status += "Don't request more work. "
            if (project.scheduler_rpc_in_progress)
                status += "Scheduler request in progress. "
            else if (project.sched_rpc_pending != 0)
                status += "Scheduler request pending. "

            return status
        }

        private fun formatResultsForProject(results: Vector<Result>, project: Project): String {
            val url = project.master_url
            val filteredResults = results.filter { it.project_url == url }
            return if (filteredResults.isNotEmpty()) formatResults(filteredResults) else ""
        }

        private fun formatResults(results: List<Result>): String {
            var text = ""
            var number = 0
            results.forEach {
                number++
                var resultState = formatResultState(it)
                val resultText = "&nbsp;&nbsp;&nbsp;&nbsp;$number. " +
                        "v" + formatRelativeTime(it.received_time * 1000) + " " +
                        formatDuration(it.current_cpu_time.toLong()) + ":" + formatDuration(it.estimated_cpu_time_remaining.toLong()) + " " +
                        "^" + formatRelativeTime(it.report_deadline * 1000) + " " +
                        (it.fraction_done * 100).toInt() + "% " +
                        if (it.active_task) "<b>$resultState</b>" else resultState
                text += "$resultText<br/>"
            }
            return text.removeSuffix("<br/>")
        }

        private fun formatRelativeTime(timeMillis: Long): String {
            val now = System.currentTimeMillis()
            var durationMillis = now - timeMillis
            if (durationMillis < 0) durationMillis *= -1
            return formatDuration(durationMillis / 1000)
        }

        private fun formatDuration(seconds: Long): String {
            val days = seconds / SECONDS_PER_DAY
            if (days > 0) return "" + days + "d"

            val hours = seconds / SECONDS_PER_HOUR
            if (hours > 0) return "" + hours + "h"

            val minutes = seconds / SECONDS_PER_MINUTE
            return if (minutes > 0) "" + minutes + "m"
            else "" + seconds + "s"
        }

        private fun formatResultState(result: Result): String {
            return when (result.state) {
                BoincResultState.RESULT_NEW.ordinal -> "Ready to download"
                BoincResultState.RESULT_FILES_DOWNLOADING.ordinal -> "Downloading"
                BoincResultState.RESULT_FILES_DOWNLOADED.ordinal -> when (result.active_task) {
                    false -> "Ready to " + (if (result.fraction_done > 0) "resume" else "start")
                    true -> when (result.scheduler_state) {
                        BoincSchedularState.CPU_SCHED_SCHEDULED.ordinal -> "Running"
                        BoincSchedularState.CPU_SCHED_PREEMPTED.ordinal -> "Waiting"
                        else -> ""
                    }
                }
                BoincResultState.RESULT_COMPUTE_ERROR.ordinal -> "Error"
                BoincResultState.RESULT_FILES_UPLOADING.ordinal -> "Uploading"
                BoincResultState.RESULT_FILES_UPLOADED.ordinal -> "Ready to report"
                BoincResultState.RESULT_ABORTED.ordinal -> "Aborted"
                BoincResultState.RESULT_UPLOAD_FAILED.ordinal -> "Upload failed"
                else -> ""
            }
        }

        fun formatMessages(messages: List<Message>): String {
            var text = ""
            var lastTime = ""
            var lastProject = "---"
            messages.forEach {
                val timeMillis = it.timestamp * 1000;
                val time = DATE_FORMATTER.format(Date(timeMillis))

                if (time != lastTime) {
                    text += "<b><i>$time (" + formatRelativeTime(timeMillis) + ")</i></b><br/>"
                    lastTime = time
                }

                val project = it.project
                if (project != lastProject) {
                    text += "<b><u>" + (if (project.isNotEmpty()) project else "Boinc Client") + "</u></b><br/>"
                    lastProject = project
                }

                text += "<b>" + it.seqno + ".</b> " + it.body + "<br/>"
            }
            return text
        }
    }
}
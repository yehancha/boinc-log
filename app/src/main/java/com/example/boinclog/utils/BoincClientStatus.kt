package com.example.boinclog.utils

import com.example.boinclog.boinc.Message
import com.example.boinclog.boinc.Project
import java.util.*

class BoincClientStatus {
    var messageCount: Int = 0
    var messages: Vector<Message> = Vector()
    var projects: Vector<Project> = Vector()
}
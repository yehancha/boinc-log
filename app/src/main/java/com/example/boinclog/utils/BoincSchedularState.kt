package com.example.boinclog.utils

enum class BoincSchedularState(val label: String) {
    CPU_SCHED_UNINITIALIZED("Uninit"),
    CPU_SCHED_PREEMPTED("Preempted"),
    CPU_SCHED_SCHEDULED("Scheduled"),
}
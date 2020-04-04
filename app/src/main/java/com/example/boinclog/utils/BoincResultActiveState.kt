package com.example.boinclog.utils

enum class BoincResultActiveState(val label: String) {
    PROCESS_UNINITIALIZED("Uninit"),
    PROCESS_EXECUTING("Excng"),
    PROCESS_EXITED("Ext"),
    PROCESS_WAS_SIGNALED("Sig"),
    PROCESS_EXIT_UNKNOWN("UnExt"),
    PROCESS_ABORT_PENDING("ToAbrt"),
    PROCESS_ABORTED("Abrt"),
    PROCESS_COULDNT_START("CldNtStart"),
    PROCESS_QUIT_PENDING("ToQt"),
    PROCESS_SUSPENDED("Susp"),
    PROCESS_COPY_PENDING("ToCp"),
}
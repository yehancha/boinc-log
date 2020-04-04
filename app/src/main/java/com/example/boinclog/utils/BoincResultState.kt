package com.example.boinclog.utils

enum class BoincResultState(val label: String) {
    RESULT_NEW("New"),
    RESULT_FILES_DOWNLOADING("Downg"),
    RESULT_FILES_DOWNLOADED("Down"),
    RESULT_COMPUTE_ERROR("Err"),
    RESULT_FILES_UPLOADING("Upng"),
    RESULT_FILES_UPLOADED("Up"),
    RESULT_ABORTED("Abrt"),
    RESULT_UPLOAD_FAILED("UpF"),
}
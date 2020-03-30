package com.example.boinclog.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

private const val PREF_KEY_CHECKPOINT_SEQ_NO = "PREF_KEY_CHECKPOINT_SEQ_NO"
private const val PREF_KEY_LAST_CHECK = "PREF_KEY_LAST_CHECK"

class LocalData(context: Context) {
    private var sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    fun getLastSeqNo() = sharedPreferences.getInt(PREF_KEY_CHECKPOINT_SEQ_NO, 0)

    fun setLastSeqNo(lastSeqNo: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt(PREF_KEY_CHECKPOINT_SEQ_NO, lastSeqNo)
        editor.apply()
    }

    fun getLastCheck() = sharedPreferences.getLong(PREF_KEY_LAST_CHECK, 0)

    fun setLastChecked(timeMillis: Long) {
        val editor = sharedPreferences.edit()
        editor.putLong(PREF_KEY_LAST_CHECK, timeMillis)
        editor.apply()
    }
}
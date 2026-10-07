package com.nitin3it.kidsafe.data

import android.content.Context
import androidx.core.content.edit
import com.nitin3it.kidsafe.data.model.Role

/** Remembers which mode (child or parent) this device was set up in. */
class RolePreferences(context: Context) {
    private val prefs = context.getSharedPreferences("kidsafe_prefs", Context.MODE_PRIVATE)

    var role: Role?
        get() = Role.from(prefs.getString(KEY_ROLE, null))
        set(value) = prefs.edit { putString(KEY_ROLE, value?.key) }

    private companion object {
        const val KEY_ROLE = "role"
    }
}

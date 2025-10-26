package mx.itesm.beneficiojuventud.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Helper class for managing branch-related SharedPreferences
 */
object BranchPreferences {
    private const val PREF_NAME = "branch_preferences"
    private const val KEY_SELECTED_BRANCH_ID = "selected_branch_id"
    private const val KEY_COLLABORATOR_ID = "collaborator_id"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Save the selected branch ID for the current collaborator
     * @param context Android context
     * @param collaboratorId The collaborator's Cognito ID
     * @param branchId The selected branch ID
     */
    fun saveSelectedBranch(context: Context, collaboratorId: String, branchId: Int) {
        getPreferences(context).edit().apply {
            putInt("${KEY_SELECTED_BRANCH_ID}_$collaboratorId", branchId)
            putString(KEY_COLLABORATOR_ID, collaboratorId)
            apply()
        }
    }

    /**
     * Get the selected branch ID for a specific collaborator
     * @param context Android context
     * @param collaboratorId The collaborator's Cognito ID
     * @return The selected branch ID, or null if not set
     */
    fun getSelectedBranch(context: Context, collaboratorId: String): Int? {
        val branchId = getPreferences(context).getInt("${KEY_SELECTED_BRANCH_ID}_$collaboratorId", -1)
        return if (branchId != -1) branchId else null
    }

    /**
     * Clear the selected branch for a specific collaborator
     * @param context Android context
     * @param collaboratorId The collaborator's Cognito ID
     */
    fun clearSelectedBranch(context: Context, collaboratorId: String) {
        getPreferences(context).edit().apply {
            remove("${KEY_SELECTED_BRANCH_ID}_$collaboratorId")
            apply()
        }
    }

    /**
     * Get the last collaborator ID that saved a selection
     * @param context Android context
     * @return The collaborator ID or null
     */
    fun getLastCollaboratorId(context: Context): String? {
        return getPreferences(context).getString(KEY_COLLABORATOR_ID, null)
    }

    /**
     * Clear all branch preferences
     * @param context Android context
     */
    fun clearAll(context: Context) {
        getPreferences(context).edit().clear().apply()
    }
}

package bruhcollective.itaysonlab.microapp.guard.widget

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import bruhcollective.itaysonlab.jetisteam.controllers.SteamSessionController
import bruhcollective.itaysonlab.microapp.guard.core.GuardController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class SteamGuardWidgetUpdateService: JobService() {

    @Inject
    lateinit var steamSessionController: SteamSessionController

    @Inject
    lateinit var guardController: GuardController

    override fun onStartJob(params: JobParameters?): Boolean {
        val scope = CoroutineScope(Dispatchers.IO) + SupervisorJob()
        scope.launch {
            val glanceId = getGlanceId(applicationContext)
            val instance = guardController.getInstance(steamSessionController.steamId())
            if (instance != null) {
                val code = instance.code.first()
                updatePreferences(applicationContext, glanceId) {
                    this[stringPreferencesKey(WIDGET_STATE)] = "succ"
                    this[stringPreferencesKey(STEAM_USER_NAME)] = instance.username
                    this[stringPreferencesKey(STEAM_GUARD_CODE)] = code.code
                    this[floatPreferencesKey(STEAM_GUARD_TIME_LEFT)] = code.progressRemaining
                }
            } else {
                updatePreferences(applicationContext, glanceId) {
                    this[stringPreferencesKey(WIDGET_STATE)] = "error"
                    this[stringPreferencesKey(STEAM_USER_NAME)] = ""
                    this[stringPreferencesKey(STEAM_GUARD_CODE)] = ""
                    this[floatPreferencesKey(STEAM_GUARD_TIME_LEFT)] = 0F
                }
            }
            withContext(Dispatchers.Main) {
                runBlocking {
                    SteamGuardWidget().updateAll(applicationContext)
                    jobFinished(params, false)
                }
            }
        }

        return true
    }

    private suspend fun getGlanceId(context: Context): GlanceId =
        GlanceAppWidgetManager(context).getGlanceIds(SteamGuardWidget::class.java).last()

    private suspend fun updatePreferences(
        context: Context,
        glanceId: GlanceId,
        update: MutablePreferences.() -> Unit
    ) = updateAppWidgetState(context, glanceId) { it.update() }

    override fun onStopJob(params: JobParameters?): Boolean {
        return false
    }

}
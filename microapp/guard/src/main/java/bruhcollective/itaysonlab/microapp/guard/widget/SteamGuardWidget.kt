package bruhcollective.itaysonlab.microapp.guard.widget
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.color.dynamicThemeColorProviders
import androidx.glance.currentState
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle

const val WIDGET_STATE = "widget_state"
const val STEAM_USER_NAME = "steam_user_name"
const val STEAM_GUARD_CODE = "steam_guard_code"
const val STEAM_GUARD_TIME_LEFT = "steam_guard_time_left"

class SteamGuardWidget : GlanceAppWidget() {
    override val stateDefinition = PreferencesGlanceStateDefinition

    @OptIn(ExperimentalUnitApi::class)
    @Composable
    override fun Content() {
        val prefs = currentState<Preferences>()
        when (prefs[stringPreferencesKey(WIDGET_STATE)]) {
            null, "" -> {
                scheduleUpdate()
                Box(modifier = GlanceModifier.background(Color.White).fillMaxSize()) {
                    Text(text = "Loading!")
                }
            }
            "error" -> {
                scheduleUpdate(delay = 5_000)
                Box(modifier = GlanceModifier.background(Color.White).fillMaxSize()) {
                    Text(text = "Error!")
                }
            }
            "succ" -> {
                scheduleUpdate(delay = 5_000)
                Column(
                    modifier = GlanceModifier
                        .background(dynamicThemeColorProviders().background)
                        .fillMaxSize()
                ) {
                    val username = prefs[stringPreferencesKey(STEAM_USER_NAME)]
                    val code = prefs[stringPreferencesKey(STEAM_GUARD_CODE)]
                    val progressLeft = prefs[floatPreferencesKey(STEAM_GUARD_TIME_LEFT)] ?: 0F

                    Text(
                        text = "$username",
                        modifier = GlanceModifier.padding(top = 24.dp).fillMaxWidth(),
                        style = TextStyle(
                            textAlign = TextAlign.Center,
                            fontSize = TextUnit(14F, TextUnitType.Sp),
                            color = dynamicThemeColorProviders().primary
                        )
                    )
                    Text(
                        text = "$code",
                        modifier = GlanceModifier.padding(top = 32.dp).fillMaxWidth(),
                        style = TextStyle(
                            textAlign = TextAlign.Center,
                            fontSize = TextUnit(28F, TextUnitType.Sp),
                            color = dynamicThemeColorProviders().secondary
                        )
                    )
                    Row(modifier = GlanceModifier.fillMaxWidth().padding(top = 24.dp)) {
                        val width = LocalSize.current.width
                        Spacer(GlanceModifier.defaultWeight())
                        Box(
                            modifier = GlanceModifier
                                .width(width * progressLeft)
                                .height(4.dp)
                                .cornerRadius(20.dp)
                                .background(Color.White)
                        ) {}
                        Spacer(GlanceModifier.defaultWeight())
                    }
                }
            }
        }
    }

    @Composable
    private fun scheduleUpdate(delay: Long = 0) {
        val context = LocalContext.current
        val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        val componentName = ComponentName(context, SteamGuardWidgetUpdateService::class.java)
        val jobInfo = JobInfo.Builder(123, componentName)
        val job = jobInfo.setMinimumLatency(delay).setOverrideDeadline(5_000).build()

        jobScheduler.schedule(job)
    }
}

class SteamGuardWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SteamGuardWidget()
}
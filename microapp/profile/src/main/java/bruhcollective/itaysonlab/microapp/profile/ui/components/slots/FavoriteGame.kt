package bruhcollective.itaysonlab.microapp.profile.ui.components.slots

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bruhcollective.itaysonlab.jetisteam.mappers.ProfileCustomizationEntry
import bruhcollective.itaysonlab.jetisteam.util.CdnUrlUtil
import coil.compose.AsyncImage
import steam.player.CPlayer_GetAchievementsProgress_Response_AchievementProgress
import steam.player.CPlayer_GetOwnedGames_Response_Game

@Composable
internal fun FavoriteGame(
    entry: ProfileCustomizationEntry,
    ownedGames: Map<Int, CPlayer_GetOwnedGames_Response_Game>,
    achievementsProgress: Map<Int, CPlayer_GetAchievementsProgress_Response_AchievementProgress>,
) {
    val game = remember(entry.slots) {
        val fg = entry.slots.first().appId
        ownedGames[fg]!! to achievementsProgress[fg]!!
    }

    Column(Modifier.padding(16.dp)) {
        AsyncImage(
            model = remember(game.first.appid) {
                CdnUrlUtil.buildAppUrl(game.first.appid, "capsule_467x181.jpg")
            },
            contentDescription = null,
            modifier = Modifier.height(96.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = game.first.name, color = Color.White, fontSize = 21.sp)
        Text(text = remember (game.first.playtimeForever) {
            "${game.first.playtimeForever / 60} hours played"
        }, color = Color.White.copy(alpha = 0.7f), fontSize = 16.sp)
    }
}
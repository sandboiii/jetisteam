package bruhcollective.itaysonlab.jetisteam.usecases

import bruhcollective.itaysonlab.jetisteam.controllers.CdnController
import bruhcollective.itaysonlab.jetisteam.models.GameFullDetailsData
import bruhcollective.itaysonlab.jetisteam.models.Language
import bruhcollective.itaysonlab.jetisteam.models.Reviews
import bruhcollective.itaysonlab.jetisteam.models.SteamDeckSupportReport
import bruhcollective.itaysonlab.jetisteam.repository.GameRepository
import bruhcollective.itaysonlab.jetisteam.repository.StoreRepository
import io.github.furstenheim.CopyDown
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import steam.common.StoreBrowseItemDataRequest
import steam.common.StoreItemID
import javax.inject.Inject

class GetGamePage @Inject constructor(
    private val gameRepository: GameRepository,
    private val storeRepository: StoreRepository,
    private val cdnController: CdnController
) {
    suspend operator fun invoke(appId: Int): GamePage {
        val strId = appId.toString()

        val details = gameRepository.getGameDetails(strId, false)
        val deckCompat = gameRepository.getDeckCompat(strId, false)
        val reviews = gameRepository.getReviewsPreview(strId, false)

        val libraryHeroUrl = cdnController.buildAppUrl(appId, "library_hero.jpg")
        val logoUrl = cdnController.buildAppUrl(appId, "logo.png")
        val fallbackUrl = cdnController.buildAppUrl(appId, "portrait.png")

        val readyBgUrl = if (cdnController.exists(libraryHeroUrl)) {
            libraryHeroUrl to false
        } else {
            fallbackUrl to true
        }

        val readyLogoUrl = if (cdnController.exists(logoUrl)) {
            logoUrl to false
        } else {
            "" to true
        }

        val storeItem = storeRepository.getItems(
            ids = listOf(StoreItemID(appid = appId)),
            dataRequest = StoreBrowseItemDataRequest(
                include_tag_count = 5,
                include_assets = true,
                include_platforms = false,
                include_basic_info = false,
                include_ratings = false,
                include_all_purchase_options = false,
                include_reviews = false,
                include_trailers = false,
                include_supported_languages = true,
                include_screenshots = false,
                include_release = false,
            )
        ).store_items.first()

        val tags = storeRepository.getLocalizedTags(storeItem.tagids)
            .let { locale ->
                storeItem.tags.sortedByDescending { it.weight }
                    .map { locale[it.tagid]!! to it.tagid!! }
            }

        val langMatrix = storeItem.supported_languages.mapNotNull {
            LanguageMatrixEntry(
                language = Language.elanguageMap[it.elanguage] ?: return@mapNotNull null,
                ui = it.supported ?: false,
                fullAudio = it.full_audio ?: false,
                subtitles = it.subtitles ?: false,
            )
        }

        return GamePage(
            fullDetails = details,
            languageMatrix = langMatrix,
            headerBackgroundUrl = readyBgUrl.first,
            headerBackgroundAutogenerated = readyBgUrl.second,
            logoUrl = readyLogoUrl.first,
            logoUrlAbsent = readyLogoUrl.second,
            tags = tags,
            deckSupportReport = deckCompat,
            reviews = reviews,
            fullDescription = withContext(Dispatchers.Default) { CopyDown().convert(details.fullDescription) }
        )
    }

    class GamePage(
        val fullDetails: GameFullDetailsData,
        val languageMatrix: List<LanguageMatrixEntry>,
        val headerBackgroundUrl: String,
        val headerBackgroundAutogenerated: Boolean,
        val logoUrl: String,
        val logoUrlAbsent: Boolean,
        val tags: List<Pair<String, Int>>,
        val deckSupportReport: SteamDeckSupportReport,
        val reviews: Reviews,
        val fullDescription: String
    )

    class LanguageMatrixEntry(
        val language: Language,
        val ui: Boolean,
        val fullAudio: Boolean,
        val subtitles: Boolean,
    )
}
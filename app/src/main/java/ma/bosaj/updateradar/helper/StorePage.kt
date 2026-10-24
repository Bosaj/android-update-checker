package ma.bosaj.updateradar.helper

import java.text.Normalizer
import java.util.Locale

/** One node of a store screen as the accessibility service sees it. Plain data, so the rules are unit-tested. */
data class PageNode(val text: String?, val viewId: String? = null)

/** What a store screen shows right now. */
enum class PageState {
    /** Still loading, or nothing recognisable yet. */
    LOADING,

    /** Play Store's Updates overview with its own "Update all" button. */
    UPDATE_ALL,

    /** An app page whose store offers a newer version. */
    UPDATE,

    /** An app page showing Open / Installed: the installed version is the latest. */
    CURRENT,
}

/**
 * Deterministic rules that read a Play Store or Galaxy Store screen. They only look for the store's own
 * buttons, by their exact label in the languages Update Radar supports, or by Galaxy Store's view id.
 * Nothing else on the screen is used.
 */
object StorePage {
    const val PLAY = "com.android.vending"
    const val GALAXY = "com.sec.android.app.samsungapps"

    /** Galaxy Store's main button on an app page: "Update", "Installed" or "Open". */
    const val GALAXY_MAIN_BUTTON = "$GALAXY:id/tv_detail_install_reduce_price"

    private val UPDATE_ALL = labels(
        "Update all", "Tout mettre à jour", "تحديث الكل", "Actualizar todo", "Alle aktualisieren",
        "Aggiorna tutto", "Atualizar tudo", "Tümünü güncelle",
    )
    private val UPDATE = labels(
        "Update", "Mettre à jour", "تحديث", "Actualizar", "Aktualisieren", "Aggiorna", "Atualizar", "Güncelle",
    )

    /** Shown instead of Update when the installed version is the latest. */
    private val CURRENT = labels(
        "Open", "Ouvrir", "فتح", "Abrir", "Öffnen", "Apri", "Aç",
        "Play", "Jouer", "تشغيل", "Jugar", "Spielen", "Gioca", "Jogar", "Oyna",
        "Installed", "Installé", "Installée", "مثبت", "Instalado", "Instalada", "Installiert", "Installata", "Yüklü",
    )

    fun classify(nodes: List<PageNode>, wantUpdateAll: Boolean): PageState {
        val texts = nodes.mapNotNull { it.text?.let(::normalize) }.toSet()
        if (wantUpdateAll) return if (texts.any { it in UPDATE_ALL }) PageState.UPDATE_ALL else PageState.LOADING
        nodes.firstOrNull { it.viewId == GALAXY_MAIN_BUTTON }?.text?.let(::normalize)?.let { main ->
            return if (main in UPDATE) PageState.UPDATE else PageState.CURRENT
        }
        return when {
            texts.any { it in UPDATE } -> PageState.UPDATE
            texts.any { it in CURRENT } -> PageState.CURRENT
            else -> PageState.LOADING
        }
    }

    /** Index in [nodes] of the button to press for [state], or -1. */
    fun target(nodes: List<PageNode>, state: PageState): Int = when (state) {
        PageState.UPDATE_ALL -> nodes.indexOfFirst { n -> n.text?.let(::normalize) in UPDATE_ALL }
        PageState.UPDATE -> nodes.indexOfFirst { it.viewId == GALAXY_MAIN_BUTTON && it.text?.let(::normalize) in UPDATE }
            .takeIf { it >= 0 } ?: nodes.indexOfFirst { n -> n.text?.let(::normalize) in UPDATE }
        else -> -1
    }

    private fun labels(vararg l: String) = l.map(::normalize).toSet()

    /** Case-, accent- and Arabic-diacritic-insensitive form, so "مثبّت" matches "مثبت" and "INSTALLÉ" matches "installé". */
    fun normalize(s: String): String {
        val trimmed = s.trim().lowercase(Locale.ROOT)
        val noArabicMarks = trimmed.replace(Regex("[\\u064B-\\u065F\\u0670\\u0640]"), "")
        return Normalizer.normalize(noArabicMarks, Normalizer.Form.NFD).replace(Regex("\\p{Mn}+"), "")
    }
}

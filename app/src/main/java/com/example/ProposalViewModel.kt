package com.example

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class ProposalStep {
    QUESTION,
    CELEBRATION,
    RECIPROCATED
}

data class ProposalUiState(
    val step: ProposalStep = ProposalStep.QUESTION,
    val partnerName: String = "রিদি",
    val nickname: String = "jaan",
    val dodgeCount: Int = 0,
    val currentDodgeMessage: String? = null,
    val noButtonOffsetX: Float = 0f,
    val noButtonOffsetY: Float = 0f,
    val yesButtonScale: Float = 1f,
    val confettiTrigger: Long = 0L,
    val isSoundEnabled: Boolean = true,
    val showCustomizeDialog: Boolean = false
)

class ProposalViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ProposalUiState())
    val uiState: StateFlow<ProposalUiState> = _uiState.asStateFlow()

    private val dodgeMessages = listOf(
        "না বলা যাবে না! 😜",
        "আরে ধরা যায় না তো! 🏃‍♂️💨",
        "ভুল বাটনে চাপ দিচ্ছ কেন? 🙈",
        "একটু ভেবে দেখো প্লিজ! 🥺",
        "তোমার আঙুল শুধু Yes-এই যাবে! ❤️",
        "উহুহু! আবার মিস হয়ে গেলো! 🤭",
        "না বলা একেবারেই নিষেধ! 🚫🥰",
        "আমার মন ভাঙতে দেবো না! 💔❌",
        "নো বাটন আকাশে উড়ে গেছে! 🚀",
        "তুমি রাজি হয়ে যাও না সোনা! 🥺💍"
    )

    fun onNoButtonDodged(screenWidthPx: Float, screenHeightPx: Float, density: Float) {
        val nextCount = _uiState.value.dodgeCount + 1
        val message = dodgeMessages[(nextCount - 1) % dodgeMessages.size]

        // Calculate a bounding area inside screen so button stays clearly visible and touchable
        val padding = 70f * density
        val maxRangeX = ((screenWidthPx / 2f) - padding).coerceAtLeast(80f * density)
        val maxRangeY = ((screenHeightPx / 4f) - padding).coerceAtLeast(100f * density)

        // Generate random offset
        val randX = Random.nextDouble(-maxRangeX.toDouble(), maxRangeX.toDouble()).toFloat()
        val randY = Random.nextDouble(-maxRangeY.toDouble(), maxRangeY.toDouble()).toFloat()

        // Slowly grow the Yes button up to 1.35x to make it more prominent and humorous
        val newYesScale = (1f + (nextCount * 0.04f)).coerceAtMost(1.35f)

        _uiState.update {
            it.copy(
                dodgeCount = nextCount,
                currentDodgeMessage = message,
                noButtonOffsetX = randX,
                noButtonOffsetY = randY,
                yesButtonScale = newYesScale
            )
        }
        SoundManager.playDodgeSound()
    }

    fun onYesClicked() {
        _uiState.update {
            it.copy(
                step = ProposalStep.CELEBRATION,
                confettiTrigger = System.currentTimeMillis()
            )
        }
        SoundManager.playCelebrationSound()
    }

    fun onReciprocateClicked() {
        _uiState.update {
            it.copy(
                step = ProposalStep.RECIPROCATED,
                confettiTrigger = System.currentTimeMillis()
            )
        }
        SoundManager.playCelebrationSound()
    }

    fun onReset() {
        _uiState.update {
            it.copy(
                step = ProposalStep.QUESTION,
                dodgeCount = 0,
                currentDodgeMessage = null,
                noButtonOffsetX = 0f,
                noButtonOffsetY = 0f,
                yesButtonScale = 1f
            )
        }
    }

    fun toggleSound() {
        val newState = !_uiState.value.isSoundEnabled
        SoundManager.isSoundEnabled = newState
        _uiState.update { it.copy(isSoundEnabled = newState) }
    }

    fun openCustomizeDialog() {
        _uiState.update { it.copy(showCustomizeDialog = true) }
    }

    fun closeCustomizeDialog() {
        _uiState.update { it.copy(showCustomizeDialog = false) }
    }

    fun saveCustomNames(newName: String, newNickname: String) {
        _uiState.update {
            it.copy(
                partnerName = newName.trim().ifEmpty { "রিদি" },
                nickname = newNickname.trim().ifEmpty { "jaan" },
                showCustomizeDialog = false
            )
        }
    }
}

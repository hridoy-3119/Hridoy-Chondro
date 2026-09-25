package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Special Question", appName)
    }

    @Test
    fun `test proposal view model flow`() {
        val viewModel = ProposalViewModel()
        assertEquals(ProposalStep.QUESTION, viewModel.uiState.value.step)
        assertEquals("রিদি", viewModel.uiState.value.partnerName)

        // Test dodging No button
        viewModel.onNoButtonDodged(1080f, 2400f, 2.75f)
        assertEquals(1, viewModel.uiState.value.dodgeCount)
        assertNotNull(viewModel.uiState.value.currentDodgeMessage)
        assertTrue(viewModel.uiState.value.yesButtonScale > 1f)

        // Test clicking Yes
        viewModel.onYesClicked()
        assertEquals(ProposalStep.CELEBRATION, viewModel.uiState.value.step)
        assertTrue(viewModel.uiState.value.confettiTrigger > 0L)

        // Test reciprocation
        viewModel.onReciprocateClicked()
        assertEquals(ProposalStep.RECIPROCATED, viewModel.uiState.value.step)

        // Test reset
        viewModel.onReset()
        assertEquals(ProposalStep.QUESTION, viewModel.uiState.value.step)
        assertEquals(0, viewModel.uiState.value.dodgeCount)
    }
}

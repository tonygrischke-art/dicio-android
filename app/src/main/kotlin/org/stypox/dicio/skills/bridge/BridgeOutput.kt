package org.stypox.dicio.skills.bridge

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.SkillOutput
import org.stypox.dicio.R
import org.stypox.dicio.error.UserAction
import org.stypox.dicio.io.graphical.Headline
import org.stypox.dicio.io.graphical.HeadlineSpeechSkillOutput
import org.stypox.dicio.io.graphical.ReportButton
import org.stypox.dicio.util.getString

sealed interface BridgeOutput : SkillOutput {
    data class Success(
        private val tool: String,
        private val result: String
    ) : BridgeOutput, HeadlineSpeechSkillOutput {
        override fun getSpeechOutput(ctx: SkillContext): String {
            return if (result.isNotBlank()) {
                result.take(200)
            } else {
                ctx.getString(R.string.skill_bridge_success, tool)
            }
        }

        @Composable
        override fun GraphicalOutput(ctx: SkillContext) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Headline(text = ctx.getString(R.string.skill_bridge_success, tool))
                Headline(text = result.take(500))
            }
        }
    }

    data class Error(
        private val tool: String,
        private val throwable: Throwable?
    ) : BridgeOutput {
        override fun getSpeechOutput(ctx: SkillContext): String =
            ctx.getString(R.string.skill_bridge_error, tool)

        @Composable
        override fun GraphicalOutput(ctx: SkillContext) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Headline(text = getSpeechOutput(ctx))
                if (throwable != null) {
                    ReportButton(throwable, UserAction.BRIDGE)
                }
            }
        }
    }
}
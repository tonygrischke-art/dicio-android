package org.stypox.dicio.skills.bridge

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.Skill
import org.dicio.skill.skill.SkillInfo
import org.stypox.dicio.R
import org.stypox.dicio.sentences.Sentences

object BridgeInfo : SkillInfo("bridge") {
    override fun name(context: Context) =
        context.getString(R.string.skill_name_bridge)

    override fun sentenceExample(context: Context) =
        context.getString(R.string.skill_sentence_example_bridge)

    @Composable
    override fun icon() =
        rememberVectorPainter(Icons.Default.Settings)

    override fun build(ctx: SkillContext): Skill<*>? {
        val data = Sentences.Bridge[ctx.sentencesLanguage] ?: return null
        return BridgeSkill(BridgeInfo, data)
    }
}
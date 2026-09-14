package com.aven.app.core.intent

/**
 * IntentEngine defines the taxonomy and rules for the intervention checkpoint.
 * Neutral by design: no option is ever marked 'correct' or 'incorrect'.
 */
object IntentEngine {

    data class IntentOption(
        val key: String,
        val label: String,
        val isIntentional: Boolean,
        val categoryDescription: String
    )

    val allOptions: List<IntentOption> = listOf(
        IntentOption(
            key = "check_specific",
            label = "Check something specific",
            isIntentional = true,
            categoryDescription = "Specific task"
        ),
        IntentOption(
            key = "reply",
            label = "Reply to someone",
            isIntentional = true,
            categoryDescription = "Direct communication"
        ),
        IntentOption(
            key = "post",
            label = "Post something",
            isIntentional = true,
            categoryDescription = "Creation / sharing"
        ),
        IntentOption(
            key = "search",
            label = "Look for something",
            isIntentional = true,
            categoryDescription = "Focused research"
        ),
        IntentOption(
            key = "bored",
            label = "I'm bored",
            isIntentional = false,
            categoryDescription = "Uncertain / passive impulse"
        ),
        IntentOption(
            key = "dont_know",
            label = "I don't know",
            isIntentional = false,
            categoryDescription = "Automatic reflex"
        ),
        IntentOption(
            key = "other",
            label = "Other",
            isIntentional = false,
            categoryDescription = "Unspecified intention"
        )
    )

    fun isIntentional(intentType: String): Boolean {
        return allOptions.find { it.label.equals(intentType, ignoreCase = true) || it.key == intentType }
            ?.isIntentional ?: false
    }

    fun getOption(keyOrLabel: String): IntentOption? {
        return allOptions.find { it.key == keyOrLabel || it.label.equals(keyOrLabel, ignoreCase = true) }
    }

    fun findByLabel(label: String): IntentOption? = getOption(label)
}

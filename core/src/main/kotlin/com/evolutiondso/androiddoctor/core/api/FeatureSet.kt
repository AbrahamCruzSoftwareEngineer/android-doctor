package com.evolutiondso.androiddoctor.core.api

/**
 * Neutral feature descriptor intended for public/free + future private extensions.
 */
interface FeatureSet {
    val name: String
    val supportsHtml: Boolean
    val supportsMarkdown: Boolean
}

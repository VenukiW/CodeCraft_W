package com.example.codecraft

import kotlinx.serialization.Serializable

@Serializable
data class CodeSyntaxRules(
    val language: String = "kotlin", // "python", "java", "kotlin"
    val keywords: List<String> = emptyList(),
    val comments: List<String> = emptyList()
)



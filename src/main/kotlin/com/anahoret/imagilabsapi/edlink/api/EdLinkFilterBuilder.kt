package com.anahoret.imagilabsapi.edlink.api

class EdLinkFilterBuilder {
    private val filters = mutableMapOf<String, List<FilterCondition>>()

    fun field(fieldName: String, block: FieldFilterBuilder.() -> Unit): EdLinkFilterBuilder = apply {
        val builder = FieldFilterBuilder()
        builder.block()
        filters[fieldName] = builder.build()
    }

    fun build(): String {
        return filters.entries.joinToString(
            prefix = "{",
            postfix = "}"
        ) { (field, conditions) ->
            val conditionsJson = conditions.joinToString(
                prefix = "[",
                postfix = "]"
            ) { condition ->
                """{"operator":"${escapeJson(condition.operator)}","value":"${escapeJson(condition.value)}"}"""
            }

            """"${escapeJson(field)}":$conditionsJson"""
        }
    }

    private fun escapeJson(str: String): String =
        str
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")


    class FieldFilterBuilder {
        private val conditions = mutableListOf<FilterCondition>()

        fun inOperator(value: String): FieldFilterBuilder = apply {
            conditions.add(FilterCondition("in", value))
        }

        fun inOperator(values: Collection<Any>): FieldFilterBuilder = apply {
            conditions.add(FilterCondition("in", values.joinToString(",")))
        }

        fun equalsOperator(value: String): FieldFilterBuilder = apply {
            conditions.add(FilterCondition("eq", value))
        }

        fun equalsOperator(value: Any): FieldFilterBuilder = apply {
            conditions.add(FilterCondition("eq", value.toString()))
        }

        internal fun build(): List<FilterCondition> = conditions
    }

    internal data class FilterCondition(
        val operator: String,
        val value: String
    )
}

fun edLinkFilter(block: EdLinkFilterBuilder.() -> Unit): String {
    val builder = EdLinkFilterBuilder()
    builder.block()
    return builder.build()
}

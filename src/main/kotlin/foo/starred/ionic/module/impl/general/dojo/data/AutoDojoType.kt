package foo.starred.ionic.module.impl.general.dojo.data

import foo.starred.kontext.api.format.impl.string.StringFormatter

enum class AutoDojoType {
    CONTROL,
    DISCIPLINE,
    FORCE,
    MASTERY;

    val display: String = StringFormatter.titleCased(name)
    val message: String = "Test of $display OBJECTIVES"

    companion object {
        fun find(string: String): AutoDojoType? {
            return entries.find { it.message == string }
        }
    }
}

package org.treeWare.mockk

import io.mockk.Matcher
import io.mockk.MockKMatcherScope
import org.treeWare.model.core.FieldModel
import org.treeWare.model.core.getFieldName

fun <E : FieldModel> MockKMatcherScope.fieldsWithNames(vararg names: String) =
    match(FieldsWithNamesMatcher<E>(names.toList()))

class FieldsWithNamesMatcher<T : FieldModel>(
    private val expectedNames: List<String>
) : Matcher<List<T>> {
    override fun match(arg: List<T>?): Boolean =
        if (arg == null) false
        else arg.map { getFieldName(it) } == expectedNames.toList()

    override fun toString(): String = "fieldsWithNames(${expectedNames.joinToString()})"
}
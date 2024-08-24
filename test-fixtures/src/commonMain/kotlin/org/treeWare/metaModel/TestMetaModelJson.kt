package org.treeWare.metaModel

fun newTestMetaModelJson(rootJson: String?, vararg packageJsonList: String): String = """
    | {
    |   $testMetaModelCommonVersionJson,
    |   ${rootJson ?: ""}
    |   ${if (rootJson == null) "" else ","}
    |   "packages": [
    |     ${packageJsonList.joinToString(",\n")}
    |   ]
    | }
""".trimMargin()

fun getMultiplicityJson(multiplicity: String?): String =
    if (multiplicity == null) ""
    else """, "multiplicity": "$multiplicity""""

val testMetaModelCommonVersionJson = """
    "version": {
      "semantic": "1.0.0",
      "name": "pacific-ocean"
    }
""".trimIndent()

val testMetaModelCommonRootJson = """
    | "root": {
    |   "name": "root",
    |   "type": "composition",
    |   "composition": {
    |     "entity": "entity1",
    |     "package": "test.common"
    |   }
    | }
""".trimMargin()

val testMetaModelCommonPackageJson = """
    | {
    |   "name": "test.common",
    |   "entities": [
    |     {
    |       "name": "entity1",
    |       "fields": [
    |         {
    |           "name": "string_field",
    |           "number": 1,
    |           "type": "string"
    |         },
    |         {
    |           "name": "entity1_composition_field",
    |           "number": 2,
    |           "type": "composition",
    |           "composition": {
    |             "entity": "entity2",
    |             "package": "test.common"
    |           }
    |         }
    |       ]
    |     },
    |     {
    |       "name": "entity2",
    |       "fields": [
    |         {
    |           "name": "boolean_field",
    |           "number": 1,
    |           "type": "boolean",
    |           "is_key": true
    |         },
    |         {
    |           "name": "entity2_composition_field",
    |           "number": 2,
    |           "type": "composition",
    |           "composition": {
    |             "entity": "entity3",
    |             "package": "test.common"
    |           }
    |         }
    |       ]
    |     },
    |     {
    |       "name": "entity3",
    |       "fields": [
    |         {
    |           "name": "int_field",
    |           "number": 1,
    |           "type": "int32",
    |           "is_key": true
    |         }
    |       ]
    |     }
    |   ],
    |   "enumerations": [
    |     {
    |       "name": "enumeration1",
    |       "values": [
    |         {
    |           "name": "value1",
    |           "number": 0
    |         },
    |         {
    |           "name": "value2",
    |           "number": 1
    |         },
    |         {
    |           "name": "value3",
    |           "number": 2
    |         }
    |       ]
    |     }
    |   ]
    | }
""".trimMargin()
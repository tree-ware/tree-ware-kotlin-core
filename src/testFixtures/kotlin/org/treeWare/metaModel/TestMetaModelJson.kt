package org.treeWare.metaModel

fun newTestMetaModelJson(rootJson: String?, vararg packageJsonList: String): String = """
    | {
    |   "data": {
    |     "meta_model": {
    |       ${rootJson ?: ""}
    |       ${if (rootJson == null) "" else ","}
    |       "packages": [
    |         ${packageJsonList.joinToString(",\n")}
    |       ]
    |     }
    |   }
    | }
""".trimMargin()

val testMetaModelCommonRootJson = """
    | "root": {
    |   "name": "root",
    |   "entity": "entity1",
    |   "package": "test.common"
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
    |           "type": "string"
    |         },
    |         {
    |           "name": "entity1_composition_field",
    |           "type": "entity",
    |           "entity": {
    |             "name": "entity2",
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
    |           "type": "boolean",
    |           "is_key": true
    |         },
    |         {
    |           "name": "entity2_composition_field",
    |           "type": "entity",
    |           "entity": {
    |             "name": "entity3",
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
    |           "type": "int",
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
    |           "name": "value1"
    |         },
    |         {
    |           "name": "value2"
    |         },
    |         {
    |           "name": "value3"
    |         }
    |       ]
    |     }
    |   ]
    | }
""".trimMargin()
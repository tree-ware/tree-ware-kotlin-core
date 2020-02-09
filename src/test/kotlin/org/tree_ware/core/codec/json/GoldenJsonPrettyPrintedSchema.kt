package org.tree_ware.core.codec.json

const val goldenJsonPrettyPrintedSchema = """{
  "schema": {
    "packages": [
      {
        "name": "package_a",
        "entities": [
          {
            "name": "entity1",
            "fields": [
              {
                "name": "primitive_string_field",
                "type": "string"
              }
            ]
          },
          {
            "name": "entity2",
            "fields": [
              {
                "name": "package_a_entity1_field",
                "type": {
                  "package": "package_a",
                  "entity": "entity1"
                }
              },
              {
                "name": "package_b_string_alias1_field",
                "type": {
                  "package": "package_b",
                  "alias": "string_alias1"
                }
              },
              {
                "name": "package_a_entity3_field",
                "type": {
                  "package": "package_a",
                  "entity": "entity3"
                }
              },
              {
                "name": "package_b_entity1_field",
                "type": {
                  "package": "package_b",
                  "entity": "entity1"
                }
              }
            ]
          },
          {
            "name": "entity3"
          }
        ]
      },
      {
        "name": "package_b",
        "aliases": [
          {
            "name": "string_alias1",
            "type": "string"
          }
        ],
        "entities": [
          {
            "name": "entity1",
            "fields": [
              {
                "name": "primitive_boolean_field",
                "type": "boolean"
              }
            ]
          }
        ]
      }
    ]
  }
}"""

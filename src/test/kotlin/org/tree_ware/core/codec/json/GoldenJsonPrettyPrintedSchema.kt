package org.tree_ware.core.codec.json

const val goldenJsonPrettyPrintedSchema = """{
  "schema": {
    "packages": [
      {
        "name": "package.a",
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
                "name": "package_a_entity1_association_field",
                "type": {
                  "entity_path": [
                    "entity1"
                  ]
                }
              },
              {
                "name": "package_b_string_alias1_field",
                "type": {
                  "package": "package.b",
                  "alias": "string_alias1"
                }
              },
              {
                "name": "package_b_enumeration1_field",
                "type": {
                  "package": "package.b",
                  "enumeration": "enumeration1"
                }
              },
              {
                "name": "package_a_entity3_association_field",
                "type": {
                  "entity_path": [
                    "entity3"
                  ]
                }
              },
              {
                "name": "package_b_entity1_composition_field",
                "type": {
                  "package": "package.b",
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
        "name": "package.b",
        "aliases": [
          {
            "name": "string_alias1",
            "type": "string"
          }
        ],
        "enumerations": [
          {
            "name": "enumeration1",
            "values": [
              {
                "name": "value1"
              },
              {
                "name": "value2"
              },
              {
                "name": "value3"
              }
            ]
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

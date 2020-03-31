package org.tree_ware.core.codec.json

const val goldenJsonPrettyPrintedSchema = """{
  "schema": {
    "packages": [
      {
        "name": "package.a",
        "root": {
          "name": "test",
          "type": {
            "package": "package.a",
            "entity": "entity1"
          }
        },
        "entities": [
          {
            "name": "entity1",
            "fields": [
              {
                "name": "primitive_string_field",
                "is_key": true,
                "type": "string"
              },
              {
                "name": "package_b_entity2_composition_field",
                "type": {
                  "package": "package.b",
                  "entity": "entity2"
                }
              }
            ]
          },
          {
            "name": "entity2",
            "fields": [
              {
                "name": "package_a_association_field",
                "type": {
                  "entity_path": [
                    "test",
                    "package_b_entity2_composition_field",
                    "package_c_entity3_composition_field"
                  ]
                }
              },
              {
                "name": "package_b_string_alias1_field",
                "is_key": true,
                "type": {
                  "package": "package.b",
                  "alias": "string_alias1"
                }
              },
              {
                "name": "package_b_enumeration1_field",
                "is_key": true,
                "type": {
                  "package": "package.b",
                  "enumeration": "enumeration1"
                }
              },
              {
                "name": "package_b_entity1_composition_field",
                "is_key": true,
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
                "is_key": true,
                "type": "boolean"
              }
            ]
          },
          {
            "name": "entity2",
            "fields": [
              {
                "name": "primitive_boolean_field",
                "is_key": true,
                "type": "boolean"
              },
              {
                "name": "package_c_entity3_composition_field",
                "type": {
                  "package": "package.c",
                  "entity": "entity3"
                }
              }
            ]
          }
        ]
      },
      {
        "name": "package.c",
        "entities": [
          {
            "name": "entity3",
            "fields": [
              {
                "name": "primitive_boolean_field",
                "is_key": true,
                "type": "boolean"
              }
            ]
          }
        ]
      }
    ]
  }
}"""

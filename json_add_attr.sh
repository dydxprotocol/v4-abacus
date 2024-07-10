#!/bin/bash

# Check if the correct number of arguments is provided
if [ "$#" -ne 4 ]; then
    echo "Usage: $0 <json_file> <json_path> <attribute_name> <attribute_value>"
    exit 1
fi

# Assign arguments to variables
json_file=$1
json_path=$2
attribute_name=$3
attribute_value=$4

# Check if the JSON file exists
if [ ! -f "$json_file" ]; then
    echo "File $json_file does not exist."
    exit 1
fi

# Add the new attribute to the specified location in the JSON file using jq
jq --arg path "$json_path" --arg name "$attribute_name" --arg value "$attribute_value" '
  getpath($path | split(".") | map(select(. != ""))) as $obj |
  setpath(($path | split(".") | map(select(. != ""))) + [$name]; $value)
' "$json_file" > tmp.$$.json && mv tmp.$$.json "$json_file"

echo "Attribute $attribute_name with value $attribute_value has been added to $json_file at path $json_path."

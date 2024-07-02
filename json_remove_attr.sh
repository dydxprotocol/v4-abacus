#!/bin/bash

# Check if jq is installed
if ! command -v jq &> /dev/null
then
    echo "jq is not installed. Please install jq and try again."
    exit 1
fi

# Function to display usage information
usage() {
    echo "Usage: $0 -f <json_file> -a <attribute>"
    echo "  -f <json_file>  Path to the JSON file"
    echo "  -a <attribute>  Attribute to remove"
    exit 1
}

# Parse command-line arguments
while getopts "f:a:" opt; do
  case $opt in
    f)
      JSON_FILE=$OPTARG
      ;;
    a)
      ATTRIBUTE=$OPTARG
      ;;
    *)
      usage
      ;;
  esac
done

# Check if both JSON file and attribute are provided
if [ -z "$JSON_FILE" ] || [ -z "$ATTRIBUTE" ]; then
    usage
fi

# Check if the JSON file exists
if [ ! -f "$JSON_FILE" ]; then
    echo "File not found: $JSON_FILE"
    exit 1
fi

# Remove the attribute from the JSON file
jq "walk(if type == \"object\" then del(.${ATTRIBUTE}) else . end)" "$JSON_FILE" > tmp.json && mv tmp.json "$JSON_FILE"


echo "Attribute '$ATTRIBUTE' removed from '$JSON_FILE'"

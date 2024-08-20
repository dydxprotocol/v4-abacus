#!/bin/sh

# This script is used to update the generated code from the swagger-codegen
# Usage: swagger_update_file.sh <file>

FILE=$1
if [ -z "$FILE" ]; then
    echo "Usage: swagger_update_file.sh <file>"
    exit 1
fi

# Add "import kotlinx.serialization.Serializable" after "package indexer.codegen"
sed -i '' 's/package indexer.codegen/package indexer.codegen\n\nimport kotlinx.serialization.Serializable\nimport kotlin.js.JsExport/' $FILE

# Add @Serializable annotation to the class
sed -i '' 's/^data\ class /@JsExport\n@Serializable\ndata\ class /' $FILE
sed -i '' 's/^enum\ class /@JsExport\n@Serializable\nenum\ class /' $FILE
sed -i '' 's/^class /@JsExport\n@Serializable\nclass /' $FILE

# For each of the line that starts with ""import indexer.codegen.", get the class name and replace it with IndexerCLASSNAME"
# For example, if the line is "import indexer.codegen.Order", replace it with "import indexer.codegen.IndexerOrder"

LINES=$(grep 'import indexer.codegen.' $FILE)
# if LINES is NOT empty, then loop through each line
if [[ ! -z "$LINES" ]]; then
    # Loop through each line
    while read -r LINE; do
        # Get the class name
        CLASSNAME=$(echo $LINE | sed 's/import indexer.codegen.//')

        echo "Replacing $CLASSNAME with Indexer$CLASSNAME in $FILE"
        # Replace CLASSNAME that not starts with Indexer with IndexerCLASSNAME in the file
        sed -i '' "s/$CLASSNAME/Indexer${CLASSNAME}/g" $FILE

    done <<< "$LINES"
fi

# Replace "import indexer.codegen." with 'import indexer.codegen.Indexer'
# sed -i '' 's/import indexer.codegen\./import indexer.codegen.Indexer/' $FILE

# Get the file name with the extension
FILENAME=$(basename $FILE)

# Get the file name without the extension
CLASSNAME="${FILENAME%.*}"

# Get the extension
EXTENSION="${FILENAME##*.}"

# Replace CLASSNAME in the file with IndexerCLASSNAME in the class name
sed -i '' "s/class $CLASSNAME/class Indexer$CLASSNAME/" $FILE
sed -i '' "s/typealias $CLASSNAME/typealias Indexer$CLASSNAME/" $FILE

# Rename the file
mv $FILE $(dirname $FILE)/Indexer$CLASSNAME.$EXTENSION
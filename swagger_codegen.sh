#!/bin/sh

CURRENT_DIR=$(pwd)

# Defining a temporary directory for cloning
TMP_DIR=$(mktemp -d)

# Function to clean up the temporary directory
cleanup() {
    echo "Cleaning up..."
    rm -rf "$TMP_DIR"
}

# Trap to clean up in case of script exit or interruption
trap cleanup EXIT

curl -o $TMP_DIR/swagger.json https://raw.githubusercontent.com/dydxprotocol/v4-chain/main/indexer/services/comlink/public/swagger.json

# Remove required attribute
${CURRENT_DIR}/json_remove_attr.sh -f $TMP_DIR/swagger.json -a required

# Remove APIOrderStatus
${CURRENT_DIR}/json_remove_attr.sh -f  $TMP_DIR/swagger.json -a APIOrderStatus

# Codegen doesn't support allOf with enum, so we need to replace it with the enum directly

# Add APIOrderStatus with content of OrderStatus and BestEffortOrderStatus
${CURRENT_DIR}/json_add_attr.sh $TMP_DIR/swagger.json '.components.schemas' APIOrderStatus TO_REPLACE

# Remove "TO_REPLACE" with the content of OrderStatus and BestEffortOrderStatus
sed -i '' "s/\"TO_REPLACE\"/{ \"enum\": [\"OPEN\",\"FILLED\",\"CANCELED\",\"BEST_EFFORT_CANCELED\",\"UNTRIGGERED\",\"BEST_EFFORT_OPENED\"],\"type\": \"string\" }/g" $TMP_DIR/swagger.json 

cd "$TMP_DIR"

swagger-codegen generate -i swagger.json -o genereated -l kotlin-client \
    --model-package indexer.codegen

mv genereated/src/main/kotlin/indexer/codegen/TransferResponseObject_sender.kt \
     genereated/src/main/kotlin/indexer/codegen/TransferResponseObjectSender.kt

# replace AllOfPerpetualPositionResponseObjectClosedAt with IsoString
sed -i '' 's/AllOfPerpetualPositionResponseObjectClosedAt/IsoString/' genereated/src/main/kotlin/indexer/codegen/PerpetualPositionResponseObject.kt

# for each of the time in the generated code, run "swagger_update_file.sh <file>"
find genereated/src/main/kotlin/indexer -type f \
    -exec $CURRENT_DIR/swagger_update_file.sh {} \;

rm -rf $CURRENT_DIR/src/commonMain/kotlin/indexer
mv genereated/src/main/kotlin/indexer  $CURRENT_DIR/src/commonMain/kotlin

cd $CURRENT_DIR
./gradlew spotlessApply

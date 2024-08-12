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

swagger-codegen generate -i swagger.json -o generated -l kotlin-client \
    --model-package indexer.codegen

mv generated/src/main/kotlin/indexer/codegen/TransferResponseObject_sender.kt \
     generated/src/main/kotlin/indexer/codegen/TransferResponseObjectSender.kt

# replace AllOfPerpetualPositionResponseObjectClosedAt with IsoString
sed -i '' 's/AllOfPerpetualPositionResponseObjectClosedAt/IsoString/' generated/src/main/kotlin/indexer/codegen/PerpetualPositionResponseObject.kt

# replace BESTEFFORTCANCELED with BEST_EFFORT_CANCELED in IndexerAPIOrderStatus.kt
sed -i '' 's/BESTEFFORTCANCELED/BEST_EFFORT_CANCELED/' generated/src/main/kotlin/indexer/codegen/APIOrderStatus.kt

# replace BESTEFFORTOPENED with BEST_EFFORT_OPENED in IndexerAPIOrderStatus.kt
sed -i '' 's/BESTEFFORTOPENED/BEST_EFFORT_OPENED/' generated/src/main/kotlin/indexer/codegen/APIOrderStatus.kt

# replace BESTEFFORTCANCELED with BEST_EFFORT_CANCELED in IndexerAPIOrderStatus.kt
sed -i '' 's/BESTEFFORTCANCELED/BEST_EFFORT_CANCELED/' generated/src/main/kotlin/indexer/codegen/OrderStatus.kt

# replace BESTEFFORTOPENED with BEST_EFFORT_OPENED in IndexerAPIOrderStatus.kt
sed -i '' 's/BESTEFFORTOPENED/BEST_EFFORT_OPENED/' generated/src/main/kotlin/indexer/codegen/OrderStatus.kt

# replace STOPLIMIT with STOP_LIMIT in IndexerAPIOrderType.kt
sed -i '' 's/STOPLIMIT/STOP_LIMIT/' generated/src/main/kotlin/indexer/codegen/OrderType.kt

# replace STOPMARKET with STOP_MARKET in IndexerAPIOrderType.kt
sed -i '' 's/STOPMARKET/STOP_MARKET/' generated/src/main/kotlin/indexer/codegen/OrderType.kt

# replace TRAILINGSTOP with TRAILING_STOP in IndexerAPIOrderType.kt
sed -i '' 's/TRAILINGSTOP/TRAILING_STOP/' generated/src/main/kotlin/indexer/codegen/OrderType.kt

# replace TAKEPROFIT with TAKE_PROFIT in IndexerAPIOrderType.kt
sed -i '' 's/TAKEPROFIT/TAKE_PROFIT/' generated/src/main/kotlin/indexer/codegen/OrderType.kt

# replace TAKEPROFITMARKET with TAKE_PROFIT_MARKET in IndexerAPIOrderType.kt
sed -i '' 's/TAKEPROFITMARKET/TAKE_PROFIT_MARKET/' generated/src/main/kotlin/indexer/codegen/OrderType.kt

# replace PerpetualPositionsMap with Map<String, PerpetualPositionResponseObject> in SubaccountResponseObject.kt
sed -i '' 's/\ PerpetualPositionsMap/\ Map<String, IndexerPerpetualPositionResponseObject>/g' generated/src/main/kotlin/indexer/codegen/SubaccountResponseObject.kt

# replace AssetPositionsMap with Map<String, AssetPositionResponseObject> in SubaccountResponseObject.kt
sed -i '' 's/\ AssetPositionsMap/\ Map<String, IndexerAssetPositionResponseObject>/g' generated/src/main/kotlin/indexer/codegen/SubaccountResponseObject.kt

# add import kotlinx.serialization.Serializable to the top of CandleResolution.kt
sed -i '' 's/package indexer.codegen/package indexer.codegen\n\nimport kotlinx.serialization.Serializable/' generated/src/main/kotlin/indexer/codegen/CandleResolution.kt

# add @SerialName("1MIN") to _1MIN("1MIN") in CandleResolution.kt
sed -i '' 's/_1MIN("1MIN")/@SerialName("1MIN")\n    _1MIN("1MIN")/' generated/src/main/kotlin/indexer/codegen/CandleResolution.kt

# add @SerialName("5MIN") to _5MIN("5MIN") in CandleResolution.kt
sed -i '' 's/_5MINS("5MINS")/@SerialName("5MINS")\n    _5MINS("5MINS")/' generated/src/main/kotlin/indexer/codegen/CandleResolution.kt

# add @SerialName("15MIN") to _15MIN("15MIN") in CandleResolution.kt
sed -i '' 's/_15MINS("15MINS")/@SerialName("15MINS")\n    _15MINS("15MINS")/' generated/src/main/kotlin/indexer/codegen/CandleResolution.kt

# add @SerialName("30MIN") to _30MIN("30MIN") in CandleResolution.kt
sed -i '' 's/_30MINS("30MINS")/@SerialName("30MINS")\n    _30MINS("30MINS")/' generated/src/main/kotlin/indexer/codegen/CandleResolution.kt

# add @SerialName("1HOUR") to _1HOUR("1HOUR") in CandleResolution.kt
sed -i '' 's/_1HOUR("1HOUR")/@SerialName("1HOUR")\n    _1HOUR("1HOUR")/' generated/src/main/kotlin/indexer/codegen/CandleResolution.kt

# add @SerialName("4HOUR") to _4HOUR("4HOUR") in CandleResolution.kt
sed -i '' 's/_4HOURS("4HOURS")/@SerialName("4HOURS")\n    _4HOURS("4HOURS")/' generated/src/main/kotlin/indexer/codegen/CandleResolution.kt

# add @SerialName("1DAY") to _1DAY("1DAY") in CandleResolution.kt
sed -i '' 's/_1DAY("1DAY")/@SerialName("1DAY")\n    _1DAY("1DAY")/' generated/src/main/kotlin/indexer/codegen/CandleResolution.kt


# for each of the time in the generated code, run "swagger_update_file.sh <file>"
find generated/src/main/kotlin/indexer -type f \
    -exec $CURRENT_DIR/swagger_update_file.sh {} \;

rm -rf $CURRENT_DIR/src/commonMain/kotlin/indexer/codegen
mv generated/src/main/kotlin/indexer/codegen  $CURRENT_DIR/src/commonMain/kotlin/indexer

cd $CURRENT_DIR
./gradlew spotlessApply

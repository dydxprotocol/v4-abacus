# Create Object

let appStateMachine = AppStateMachine()

At this moment, we only have an AppStateMachine object, that's not initialized

# Set up environment

Abacus supports both v3 and v4 environments, including production and testing environments.

let selections = appStateMachine.availableEnvironments()
selections returns an array of SelectionOption, which contains a value and a stringKey

FE apps should display a picker display the localized stringKey value. When user makes the selection, call

let response = appStateMachine.setEnvironment(environment)
FE apps should handle the response, which has a type of [AppStateResponse](AppStateResponse.md)

# Connect

FE apps should use reachability to detect the state of the network connectivity. When network is reachable, call

let response = appStateMachine.setReadyToConnect(true)

When network is no longer reachable, call

let response = appStateMachine.setReadyToConnect(true)

# [Networking](API/Networking.md)

Abacus is responsible for construct the network requests, and process the response payloads.

FE app is responsible for processing the network requests from Abacus, sending the request in platform code, and send the response data to Abacus for processing

# Actions

Actions are user interactions in the app which impact the state

There are three actions

fun [trade](API/Actions.md#trade)(data: String?, type: TradeInputField?): AppStateResponse
fun [closePosition](API/Actions.md#closePosition)(data: String?, type: ClosePositionInputField): AppStateResponse
fun [transfer](API/Actions.md#transfer)(data: String?, type: TransferInputField?): AppStateResponse


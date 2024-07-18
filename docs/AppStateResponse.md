# Purpose

AppStateResponse is the primary interface from Abacus to FE apps.

Whenever an action is performed, which may impact a state change, Abacus will return a AppStateResponse

FE apps are responsible for processing the AppStateResponse

# Definition (kotlin)

data class AppStateResponse(  
&emsp;val state: [PerpetualState](PerpetualState.md)?,  
&emsp;val changes: StateChanges?,  
&emsp;val errors: ParsingErrors?,  
&emsp;val networkRequests: [NetworkRequests](API/Networking.md#NetworkRequests)?,  
&emsp;val apiState: ApiState?,  
&emsp;val lastOrder: SubaccountOrder?  
) {  
}

# state

The state field with type of PerpetualState contains the state of the state machine.

FE apps should update UI when the state object changes.

Note, state is immutable. When data changes, a new object is created. So you can always compare the pointer of the state with previous value to know if update is needed.

This applies to all child objects too.

For example, state has a wallet object. A market update may changes the marketsSummary object, but not the wallet object. In this case, you can check the pointer and update markets UI only, but not the portfolio.

# changes

The changes object contains a list of enums and related fields to FE apps, which has changed. If you check the state object pointer, you don't have to use changes.

# errors

The errors object contains an optional list of upper level errors, generally as result of parsing network payload.

# networkRequests

Abacus may request the FE apps to perform certain network connection to retrieve or process data.

# apiState

For V4, Abacus can process validator and indexer data to understand the state of network health.

# lastOrder

For V4, lastOrder is the last SubaccountOrder object, which is associated with the previous trade order placed.

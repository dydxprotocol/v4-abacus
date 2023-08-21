# Overview

Abacus [AppStateResponse](../AppStateResponse.md) contains IO requests which the FE app needs to process in platform code.

For every Abacus function which requires a AppStateResponse, FE app should check the networkRequests([NetworkRequests](#NetworkRequests)) field, and process network requests as needed.

When receiving the response from a HTTP request, FE should call appStateMachine.

# NetworkRequests

data class NetworkRequests(  
&emsp;val socketRequests: Array<[SocketRequest](#SocketRequest)>?,  
&emsp;val restRequests: Array<[RestRequest](#RestRequest)>?  
)

# SocketRequest

data class SocketRequest(  
&emsp;val type: SocketRequestType,  
&emsp;val url: AbUrl,  
&emsp;val text: String?
)

FE app needs to process the socket request, which includes connecting the socket, send text to the channel, and close connection

## SocketRequestType

    SocketConnect   
    SocketText  
    SocketClose  

## url

The URL of the socket

## text

For SocketRequestType of socketText, send the text to the socket

# RestRequest

data class RestRequest(  
&emsp;val url: AbUrl,  
&emsp;val verb: HttpVerb,  
&emsp;val headers: NetworkParams?,  
&emsp;val body: String?,
)

## url

URL for the HTTP request

## verb

GET, POST, PUT or DELETE

## headers

FE app should add the headers to the request as key-value pairs

## body

For POST and PUT requests, send in the body

# setSocketConnected

fun setSocketConnected(url: AbUrl, socketConnected: Boolean): AppStateResponse

When a websocket is connected or disconnected, FE should call AppStateMachine's setSocketConnected and process the response

## url

URL of the socket

## socketConnected

A boolean flag, whether the socket is connected or disconnected

# processSocketResponse

fun processSocketResponse(url: AbUrl, text: String): AppStateResponse

Whenever FE app receives data from the socket, it should call Abacus processSocketResponse, and process the response

## url

URL of the socket

## text

Text data from the socket

# processHttpResponse

fun processHttpResponse(url: AbUrl, text: String): AppStateResponse

When FE app receives a response from the HTTP request, call Abacus processHttpResponse, and process the response

## url

URL of the HTTP request

## text

Response body as text
# Wallet

wallet in PerpetualState is an object, containing user(v3 only) and account information

data class Wallet(  
&emsp;val walletAddress: String?,  
&emsp;val balance: TradeStatesWithStringValues?,  
&emsp;val user: User?  
)

## walletAddress

walletAddress (ETH for v3)

## balance

The balance of the wallet in current, postOrder and postAllOrder states. They are big decimal expressed in string format

## user

user is the type of [User](#User)

# User

data class User(  
&emsp;val isRegistered: Boolean,  
&emsp;val email: String?,  
&emsp;val username: String?,  
&emsp;val makerFeeRate: Double,  
&emsp;val takerFeeRate: Double,  
&emsp;val makerVolume30D: Double,  
&emsp;val takerVolume30D: Double,  
&emsp;val fees30D: Double,  
&emsp;val isEmailVerified: Boolean,  
&emsp;val country: String?,  
&emsp;val favorited: Array<String>?,  
&emsp;val walletId: String?  
)

## isRegistered

Whether the user has gone through onboarding

## email

Email address

## username

user Name

## makerFeeRate

Fee rate for maker orders

## takeFeeRate

Fee rate for taker orders

## makerVolume30D

Maker order volume for the last 30 days

## takerVolume30D

Take order volume for the last 30 days

## fees30D

Total fee paid over the last 30 days

## isEmailVerified

Whether the email address has been verified

## country

Not used?

## favorited

A list of markets favorited by the user

## walletId

Type of wallet
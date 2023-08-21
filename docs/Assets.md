# Assets

assets in PerpetualState is a dictionary of String to Asset

An asset contains static data to display an asset

data class Asset(  
&emsp;val id: String,  
&emsp;val symbol: String?,  
&emsp;val name: String?,  
&emsp;val tags: Array<String>?,  
&emsp;val circulatingSupply: Double?,  
&emsp;val resources: [AssetResources](#resources)?  
)

# id

ID of the Asset. Other objects may contain an assetId to refer to this

# symbol

Symbol of the Asset, such as BTC or ETH

# name

Name of the Asset, such as "Bitcoin" or "Ethereum"

# tags

The tags are used to describe the Asset. For example, Assets for Layer 1 chains contain a "Layer 1" tag. An Asset may have zero to many tags

# circulatingSupply

Number of tokens in circulation

# resources

This is used to display the Asset, defined as

data class AssetResources(  
&emsp;val websiteLink: String?,  
&emsp;val whitepaperLink: String?,  
&emsp;val coinMarketCapsLink: String?,  
&emsp;val imageUrl: String?,  
&emsp;val primaryDescriptionKey: String?,  
&emsp;val secondaryDescriptionKey: String?  
)

## resources.websiteLink

Link to the project website

## resources.whitepaperLink

Link to the project white paper

## resources.coinMarketCapsLink

Link to the CoinMarketCaps page, showing the Asset

## resources.imageUrl

Link to the image icon

## resources.primaryDescriptionKey

A string key to display the localized primary description

## resources.secondaryDescriptionKey

A string key to display the localized secondary description
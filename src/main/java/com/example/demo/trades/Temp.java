//  /*
//      *  ROLE_USER only: get all stock info, or one stock info at stockURL + "/{symbol}" - SECURITY CONFIG AT END

//      *  Market should open from 9am to 5pm on weekdays only. -> CONDITIONS TO BE MET BEFORE STOCKS CAN BE PURCHASED?
        
//         Price/volumes could be changing over time to reflect the trade info in the market.
//         Note: we will not test add/update/delete stocks.

//         Your API should be providing the info below. 
//         Note: it is not required to continuously fetch external pricing info.
//         You can just simply obtain the initial static pricing of stocks, 
//         and add small random variations to simulate bids/asks, etc.

//         The volume/bid/ask info below should reflect the actual trades in your API.
//         Note that there can be many different trades with different bid/ask values for a stock,
//         your API should only show info for the best trade (best price, or if the prices are 
//         the same then trade submitted earlier has the priority).
        
//         The last_price should reflect the actual price for the last trade done in your API.
//         If no trade has been done, the last_price can be obtained from an external data source.
//         Note that bid is always smaller than ask price.
    
//         Market Maker function: your API should be generating various sell and buy orders
//             for all traded stocks at appropriate bids and asks (based on actual last_price).
//             The volumes for these orders should be specified or fixed by your API, i.e.,
//             you can specify any volumes required for testing purposes, e.g., 20000.
//             This is to create liquidity in the market, and facilitate fast order matching.
     
//         *** Example:
//         *** Your API can obtain initial static pricing from https://www.sgx.com/indices/products/sti
//         *** When your API starts (or market starts), your API will auto-create multiple open buy and sell trades,
//         *** one pair (buy and sell) for each stock listed at the bid and ask price, respectively.
//         *** The volumes of these trades can be set to a fixed value, say 20000.

//         *** E.g., for A17U, based on the actual last_price of $3.28, you can create a sell trade with ask = $3.29 & volume = 20000,
//         *** and a buy trade with bid = $3.26 & volume = 20000.
        
//         *** These trades are referred to as the market maker's trades - to create liquidity in the market.
//         *** The customers' trades can then be matched with these market maker's trades.

//     */
//     public static String stockURL = baseURL + "/stocks";

//     /** 
//      *  For ROLE_USER only: create trade via tradeURL, cancel/view each trade via tradeURL + "/{id}" - SECURITY CONFIG AT END
//      * 
//      *  Trade stocks: no short-selling, no contra trading (upfront cash required to buy)
//      *  Lot size: 100 (buy or sell have to be in multiples of 100)

//      *   + Limit order: customer can specify any price they like. 
//      *     The limit order might not be matched, i.e., it stays open till end of day (5pm) and expires.
//      *     ** Example: customer submits a trade to sell at 2x ask price. This order might never be filled.
//      * 
//      *   + Market order: buy or sell at market price. This kind of order should be matched fast,
//      *     subjecting to available volume, but there is no guarantee for the final filled price.
//      *     ** Example 1: customer submits a buy trade of 2000 A17U stocks at market price.
//      *                   The trade is filled right away by market maker with price of $3.29 (ask price).
//      *     ** Example 2: customer submits a sell trade of 40000 A17U stocks at market price.
//      *                   The trade is partial-filled right away with quantity of 20000 (market maker's buy), price of $3.26 (bid price).
//      *                   If there is no more open trade to fill this trade, it is partial-filled.
     
//      *  Trade matching is done according to price/time priority:
//      *   + The better-priced trade will be matched first
//      *   + If prices are the same, earlier trade will be matched first
//      * 
//      *   + Buy trades having limit price above market price (current ask) will be matched at current ask.
//      *      * Example: a buy trade for A17U with price of $4 will be matched at $3.29 (current ask)
//      * 
//      *   + Sell trades having limit price below market price (current bid) will be matched at current bid.
//      *      * Example: a sell trade for A17U with price of $3 will be match at $3.26 (current bid)
//      * 
//      *   + One trade can be matched by several other trades depending on the volumes.
     
//      *  Settlement will be done via custommer's account (cash settlement)
//      * 
//      *  Note: a trade might involve one or more fund transfer transactions.
//      *  For buy trades, make sure the account balance is enough when doing matching.
//      *  If the balance is not enough, the buy trade might be partially filled.
//      * 
//      *  For sell trades, make sure the customer has the stocks in his portfolio to sell.

//      *  All numbers are double except quantity/volume (int) and timestamp (long). - CHANGE TIMESTAMP TO LONG
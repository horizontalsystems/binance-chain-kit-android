package io.horizontalsystems.binancechainkit.core.api

import io.horizontalsystems.binancechainkit.helpers.EncodeUtils


enum class Source(val value: Int) {
    HIDDEN(0),
    BROADCAST(1)
}

enum class TransactionType {
    NEW_ORDER,
    ISSUE_TOKEN,
    BURN_TOKEN,
    LIST_TOKEN,
    CANCEL_ORDER,
    FREEZE_TOKEN,
    UN_FREEZE_TOKEN,
    TRANSFER,
    PROPOSAL,
    VOTE
}

enum class TxType {
    NEW_ORDER,
    CANCEL_ORDER,
    FREEZE_TOKEN,
    UNFREEZE_TOKEN,
    TRANSFER,
    VOTE,
    ISSUE,
    BURN,
    MINT,
    SUBMIT_PROPOSAL,
    DEPOSIT,
    CREATE_VALIDATOR,
    REMOVE_VALIDATOR,
    LISTING,
    TimeLock,
    TimeUnlock,
    TimeRelock,
    SetAccountFlag
}

enum class MessageType private constructor(typePrefix: String?) {

    Send("2A2C87FA"),
    NewOrder("CE6DC043"),
    CancelOrder("166E681B"),
    TokenFreeze("E774B32D"),
    TokenUnfreeze("6515FF0D"),
    StdSignature(null),
    PubKey("EB5AE987"),
    StdTx("F0625DEE"),
    Vote("A1CADD36"),
    Issue("17EFAB80"),
    Burn("7ED2D2A0"),
    Mint("467E0829"),
    SubmitProposal("B42D614E"),
    Deposit("A18A56E5"),
    CreateValidator("DB6A19FD"),
    RemoveValidator("C1AFE85F"),
    Listing("B41DE13F"),
    TimeLock("07921531"),
    TimeUnlock("C4050C6C"),
    TimeRelock("504711DA"),
    SetAccountFlag("BEA6E301");

    var typePrefixBytes: ByteArray
        private set

    init {
        if (typePrefix == null) {
            this.typePrefixBytes = ByteArray(0)
        }
        else
            this.typePrefixBytes = EncodeUtils.hexStringToByteArray(typePrefix)
    }

//    companion object {
//
//        fun getMessageType(bytes: ByteArray?): MessageType? {
//
//            if (null == bytes || bytes.size < 4) {
//                null
//            }
//            else MessageType.values().filter {
//                 type ->
//                    if ( type.typePrefixBytes.size < 4) {
//                        return type
//                    }
//                    for (i in 0..3) {
//                        if (type.typePrefixBytes[i] != bytes[i]) {
//                            return type
//                        }
//                    }
//                    true
//                }.fil
//                .findAny().orElse(null)
//        }
//    }

}
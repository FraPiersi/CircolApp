package com.example.circolapp.model

import java.util.Date

data class RichiestaTessera(
    val id: String = "",
    val uidUtente: String = "",
    val nomeUtente: String = "",
    val emailUtente: String = "",
    val dataRichiesta: Date = Date(),
    val tipo: TipoRichiesta = TipoRichiesta.NUOVA,
    val stato: StatoRichiesta = StatoRichiesta.IN_ATTESA,
    val note: String = ""
)

enum class TipoRichiesta {
    NUOVA, RINNOVO
}

enum class StatoRichiesta {
    IN_ATTESA, APPROVATA, RIFIUTATA
}

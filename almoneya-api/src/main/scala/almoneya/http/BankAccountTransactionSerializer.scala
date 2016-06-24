package almoneya.http

import almoneya.BankAccountTransaction
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{JsonSerializer, SerializerProvider}

class BankAccountTransactionSerializer extends JsonSerializer[BankAccountTransaction] {
    override def serialize(txn: BankAccountTransaction, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
        gen.writeStartObject()

        gen.writeObjectField("id", txn.id)
        gen.writeObjectField("bank_account", txn.bankAccount)
        gen.writeObjectField("check_num", txn.checkNum)
        gen.writeObjectField("description1", txn.description1)
        gen.writeObjectField("description2", txn.description2)
        gen.writeObjectField("posted_on", txn.postedOn)
        gen.writeObjectField("amount", txn.amount)
        gen.writeObjectField("reconciled", txn.reconciled)

        gen.writeEndObject()
    }
}

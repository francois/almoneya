package almoneya.http

import almoneya.BankAccountTransaction
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{JsonSerializer, SerializerProvider}

class BankAccountTransactionSerializer extends JsonSerializer[BankAccountTransaction] {
    override def serialize(txn: BankAccountTransaction, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
        gen.writeStartObject()

        txn.id.foreach { id => gen.writeFieldName("bank_account_transaction_id"); gen.writeObject(id) }
        gen.writeObjectField("bank_account", txn.bankAccount)
        txn.checkNum.foreach { checkNum => gen.writeFieldName("check_num"); gen.writeObject(checkNum) }
        txn.description1.foreach { desc1 => gen.writeFieldName("description1"); gen.writeObject(desc1) }
        txn.description2.foreach { desc2 => gen.writeFieldName("description2"); gen.writeObject(desc2) }
        gen.writeObjectField("posted_on", txn.postedOn)
        gen.writeObjectField("amount", txn.amount)

        gen.writeEndObject()
    }
}

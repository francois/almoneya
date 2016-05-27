package almoneya.http

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{JsonSerializer, SerializerProvider}

class TransactionFormSerializer extends JsonSerializer[TransactionForm] {
    override def serialize(form: TransactionForm, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
        gen.writeStartObject()

        gen.writeObjectField("payee", form.payee)
        form.description.foreach(desc => gen.writeObjectField("description", desc))
        gen.writeObjectField("posted_on", form.postedOn)
        gen.writeObjectField("entries", form.entries)
        form.bankAccountTransactionId.foreach(id => gen.writeObjectField("bank_account_transaction_id", id))

        gen.writeEndObject()
    }
}

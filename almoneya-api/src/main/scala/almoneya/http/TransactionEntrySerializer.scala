package almoneya.http

import almoneya.TransactionEntry
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{JsonSerializer, SerializerProvider}

class TransactionEntrySerializer extends JsonSerializer[TransactionEntry] {
    override def serialize(entry: TransactionEntry, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
        gen.writeStartObject()

        entry.transactionEntryId.foreach { id => gen.writeFieldName("transaction_entry_id"); gen.writeObject(id) }
        gen.writeObjectField("account", entry.account)
        gen.writeFieldName("amount")
        gen.writeObject(entry.amount)

        gen.writeEndObject()
    }
}

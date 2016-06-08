package almoneya.http

import almoneya.TransactionEntry
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{JsonSerializer, SerializerProvider}

class TransactionEntrySerializer extends JsonSerializer[TransactionEntry] {
    override def serialize(entry: TransactionEntry, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
        gen.writeStartObject()

        gen.writeObjectField("id", entry.transactionEntryId)
        gen.writeObjectField("account", entry.account)
        gen.writeObjectField("amount", entry.amount)

        gen.writeEndObject()
    }
}

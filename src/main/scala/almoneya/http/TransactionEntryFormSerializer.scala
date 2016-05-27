package almoneya.http

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{JsonSerializer, SerializerProvider}

class TransactionEntryFormSerializer extends JsonSerializer[TransactionEntryForm] {
    override def serialize(entry: TransactionEntryForm, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
        gen.writeStartObject()

        gen.writeObjectField("account_name", entry.accountName)
        gen.writeObjectField("amount", entry.amount)

        gen.writeEndObject()
    }
}

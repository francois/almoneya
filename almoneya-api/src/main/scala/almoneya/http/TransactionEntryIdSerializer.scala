package almoneya.http

import almoneya.TransactionEntryId
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{JsonSerializer, SerializerProvider}

class TransactionEntryIdSerializer extends JsonSerializer[TransactionEntryId] {
    override def serialize(transactionEntryId: TransactionEntryId, gen: JsonGenerator, serializers: SerializerProvider): Unit =
        gen.writeNumber(transactionEntryId.value)
}

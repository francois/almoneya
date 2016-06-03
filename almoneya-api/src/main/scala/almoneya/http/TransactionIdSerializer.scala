package almoneya.http

import almoneya.TransactionId
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{SerializerProvider, JsonSerializer}

class TransactionIdSerializer extends JsonSerializer[TransactionId] {
    override def serialize(transactionId: TransactionId, gen: JsonGenerator, serializers: SerializerProvider): Unit =
    gen.writeNumber(transactionId.value)
}

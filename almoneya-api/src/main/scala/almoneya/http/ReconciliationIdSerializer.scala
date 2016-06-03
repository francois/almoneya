package almoneya.http

import almoneya.ReconciliationId
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{JsonSerializer, SerializerProvider}

class ReconciliationIdSerializer extends JsonSerializer[ReconciliationId] {
    override def serialize(reconciliationId: ReconciliationId, gen: JsonGenerator, serializers: SerializerProvider): Unit =
        gen.writeNumber(reconciliationId.value)
}

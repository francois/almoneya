package almoneya.http

import almoneya.ReconciliationEntryId
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{JsonSerializer, SerializerProvider}

class ReconciliationEntryIdSerializer extends JsonSerializer[ReconciliationEntryId] {
    override def serialize(reconciliationEntryId: ReconciliationEntryId, gen: JsonGenerator, serializers: SerializerProvider): Unit =
        gen.writeNumber(reconciliationEntryId.value)
}

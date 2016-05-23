package almoneya.http

import almoneya.Envelope
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{JsonSerializer, SerializerProvider}

class EnvelopeSerializer extends JsonSerializer[Envelope] {
    override def serialize(envelope: Envelope, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
        gen.writeStartObject()

        if (envelope.id.isDefined) gen.writeNumberField("id", envelope.id.get.value)
        gen.writeStringField("name", envelope.name.value)

        gen.writeEndObject()
    }
}

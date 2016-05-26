package almoneya.http

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{JsonSerializer, SerializerProvider}
import org.joda.time.ReadableInstant

class ReadableInstantSerializer extends JsonSerializer[ReadableInstant] {
    override def serialize(instant: ReadableInstant, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
        gen.writeString(instant.toString)
    }
}

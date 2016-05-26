package almoneya.http

import almoneya.Description
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{JsonSerializer, SerializerProvider}

class DescriptionSerializer extends JsonSerializer[Description] {
    override def serialize(description: Description, gen: JsonGenerator, serializers: SerializerProvider): Unit =
        gen.writeString(description.value)
}

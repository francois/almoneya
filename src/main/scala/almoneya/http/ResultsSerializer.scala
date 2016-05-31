package almoneya.http

import almoneya.http.FrontController.Results
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{JsonSerializer, SerializerProvider}

class ResultsSerializer extends JsonSerializer[Results[_]] {
    override def serialize(value: Results[_], gen: JsonGenerator, serializers: SerializerProvider): Unit = {
        gen.writeStartObject()
        gen.writeObjectField("data", value.data)
        gen.writeEndObject()
    }
}

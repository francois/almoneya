package almoneya.http

import almoneya.CheckNum
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{SerializerProvider, JsonSerializer}

class CheckNumSerializer extends JsonSerializer[CheckNum]{
    override def serialize(checkNum: CheckNum, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
        gen.writeString(checkNum.value)
    }
}

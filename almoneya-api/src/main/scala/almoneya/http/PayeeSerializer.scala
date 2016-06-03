package almoneya.http

import almoneya.Payee
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{JsonSerializer, SerializerProvider}

class PayeeSerializer extends JsonSerializer[Payee] {
    override def serialize(payee: Payee, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
        gen.writeString(payee.value)
    }
}

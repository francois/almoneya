package almoneya.http

import almoneya.Amount
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{SerializerProvider, JsonSerializer}

class AmountSerializer extends JsonSerializer[Amount] {
    override def serialize(amount: Amount, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
        gen.writeString(amount.value.toString())
    }
}

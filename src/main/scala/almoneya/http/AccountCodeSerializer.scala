package almoneya.http

import almoneya.AccountCode
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{JsonSerializer, SerializerProvider}

class AccountCodeSerializer extends JsonSerializer[AccountCode] {
    override def serialize(code: AccountCode, gen: JsonGenerator, serializers: SerializerProvider): Unit =
        gen.writeString(code.value)
}

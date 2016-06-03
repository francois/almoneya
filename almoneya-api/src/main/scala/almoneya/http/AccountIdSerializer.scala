package almoneya.http

import almoneya.AccountId
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{JsonSerializer, SerializerProvider}

class AccountIdSerializer extends JsonSerializer[AccountId] {
    override def serialize(id: AccountId, gen: JsonGenerator, serializers: SerializerProvider): Unit =
        gen.writeNumber(id.value)
}

package almoneya.http

import almoneya.AccountLast4
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{JsonSerializer, SerializerProvider}

class AccountLast4Serializer extends JsonSerializer[AccountLast4] {
    override def serialize(last4: AccountLast4, gen: JsonGenerator, serializers: SerializerProvider): Unit =
        gen.writeString(last4.value)

}

package almoneya.http

import almoneya.AccountHash
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{SerializerProvider, JsonSerializer}

class AccountHashSerializer extends JsonSerializer[AccountHash] {
    override def serialize(accountHash: AccountHash, gen: JsonGenerator, serializers: SerializerProvider): Unit =
    gen.writeString(accountHash.value)
}

package almoneya.http

import almoneya.AccountKind
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{JsonSerializer, SerializerProvider}

class AccountKindSerializer extends JsonSerializer[AccountKind] {
    override def serialize(kind: AccountKind, gen: JsonGenerator, serializers: SerializerProvider): Unit =
        gen.writeString(kind.kindName)
}

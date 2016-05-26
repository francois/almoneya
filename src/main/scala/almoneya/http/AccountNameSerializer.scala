package almoneya.http

import almoneya.AccountName
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{JsonSerializer, SerializerProvider}

class AccountNameSerializer extends JsonSerializer[AccountName] {
    override def serialize(accountName: AccountName, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
        gen.writeString(accountName.value)
    }
}

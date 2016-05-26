package almoneya.http

import almoneya.Account
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{JsonSerializer, SerializerProvider}

class AccountSerializer extends JsonSerializer[Account] {
    override def serialize(account: Account, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
        gen.writeStartObject()

        account.id.foreach { id => gen.writeFieldName("account_id"); gen.writeObject(account.id) }
        account.code.foreach { code => gen.writeFieldName("code"); gen.writeObject(code) }
        gen.writeFieldName("name")
        gen.writeObject(account.name)
        gen.writeFieldName("kind")
        gen.writeObject(account.kind)
        gen.writeBooleanField("virtual", account.virtual)

        gen.writeEndObject()
    }
}

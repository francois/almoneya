package almoneya.http

import almoneya.Account
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{JsonSerializer, SerializerProvider}

class AccountSerializer extends JsonSerializer[Account] {
    override def serialize(value: Account, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
        val id = value.id
        val code = value.code
        val name = value.name
        val kind = value.kind

        gen.writeStartObject()

        if (id.isDefined) gen.writeNumberField("id", id.get.value)
        if (code.isDefined) gen.writeStringField("code", code.get.value)
        gen.writeStringField("name", name.value)
        gen.writeStringField("kind", kind.kindName)

        gen.writeEndObject()
    }
}

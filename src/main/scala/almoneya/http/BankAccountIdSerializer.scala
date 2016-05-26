package almoneya.http

import almoneya.BankAccountId
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{JsonSerializer, SerializerProvider}

class BankAccountIdSerializer extends JsonSerializer[BankAccountId] {
    override def serialize(bankAccountId: BankAccountId, gen: JsonGenerator, serializers: SerializerProvider): Unit =
        gen.writeNumber(bankAccountId.value)
}

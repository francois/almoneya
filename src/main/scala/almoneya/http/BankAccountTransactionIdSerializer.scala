package almoneya.http

import almoneya.BankAccountTransactionId
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{JsonSerializer, SerializerProvider}

class BankAccountTransactionIdSerializer extends JsonSerializer[BankAccountTransactionId] {
    override def serialize(bankAccountTransactionId: BankAccountTransactionId, gen: JsonGenerator, serializers: SerializerProvider): Unit =
        gen.writeNumber(bankAccountTransactionId.value)
}

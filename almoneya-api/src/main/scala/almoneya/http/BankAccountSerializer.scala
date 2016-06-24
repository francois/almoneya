package almoneya.http

import almoneya.BankAccount
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{JsonSerializer, SerializerProvider}

class BankAccountSerializer extends JsonSerializer[BankAccount] {
    override def serialize(bankAccount: BankAccount, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
        gen.writeStartObject()

        gen.writeObjectField("id", bankAccount.id)
        gen.writeObjectField("account_hash", bankAccount.accountHash)
        gen.writeObjectField("last4", bankAccount.last4)
        gen.writeObjectField("account", bankAccount.account)

        gen.writeEndObject()
    }
}

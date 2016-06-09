package almoneya.http

import almoneya.Transaction
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{JsonSerializer, SerializerProvider}

class TransactionSerializer extends JsonSerializer[Transaction] {
    override def serialize(transaction: Transaction, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
        gen.writeStartObject()

        gen.writeObjectField("id", transaction.transactionId)
        gen.writeObjectField("payee", transaction.payee)
        gen.writeObjectField("description", transaction.description)
        gen.writeObjectField("posted_on", transaction.postedOn.toString("yyyy-MM-dd"))
        gen.writeObjectField("booked_at", transaction.bookedAt.toString("yyyy-MM-dd HH:MM:SS"))
        gen.writeObjectField("balance", transaction.balance)
        gen.writeObjectField("entries", transaction.entries)

        gen.writeEndObject()
    }
}

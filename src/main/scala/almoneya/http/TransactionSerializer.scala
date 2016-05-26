package almoneya.http

import almoneya.Transaction
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{JsonSerializer, SerializerProvider}

class TransactionSerializer extends JsonSerializer[Transaction] {
    override def serialize(transaction: Transaction, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
        gen.writeStartObject()

        transaction.transactionId.foreach { id => gen.writeFieldName("transaction_id"); gen.writeObject(id) }

        gen.writeFieldName("payee")
        gen.writeObject(transaction.payee)

        transaction.description.foreach { desc => gen.writeFieldName("description"); gen.writeObject(desc.value) }

        gen.writeFieldName("posted_on")
        gen.writeObject(transaction.postedOn.toString("yyyy-MM-dd"))

        gen.writeFieldName("booked_at")
        gen.writeObject(transaction.bookedAt.toString("yyyy-MM-dd HH:MM:SS"))

        gen.writeObjectField("entries", transaction.entries)

        gen.writeEndObject()
    }
}

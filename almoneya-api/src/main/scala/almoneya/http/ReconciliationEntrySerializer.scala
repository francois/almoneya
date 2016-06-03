package almoneya.http

import almoneya.ReconciliationEntry
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{JsonSerializer, SerializerProvider}

class ReconciliationEntrySerializer extends JsonSerializer[ReconciliationEntry] {
    override def serialize(reconciliationEntry: ReconciliationEntry, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
        gen.writeStartObject()

        reconciliationEntry.id.foreach { id => gen.writeObjectField("reconciliation_entry_id", id) }
        gen.writeObjectField("account_name", reconciliationEntry.accountName)
        gen.writeObjectField("transaction_id", reconciliationEntry.transactionId)
        gen.writeObjectField("posted_on", reconciliationEntry.postedOn)

        gen.writeEndObject()
    }
}

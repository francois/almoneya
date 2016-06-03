package almoneya.http

import almoneya.Reconciliation
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{JsonSerializer, SerializerProvider}

class ReconciliationSerializer extends JsonSerializer[Reconciliation] {
    override def serialize(reconciliation: Reconciliation, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
        gen.writeStartObject()

        reconciliation.id.foreach(id => gen.writeObjectField("reconciliation_id", id))
        gen.writeObjectField("account_name", reconciliation.accountName)
        gen.writeObjectField("posted_on", reconciliation.postedOn)
        gen.writeObjectField("opening_balance", reconciliation.openingBalance)
        gen.writeObjectField("ending_balance", reconciliation.endingBalance)
        reconciliation.closedAt.foreach(closedAt => gen.writeObjectField("closed_at", closedAt))
        reconciliation.notes.foreach(notes => gen.writeObjectField("notes", notes))

        gen.writeEndObject()
    }
}

package almoneya.http

import almoneya._
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{JsonSerializer, SerializerProvider}

class ObligationSerializer extends JsonSerializer[Obligation] {
    override def serialize(obligation: Obligation, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
        gen.writeStartObject()

        if (obligation.id.isDefined) gen.writeNumberField("id", obligation.id.get.value)
        gen.writeObjectField("envelope", obligation.envelope)
        if (obligation.description.isDefined) gen.writeStringField("description", obligation.description.get.value)
        gen.writeStringField("start_on", obligation.startOn.toString("yyyy-MM-dd"))
        if (obligation.endOn.isDefined) gen.writeStringField("end_on", obligation.endOn.get.toString("yyyy-MM-dd"))
        gen.writeNumberField("amount", obligation.amount.toNumeric)
        gen.writeNumberField("every", obligation.every.value)
        gen.writeStringField("period", obligation.period match {
            case Daily => "day"
            case Weekly => "week"
            case Monthly => "month"
            case Yearly => "year"
        })

        gen.writeEndObject()
    }
}

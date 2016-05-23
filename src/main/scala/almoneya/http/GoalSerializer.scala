package almoneya.http

import almoneya._
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{JsonSerializer, SerializerProvider}

class GoalSerializer extends JsonSerializer[Goal] {
    override def serialize(goal: Goal, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
        gen.writeStartObject()

        if (goal.id.isDefined) gen.writeNumberField("id", goal.id.get.value)
        gen.writeObjectField("envelope", goal.envelope)
        if (goal.description.isDefined) gen.writeStringField("description", goal.description.get.value)
        gen.writeStringField("due_on", goal.dueOn.toString("yyyy-MM-dd"))
        gen.writeNumberField("amount", goal.amount.toNumeric)

        gen.writeEndObject()
    }
}

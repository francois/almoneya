package almoneya.http

import almoneya._
import almoneya.automation.FundingGoal
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{JsonSerializer, SerializerProvider}

class FundingGoalSerializer extends JsonSerializer[FundingGoal] {
    override def serialize(fundingGoal: FundingGoal, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
        gen.writeStartObject()

        gen.writeStringField("name", fundingGoal.name.value)
        gen.writeNumberField("priority", fundingGoal.priority.value)
        gen.writeStringField("due_on", fundingGoal.dueOn.toString("yyyy-MM-dd"))
        gen.writeNumberField("balance", fundingGoal.balance.value)
        gen.writeNumberField("target", fundingGoal.target.value)

        gen.writeEndObject()
    }
}

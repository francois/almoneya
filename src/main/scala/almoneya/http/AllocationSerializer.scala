package almoneya.http

import almoneya._
import almoneya.automation.Allocation
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{JsonSerializer, SerializerProvider}

class AllocationSerializer extends JsonSerializer[Allocation] {
    override def serialize(allocation: Allocation, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
        gen.writeStartObject()

        gen.writeObjectField("goal", allocation.goal)
        gen.writeNumberField("plan_to_take", allocation.planToTake.value)
        gen.writeNumberField("real_take", allocation.realTake.value)

        gen.writeEndObject()
    }
}

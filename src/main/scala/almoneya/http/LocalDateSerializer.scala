package almoneya.http

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{JsonSerializer, SerializerProvider}
import org.joda.time.LocalDate

class LocalDateSerializer extends JsonSerializer[LocalDate] {
    override def serialize(date: LocalDate, gen: JsonGenerator, serializers: SerializerProvider): Unit =
        gen.writeString(date.toString("yyyy-MM-dd"))
}

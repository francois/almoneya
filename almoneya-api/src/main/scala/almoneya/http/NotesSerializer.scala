package almoneya.http

import almoneya.Notes
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{JsonSerializer, SerializerProvider}

class NotesSerializer extends JsonSerializer[Notes] {
    override def serialize(notes: Notes, gen: JsonGenerator, serializers: SerializerProvider): Unit =
        gen.writeString(notes.value)
}

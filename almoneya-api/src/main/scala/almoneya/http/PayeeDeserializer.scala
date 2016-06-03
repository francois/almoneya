package almoneya.http

import almoneya.Payee
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer

class PayeeDeserializer extends StdScalarDeserializer[Payee](classOf[Payee]) {
    override def deserialize(p: JsonParser, ctxt: DeserializationContext): Payee =
        Payee(p.getValueAsString)
}

package almoneya.http

import almoneya.Amount
import com.fasterxml.jackson.core.{JsonParseException, JsonParser, JsonToken}
import com.fasterxml.jackson.databind.{JsonMappingException, DeserializationContext}
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer

class AmountDeserializer extends StdScalarDeserializer[Amount](classOf[Amount]) {
    override def deserialize(p: JsonParser, ctxt: DeserializationContext): Amount = {
        Option(p.getCurrentToken) match {
            case Some(JsonToken.VALUE_STRING) => Amount(BigDecimal(p.getValueAsString))
            case Some(JsonToken.VALUE_NUMBER_FLOAT) | Some(JsonToken.VALUE_NUMBER_INT) => Amount(p.getDoubleValue)
            case Some(token) =>
                throw new JsonParseException(p, "Bad format: expected STRING or NUMBER, found " + token.asString())
            case None =>
                throw new JsonParseException(p, "Bad format: expected STRING or NUMBER, found null")
        }
    }
}

package almoneya.http

import almoneya.AccountName
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer

class AccountNameDeserializer extends StdScalarDeserializer[AccountName](classOf[AccountName]) {
    override def deserialize(p: JsonParser, ctxt: DeserializationContext): AccountName = {
        AccountName(p.getValueAsString)
    }
}

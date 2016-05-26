package almoneya.http

import almoneya.Payee
import org.scalatest.FunSuite

class PayeeSerializationTest extends FunSuite {
    test("round-trips from Scala object") {
        val value = Payee("RBC Banque Royale")
        val jsonString = JSON.mapper.writeValueAsString(value)
        val deserialized = JSON.mapper.readValue(jsonString, classOf[Payee])
        assert(value == deserialized)
    }

    test("round-trips from JSON String") {
        val jsonString = "\"RBC Banque Royale\""
        val payee = JSON.mapper.readValue(jsonString, classOf[Payee])
        val serialized = JSON.mapper.writeValueAsString(payee)
        assert(jsonString == serialized)
    }
}

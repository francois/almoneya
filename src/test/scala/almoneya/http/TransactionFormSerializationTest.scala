package almoneya.http

import almoneya.{AccountName, Amount, Description, Payee}
import org.joda.time.LocalDate
import org.scalatest.FunSuite

class TransactionFormSerializationTest extends FunSuite {
    val transactionForm = TransactionForm(Payee("Checking Account"), Some(Description("Roof repairs")), new LocalDate("2016-05-29"),
        Set(TransactionEntryForm(AccountName("Checking Account"), Amount(BigDecimal("-1000"))), TransactionEntryForm(AccountName("Repairs"), Amount(BigDecimal("1000")))))
    val serializedForm ="""{"payee":"Checking Account","description":"Roof repairs","postedOn":"2016-05-29","entries":[{"account":"Checking Account","amount":"-1000"},{"account":"Repairs","amount":"1000"}]}"""

    test("generates expected output") {
        val jsonString = JSON.mapper.writeValueAsString(transactionForm)
        assert(jsonString == serializedForm)
    }

    test("reads serialized form") {
        val deserialized = JSON.mapper.readValue(serializedForm, classOf[TransactionForm])
        assert(transactionForm == deserialized)
    }
}

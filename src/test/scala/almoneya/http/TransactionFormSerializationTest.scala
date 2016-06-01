package almoneya.http

import org.scalatest.FunSuite

class TransactionFormSerializationTest extends FunSuite {
    val transactionForm = TransactionForm(Some("Checking Account"), Some("Roof repairs"), Some("2016-05-29"),
        Set(TransactionEntryForm(Some("Checking Account"), Some("-1000")), TransactionEntryForm(Some("Repairs"), Some("1000"))),
        bankAccountTransactionId = Some("131"))
    val serializedForm ="""{"payee":"Checking Account","description":"Roof repairs","posted_on":"2016-05-29","entries":[{"account_name":"Checking Account","amount":"-1000"},{"account_name":"Repairs","amount":"1000"}],"bank_account_transaction_id":"131"}"""

    test("generates expected output") {
        val jsonString = JSON.mapper.writeValueAsString(transactionForm)
        assert(jsonString == serializedForm)
    }

    test("reads serialized form") {
        val deserialized = JSON.mapper.readValue(serializedForm, classOf[TransactionForm])
        assert(transactionForm == deserialized)
    }
}

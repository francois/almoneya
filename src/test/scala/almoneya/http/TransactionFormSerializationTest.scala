package almoneya.http

import almoneya._
import org.joda.time.LocalDate
import org.scalatest.FunSuite

class TransactionFormSerializationTest extends FunSuite {
    val transactionForm = TransactionForm(Payee("Checking Account"), Some(Description("Roof repairs")), new LocalDate("2016-05-29"),
        Set(TransactionEntryForm(AccountName("Checking Account"), Amount(BigDecimal("-1000"))), TransactionEntryForm(AccountName("Repairs"), Amount(BigDecimal("1000")))),
        bankAccountTransactionId = Some(BankAccountTransactionId(131)))
    val serializedForm ="""{"payee":"Checking Account","description":"Roof repairs","posted_on":"2016-05-29","entries":[{"account_name":"Checking Account","amount":"-1000"},{"account_name":"Repairs","amount":"1000"}],"bank_account_transaction_id":131}"""

    test("generates expected output") {
        val jsonString = JSON.mapper.writeValueAsString(transactionForm)
        assert(jsonString == serializedForm)
    }

    test("reads serialized form") {
        val deserialized = JSON.mapper.readValue(serializedForm, classOf[TransactionForm])
        assert(transactionForm == deserialized)
    }
}

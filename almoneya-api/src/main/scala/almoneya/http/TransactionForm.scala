package almoneya.http

import almoneya._
import org.joda.time.LocalDate

case class TransactionForm(payee: Option[String],
                           description: Option[String],
                           postedOn: Option[String],
                           entries: Set[TransactionEntryForm],
                           bankAccountTransactionId: Option[String]) {
    def toTransaction(accounts: Set[Account]): Transaction = {
        Transaction(payee = Payee(payee.get),
            description = description.map(Description.apply),
            postedOn = new LocalDate(postedOn.get),
            entries = buildEntries(accounts))
    }

    def buildEntries(accounts: Set[Account]): Set[TransactionEntry] = entries.map { entry =>
        TransactionEntry(account = accounts.find(_.name.value == entry.accountName.get).get,
            amount = Amount(BigDecimal(entry.amount.get)))
    }
}

object TransactionForm {

    import com.wix.accord.dsl._

    implicit val transactionFormValidator = validator[TransactionForm] { form =>
        form.payee is notEmpty
        form.payee.each is notEmpty

        form.postedOn is notEmpty
        form.postedOn.each is matchRegexFully(LocalDateEx.VALID_RE)

        (form.bankAccountTransactionId is empty) or ((form.bankAccountTransactionId is notEmpty) and (form.bankAccountTransactionId.each is matchRegexFully("""\d+""".r)))

        form.entries.size should be >= 2
        form.entries.each is valid
    }
}

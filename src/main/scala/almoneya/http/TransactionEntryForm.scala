package almoneya.http

case class TransactionEntryForm(accountName: Option[String],
                                amount: Option[String])

object TransactionEntryForm {

    import com.wix.accord.dsl._

    implicit val transactionEntryFormValidator = validator[TransactionEntryForm] { form =>
        form.accountName is notEmpty
        form.accountName.each is notEmpty

        form.amount is notEmpty
        form.amount.each is notEmpty
        form.amount.each is matchRegexFully("""-?\d+(?:\.\d+)?""".r)
    }
}

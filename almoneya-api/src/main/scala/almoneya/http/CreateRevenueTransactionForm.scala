package almoneya.http

import java.sql.Connection

import almoneya._
import com.fasterxml.jackson.core.{JsonParseException, JsonParser, JsonToken}
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.wix.accord.dsl._
import org.joda.time.LocalDate


case class CreateRevenueTransactionForm(revenueAccountName: Option[String],
                                        bankAccountAccountName: Option[String],
                                        payee: Option[String],
                                        receivedOn: Option[String],
                                        amount: Option[String],
                                        validAccounts: Set[Account] = Set.empty) {
    def toTransaction(implicit connection: Connection): Transaction = {
        val revenueAccount = validAccounts.find(_.name.isEqualTo(revenueAccountName.get))
        assert(revenueAccount.isDefined)

        val bankAccountAccount = validAccounts.find(_.name.isEqualTo(bankAccountAccountName.get))
        assert(bankAccountAccount.isDefined)

        Transaction(
            payee = Payee(payee.get),
            postedOn = new LocalDate(receivedOn.get),
            entries = Set(TransactionEntry(account = revenueAccount.get, amount = Amount(BigDecimal(amount.get)) * -1),
                TransactionEntry(account = bankAccountAccount.get, amount = Amount(BigDecimal(amount.get)))))
    }
}

object CreateRevenueTransactionForm {
    implicit val createRevenueTransactionFormValidator = validator[CreateRevenueTransactionForm] { form =>
        // this is really an assertion: it would mean the caller did not correctly provide the values we're looking for
        form.validAccounts is notEmpty

        form.payee is notEmpty
        form.payee.each is notEmpty

        form.receivedOn is notEmpty
        form.receivedOn.each is notEmpty
        form.receivedOn.each is matchRegexFully(LocalDateEx.VALID_RE)

        form.revenueAccountName is notEmpty
        form.revenueAccountName.each is notEmpty
        form.revenueAccountName.each is valid(AccountNameValidator(form.validAccounts).build)

        form.bankAccountAccountName is notEmpty
        form.bankAccountAccountName.each is notEmpty
        form.bankAccountAccountName.each is valid(AccountNameValidator(form.validAccounts).build)

        form.amount is notEmpty
        form.amount.each is notEmpty
        form.amount.each is matchRegexFully(Amount.VALID_RE)
    }
}

class CreateRevenueTransactionFormDeserializer extends StdDeserializer[CreateRevenueTransactionForm](classOf[CreateRevenueTransactionForm]) {
    override def deserialize(p: JsonParser, ctxt: DeserializationContext): CreateRevenueTransactionForm = {
        var revenueAccountName: Option[String] = None
        var bankAccountAccountName: Option[String] = None
        var payee: Option[String] = None
        var receivedOn: Option[String] = None
        var amount: Option[String] = None
        var fieldName: Option[String] = None
        var done: Boolean = false

        while (!done && p.hasCurrentToken) {
            p.getCurrentToken match {
                case JsonToken.FIELD_NAME =>
                    fieldName = Some(p.getValueAsString)
                    p.nextToken

                case JsonToken.VALUE_STRING =>
                    fieldName match {
                        case Some("revenueAccountName") | Some("revenue_account_name") => revenueAccountName = Some(p.getValueAsString)
                        case Some("bankAccountAccountName") | Some("bank_account_account_name") => bankAccountAccountName = Some(p.getValueAsString)
                        case Some("payee") | Some("payee") => payee = Some(p.getValueAsString)
                        case Some("received_on") | Some("receivedOn") => receivedOn = Some(p.getValueAsString)
                        case Some("amount") | Some("amount") => amount = Some(p.getValueAsString)

                        case Some(unknownFieldName) =>
                            throw new JsonParseException(p, "Unknown field named \"" + unknownFieldName + "\" found", p.getCurrentLocation)
                        case None => throw new JsonParseException(p, "Found a STRING without a fieldName, this is pretty bad!", p.getCurrentLocation)
                    }
                    p.nextToken

                case JsonToken.START_OBJECT =>
                    p.nextToken // all right, we're starting the object
                case JsonToken.END_OBJECT =>
                    p.nextToken
                    done = true

                case otherToken =>
                    throw new JsonParseException(p, "Unexpected \"" + otherToken.asString() + "\" token found! Only field names strings are expected. Yes, that means amount too!", p.getCurrentLocation)
            }
        }

        CreateRevenueTransactionForm(
            revenueAccountName = revenueAccountName,
            bankAccountAccountName = bankAccountAccountName,
            payee = payee,
            receivedOn = receivedOn,
            amount = amount)
    }
}

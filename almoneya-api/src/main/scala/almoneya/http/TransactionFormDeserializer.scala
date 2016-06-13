package almoneya.http

import com.fasterxml.jackson.core.{JsonParseException, JsonParser, JsonToken}
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer

class TransactionFormDeserializer extends StdDeserializer[TransactionForm](classOf[TransactionForm]) {

    sealed trait Field

    case object PayeeField extends Field

    case object DescriptionField extends Field

    case object PostedOnField extends Field

    case object EntriesField extends Field

    case object AccountNameField extends Field

    case object AmountField extends Field

    case object BankAccountTransactionIdField extends Field

    override def deserialize(p: JsonParser, ctxt: DeserializationContext): TransactionForm = {
        var payee: Option[String] = None
        var description: Option[String] = None
        var postedOn: Option[String] = None
        var entries: Set[TransactionEntryForm] = Set.empty
        var accountName: Option[String] = None
        var amount: Option[String] = None
        var bankAccountTransactionId: Option[String] = None
        var currentField: Option[Field] = None
        var done = false

        while (p.hasCurrentToken && !done) {
            p.getCurrentToken match {
                case JsonToken.FIELD_NAME =>
                    currentField = Some(p.getCurrentName match {
                        case "payee" => PayeeField
                        case "description" => DescriptionField
                        case "posted_on" | "postedOn" => PostedOnField
                        case "entries" => EntriesField
                        case "account" | "account_name" | "accountName" => AccountNameField
                        case "amount" => AmountField
                        case "bank_account_transaction_id" => BankAccountTransactionIdField
                        case other =>
                            throw new JsonParseException(p, "Found unknown FIELD named [" + other + "]: cannot continue", p.getCurrentLocation)
                    })

                case JsonToken.VALUE_STRING =>
                    currentField match {
                        case Some(PayeeField) => payee = Some(p.getValueAsString)
                        case Some(DescriptionField) => description = Some(p.getValueAsString)
                        case Some(PostedOnField) => postedOn = Some(p.getValueAsString)
                        case Some(EntriesField) => ()
                        case Some(AccountNameField) => accountName = Some(p.getValueAsString)
                        case Some(AmountField) => amount = Some(p.getValueAsString)
                        case Some(BankAccountTransactionIdField) => bankAccountTransactionId = Some(p.getValueAsString)
                        case None =>
                            throw new JsonParseException(p, "Found STRING when no current field", p.getCurrentLocation)
                    }

                case JsonToken.VALUE_NUMBER_INT =>
                    currentField match {
                        case Some(BankAccountTransactionIdField) => bankAccountTransactionId = Some(p.getValueAsString)
                        case Some(field) =>
                            throw new JsonParseException(p, "Found unexpected INT value in field [" + field + "]", p.getCurrentLocation)
                        case None =>
                            throw new JsonParseException(p, "Found STRING when no current field", p.getCurrentLocation)
                    }

                case JsonToken.END_ARRAY =>
                    entries = entries + TransactionEntryForm(accountName, amount)

                case JsonToken.END_OBJECT =>
                    // Either we're closing an entry, or we're closing the whole transaction form: make a decision here
                    currentField match {
                        case Some(AccountNameField) | Some(AmountField) =>
                            // closing an entry, we're fine
                            entries = entries + TransactionEntryForm(accountName, amount)
                            currentField = None
                        case _ =>
                            done = true
                    }

                case _ => ()
            }

            p.nextToken()
        }

        TransactionForm(payee, description, postedOn, entries, bankAccountTransactionId)
    }
}

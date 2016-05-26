package almoneya.http

import almoneya.{AccountName, Amount, Description, Payee}
import com.fasterxml.jackson.core.{JsonParseException, JsonParser, JsonToken}
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import org.joda.time.LocalDate

class TransactionFormDeserializer extends StdDeserializer[TransactionForm](classOf[TransactionForm]) {

    sealed trait Field

    case object PayeeField extends Field

    case object DescriptionField extends Field

    case object PostedOnField extends Field

    case object EntriesField extends Field

    case object AccountNameField extends Field

    case object AmountField extends Field

    override def deserialize(p: JsonParser, ctxt: DeserializationContext): TransactionForm = {
        var payee: Option[Payee] = None
        var description: Option[Description] = None
        var postedOn: Option[LocalDate] = None
        var entries: Set[TransactionEntryForm] = Set.empty
        var accountName: Option[AccountName] = None
        var amount: Option[Amount] = None
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
                        case other =>
                            throw new JsonParseException(p, "Found unknown FIELD named [" + other + "]: cannot continue", p.getCurrentLocation)

                    })

                case JsonToken.VALUE_STRING =>
                    currentField match {
                        case Some(PayeeField) => payee = Some(Payee(p.getValueAsString))
                        case Some(DescriptionField) => description = Some(Description(p.getValueAsString))
                        case Some(PostedOnField) => postedOn = Some(new LocalDate(p.getValueAsString))
                        case Some(EntriesField) => ()
                        case Some(AccountNameField) => accountName = Some(AccountName(p.getValueAsString))
                        case Some(AmountField) => amount = Some(Amount(BigDecimal(p.getValueAsString)))
                        case None =>
                            throw new JsonParseException(p, "Found STRING when no current field", p.getCurrentLocation)
                    }

                case JsonToken.END_ARRAY =>
                    entries = entries + TransactionEntryForm(accountName.get, amount.get)

                case JsonToken.END_OBJECT =>
                    // Either we're closing an entry, or we're closing the whole transaction form: make a decision here
                    currentField match {
                        case Some(AccountNameField) | Some(AmountField) =>
                            // closing an entry, we're fine
                            entries = entries + TransactionEntryForm(accountName.get, amount.get)
                            currentField = None
                        case _ =>
                            done = true
                    }

                case _ => ()
            }

            p.nextToken()
        }

        TransactionForm(payee.get, description, postedOn.get, entries)
    }
}

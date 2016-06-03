package almoneya.http

import almoneya._
import almoneya.automation.{Allocation, FundingGoal}
import almoneya.http.FrontController.Results
import com.fasterxml.jackson.core.{JsonFactory, JsonGenerator}
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.{ObjectMapper, SerializationFeature}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.joda.time.{LocalDate, ReadableInstant}
import org.slf4j.LoggerFactory

object JSON {
    lazy val mapper = prepareJacksonObjectMapper

    private[this] def prepareJacksonObjectMapper: ObjectMapper = {
        val jsonFactory = new JsonFactory()
        jsonFactory.enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN)
        jsonFactory.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET)

        val mapper = new ObjectMapper(jsonFactory)
        mapper.registerModule(DefaultScalaModule)
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        Option(System.getProperty("indent.json")) match {
            case Some("yes") | Some("true") => mapper.enable(SerializationFeature.INDENT_OUTPUT)
            case _ =>
                Option(System.getenv("INDENT_JSON")) match {
                    case Some("yes") | Some("true") => mapper.enable(SerializationFeature.INDENT_OUTPUT)
                    case _ => ()
                }
        }

        if (mapper.isEnabled(SerializationFeature.INDENT_OUTPUT)) {
            log.info("Using indented output for JSON")
        } else {
            log.info("Using compact output for JSON")
        }

        val serializerModule = new SimpleModule()

        serializerModule.addDeserializer(classOf[Amount], new AmountDeserializer())
        serializerModule.addDeserializer(classOf[Payee], new PayeeDeserializer())
        serializerModule.addDeserializer(classOf[TransactionForm], new TransactionFormDeserializer())

        serializerModule.addSerializer(classOf[AccountCode], new AccountCodeSerializer())
        serializerModule.addSerializer(classOf[AccountHash], new AccountHashSerializer())
        serializerModule.addSerializer(classOf[AccountId], new AccountIdSerializer())
        serializerModule.addSerializer(classOf[AccountKind], new AccountKindSerializer())
        serializerModule.addSerializer(classOf[AccountLast4], new AccountLast4Serializer())
        serializerModule.addSerializer(classOf[AccountName], new AccountNameSerializer())
        serializerModule.addSerializer(classOf[Account], new AccountSerializer())
        serializerModule.addSerializer(classOf[Allocation], new AllocationSerializer())
        serializerModule.addSerializer(classOf[Amount], new AmountSerializer())
        serializerModule.addSerializer(classOf[BankAccountId], new BankAccountIdSerializer())
        serializerModule.addSerializer(classOf[BankAccountTransactionId], new BankAccountTransactionIdSerializer())
        serializerModule.addSerializer(classOf[BankAccountTransaction], new BankAccountTransactionSerializer())
        serializerModule.addSerializer(classOf[BankAccount], new BankAccountSerializer())
        serializerModule.addSerializer(classOf[Description], new DescriptionSerializer())
        serializerModule.addSerializer(classOf[FundingGoal], new FundingGoalSerializer())
        serializerModule.addSerializer(classOf[Goal], new GoalSerializer())
        serializerModule.addSerializer(classOf[LocalDate], new LocalDateSerializer())
        serializerModule.addSerializer(classOf[Obligation], new ObligationSerializer())
        serializerModule.addSerializer(classOf[Payee], new PayeeSerializer())
        serializerModule.addSerializer(classOf[ReadableInstant], new ReadableInstantSerializer())
        serializerModule.addSerializer(classOf[Reconciliation], new ReconciliationSerializer())
        serializerModule.addSerializer(classOf[Notes], new NotesSerializer())
        serializerModule.addSerializer(classOf[ReconciliationId], new ReconciliationIdSerializer())
        serializerModule.addSerializer(classOf[ReconciliationEntry], new ReconciliationEntrySerializer())
        serializerModule.addSerializer(classOf[ReconciliationEntryId], new ReconciliationEntryIdSerializer())
        serializerModule.addSerializer(classOf[Results[_]], new ResultsSerializer())
        serializerModule.addSerializer(classOf[TransactionEntryId], new TransactionEntryIdSerializer())
        serializerModule.addSerializer(classOf[TransactionEntry], new TransactionEntrySerializer())
        serializerModule.addSerializer(classOf[TransactionForm], new TransactionFormSerializer())
        serializerModule.addSerializer(classOf[TransactionEntryForm], new TransactionEntryFormSerializer())
        serializerModule.addSerializer(classOf[TransactionId], new TransactionIdSerializer())
        serializerModule.addSerializer(classOf[Transaction], new TransactionSerializer())

        mapper.registerModule(serializerModule)
        mapper
    }

    private[this] val log = LoggerFactory.getLogger(this.getClass)
}

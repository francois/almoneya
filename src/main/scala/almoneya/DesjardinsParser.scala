package almoneya

import java.nio.charset.StandardCharsets

import org.joda.time.LocalDate

class DesjardinsParser {

    import RbcParser.digester

    def parse(records: Seq[Seq[String]]): Seq[BankAccountTransaction] = {
        records.filter(_.size >= 12).map { row =>
            val isLiability = row(1).trim().isEmpty
            val accountHash = AccountHash(rowToAccountHash(row))
            val last4 = if (isLiability) row(0).substring(row(0).length - 4) else row(1).substring(row(1).length - 4)
            val checkNumber = if (row(6).trim().isEmpty) None else Some(CheckNum(row(6).trim()))
            val postedOnComponents = row(3).split("""\D""").map(_.toInt)
            val postedOn = new LocalDate(postedOnComponents(0), postedOnComponents(1), postedOnComponents(2))
            val amount = if (isLiability) {
                if (row(11).trim.isEmpty) Amount(BigDecimal(row(12))) else Amount(BigDecimal(-1 * BigDecimal(row(11))))
            } else {
                if (row(7).trim.isEmpty) Amount(BigDecimal(row(8))) else Amount(BigDecimal(-1 * BigDecimal(row(7))))
            }

            BankAccountTransaction(bankAccount = BankAccount(accountNum = accountHash, last4 = AccountLast4(last4)), checkNum = checkNumber, postedOn = postedOn, description1 = Some(Description(row(5))), amount = amount)
        }
    }

    private[this] def rowToAccountHash(row: Seq[String]): String = {
        digester.digest(row.slice(0, 3).filterNot(_.isEmpty).map(_.trim()).mkString(" ").getBytes(StandardCharsets.UTF_8)) // Calculate the account's hash
                .map("%02x".format(_)) // Convert to hex bytes
                .mkString("") // Join everything together
    }
}

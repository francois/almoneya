package almoneya

import java.nio.charset.StandardCharsets
import java.security.MessageDigest

import org.joda.time.{DateTime, LocalDate}

class RbcParser {

    import RbcParser.digester

    def parse(records: Seq[Seq[String]]): Seq[BankAccountTransaction] = {
        val now = new DateTime()
        records.filterNot(_ (0) == "Type de compte").map { row =>
            val accountHash = rowToAccountHash(row)
            val accountNum = if (row(1).isEmpty) {
                // VISA
                row(0)
            } else {
                row(1)
            }
            val last4 = accountNum.substring(row(1).length - 4, row(1).length)
            val bankAccount = BankAccount(accountNum = AccountHash(accountHash), last4 = AccountLast4(last4), createdAt = now, updatedAt = now)
            val postedOnComponents = row(2).split("\\D").map(_.toInt)
            BankAccountTransaction(
                bankAccount = bankAccount,
                checkNum = if (row(3).isEmpty) None else Some(CheckNum(row(6))),
                postedOn = new LocalDate(postedOnComponents(2), postedOnComponents(0), postedOnComponents(1)),
                description1 = Some(Description(row(4))),
                description2 = Some(Description(row(5))),
                amount = Amount(BigDecimal(row(6)))
            )
        }
    }

    private[this] def rowToAccountHash(row: Seq[String]): String =
        digester.digest(row.slice(0, 1).filterNot(_.isEmpty).mkString(" ").getBytes(StandardCharsets.UTF_8)) // Calculate the account's hash
                .map("%02x".format(_)) // Convert to hex bytes
                .mkString("") // Join everything together
}

object RbcParser {
    val digester = MessageDigest.getInstance("SHA-256")
}

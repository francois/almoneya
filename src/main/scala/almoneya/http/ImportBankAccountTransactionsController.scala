package almoneya.http

import java.io.{InputStreamReader, Reader}
import java.nio.charset.Charset
import javax.servlet.http.HttpServletRequest

import almoneya._
import com.wix.accord.{RuleViolation, Violation}
import org.apache.commons.csv.CSVFormat
import org.eclipse.jetty.server.Request

import scala.collection.JavaConversions._

class ImportBankAccountTransactionsController(bankAccountTransactionsRepo: BankAccountTransactionsRepository) extends Controller {
    override def handle(tenantId: TenantId, baseRequest: Request, request: HttpServletRequest): Either[Iterable[Violation], AnyRef] = {
        val hasContentType = Option(request.getContentType).isDefined
        if (hasContentType && request.getContentType.startsWith("multipart/form-data")) {
            Option(request.getPart("file")) match {
                case Some(filePart) if TEXT_CSV_RE.findFirstIn(filePart.getContentType).isDefined =>
                    val reader = new InputStreamReader(filePart.getInputStream, Charset.forName("ISO-8859-1"))
                    bankAccountTransactionsRepo.transaction {
                        Right(importTransactions(tenantId, reader))
                    }

                case _ =>
                    Left(Set(RuleViolation(null, "When uploading files using multipart/form-data, we expect to find a file part, but we did not find it. Instead, we found the following part names: " + request.getParts.map(_.getName).mkString(", "), None)))
            }
        } else if (hasContentType && TEXT_CSV_RE.findFirstIn(request.getContentType).isDefined) {
            Right(importTransactions(tenantId, request.getReader))
        } else {
            Left(Set(RuleViolation(null, "Expected to find text/csv or multipart/form-data Content-Type but found " + Option(request.getContentType).getOrElse("[NULL]"), None)))
        }
    }

    def importTransactions(tenantId: TenantId, reader: Reader): Seq[BankAccountTransaction] = {
        val parser = CSVFormat.EXCEL.parse(reader)
        val rows = parser.getRecords
        val parsed = rows.map(row => (0 until row.size()).map(idx => row.get(idx))).toSeq
        parseToBankAccountTransactions(parsed) match {
            case Left(ex) => throw ex
            case Right(transactions) =>
                bankAccountTransactionsRepo.importBankTransactionsTransactions(tenantId, transactions)
        }
    }

    private[this] def parseToBankAccountTransactions(rows: Seq[Seq[String]]): Either[Exception, Seq[BankAccountTransaction]] = {
        val sizes = rows.map(_.size).toSet
        if (sizes.contains(14)) {
            Right(new DesjardinsParser().parse(rows))
        } else if (sizes.contains(8) || sizes.contains(9)) {
            Right(new RbcParser().parse(rows))
        } else {
            Left(new IllegalArgumentException("Unknown parser class to use for files with " + sizes + " columns"))
        }
    }

    private[this] val TEXT_CSV_RE = """\Atext/csv\b""".r
}

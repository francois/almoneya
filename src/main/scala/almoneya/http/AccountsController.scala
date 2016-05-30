package almoneya.http

import java.io.{BufferedReader, InputStreamReader}
import javax.servlet.http.HttpServletRequest

import almoneya._
import com.fasterxml.jackson.databind.ObjectMapper
import com.wix.accord._
import org.eclipse.jetty.server.Request
import org.joda.time.LocalDate

import scala.util.{Failure, Success, Try}

class AccountsController(private[this] val mapper: ObjectMapper, private[this] val accountsRepository: AccountsRepository) extends JsonApiController[Set[Account]](mapper) {
    override def process(tenantId: TenantId, baseRequest: Request, request: HttpServletRequest): Try[Set[Account]] = {
        Option(request.getPathInfo) match {
            case Some("/") | Some("/list") | None if Option(request.getMethod).map(_.toLowerCase).contains("get") =>
                accountsRepository.findAllWithBalance(tenantId, new LocalDate())

            case Some("/search") if Option(request.getMethod).map(_.toLowerCase).contains("get") =>
                val query = SearchForAccountForm(Option(request.getParameter("q")))
                validate(query) match {
                    case com.wix.accord.Success =>
                        accountsRepository.search(tenantId, query.q.get)

                    case com.wix.accord.Failure(violations) =>
                        Failure(new RuntimeException(violations.map(violation => violation.description.map(_ + " ").getOrElse("") + violation.constraint).mkString("; ")))
                }

            case Some("/create") if Option(request.getMethod).map(_.toLowerCase).contains("post") =>
                val (code: Option[String], name: Option[String], kind: Option[String], virtual: Option[String]) = Option(request.getContentType) match {
                    case Some(contentType) if contentType.startsWith("application/json") =>
                        (None, None, None, None)

                    case Some(contentType) if contentType.startsWith("multipart/form-data") =>
                        val results = Seq("code", "name", "kind", "virtual").map { partName =>
                            Option(request.getPart(partName)).map(_.getInputStream).map(new InputStreamReader(_)).map(new BufferedReader(_)).map(_.readLine())
                        }

                        (results(0), results(1), results(2), results(3))
                }

                val accountForm = AccountForm(code, name, kind, virtual)
                validate(accountForm) match {
                    case com.wix.accord.Success =>
                        accountsRepository.create(tenantId, accountForm.toAccount).map(Set(_))

                    case com.wix.accord.Failure(violations) =>
                        Failure(new RuntimeException(violations.map(violation => violation.description.map(_ + " ").getOrElse("") + violation.constraint).mkString("; ")))
                }

            case Some(pathWithId) if Option(request.getMethod).map(_.toLowerCase).contains("get") =>
                Try(pathWithId.split("/").lastOption).map(_.map(_.toInt)) match {
                    case Success(Some(id)) => Success(Set.empty)

                    case Success(None) =>
                        Failure(new RuntimeException("Failed to extract ID from PATH_INFO [" + pathWithId + "]"))

                    case Failure(ex) =>
                        Failure(new RuntimeException("Failed to extract ID from PATH_INFO [" + pathWithId + "]", ex))
                }

            case _ =>
                Failure(new NotFoundException("Unrecognized path [" + request.getPathInfo + "] with HTTP method " + Option(request.getMethod).getOrElse("UNKNOWN")))
        }
    }
}

package almoneya.http

import java.sql.Connection

import almoneya.{TenantId, TransactionId, TransactionsRepository}
import com.wix.accord.{Failure => AccordFailure, Result, RuleViolation, Success => AccordSuccess, Validator}

import scala.util.{Failure => ScalaFailure, Success => ScalaSuccess, Try}

case class TransactionIdValidatorBuilder(tenantId: TenantId, transactionsRepository: TransactionsRepository) {
    def build: Validator[String] = {
        new Validator[String] {
            override def apply(transactionId: String): Result = {
                AppConnection.currentConnection.get() match {
                    case Some(conn) =>
                        implicit val connection = conn
                        Try(transactionId.toInt).map(TransactionId.apply).map { id =>
                            if (transactionsRepository.exists(tenantId, id)) {
                                AccordSuccess
                            } else {
                                AccordFailure(Set(RuleViolation(transactionId, "must already exist", Some("transactionId"))))
                            }
                        } match {
                            case ScalaSuccess(result) => result
                            case ScalaFailure(ex) => AccordFailure(Set(RuleViolation(transactionId, "is not a TransactionId", Some("transactionId"))))
                        }

                    case None =>
                        throw new RuntimeException("Required connection parameter not available in AppConnection.currentConnection")
                }
            }
        }
    }
}

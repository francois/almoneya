package almoneya.http

import almoneya.TransactionId
import com.wix.accord.{Failure => AccordFailure, Result, RuleViolation, Success => AccordSuccess, Validator}

import scala.util.{Failure => ScalaFailure, Success => ScalaSuccess, Try}

case class TransactionIdValidatorBuilder(validTransactionIds: Set[TransactionId]) {
    def build: Validator[String] = {
        new Validator[String] {
            override def apply(transactionId: String): Result = {
                val result = for (txnId <- Try(transactionId.toInt)) yield {
                    if (validTransactionIds.contains(TransactionId(txnId))) {
                        AccordSuccess
                    } else {
                        AccordFailure(Set(RuleViolation(transactionId, "must already exist", Some("transactionId"))))
                    }
                }

                result match {
                    case ScalaSuccess(realResult) => realResult
                    case ScalaFailure(_) => AccordFailure(Set(RuleViolation(transactionId, "must be an integer", Some("transactionId"))))
                }
            }
        }
    }
}

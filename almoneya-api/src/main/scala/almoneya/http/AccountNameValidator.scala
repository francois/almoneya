package almoneya.http

import almoneya.Account
import com.wix.accord._

case class AccountNameValidator(validAccounts: Set[Account]) {
    def build: Validator[String] = new Validator[String] {
        override def apply(name: String): Result = {
            validAccounts.find(_.name.isEqualTo(name)) match {
                case Some(account) => Success
                case None => Failure(Set(RuleViolation(name, "must already exist", Some("accountName"))))
            }
        }
    }
}

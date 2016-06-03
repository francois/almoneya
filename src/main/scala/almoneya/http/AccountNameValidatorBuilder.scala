package almoneya.http

import almoneya.{AccountsRepository, TenantId}
import com.wix.accord._

case class AccountNameValidatorBuilder(tenantId: TenantId, accountsRepository: AccountsRepository) {
    def build: Validator[String] = new Validator[String] {
        override def apply(name: String): Result = {
            AppConnection.currentConnection.get() match {
                case Some(conn) =>
                    implicit val connection = conn
                    accountsRepository.findAll(tenantId).find(_.name.isEqualTo(name)) match {
                        case Some(_) => Success
                        case None => Failure(Set(RuleViolation(name, "must already exist", Some("accountName"))))
                    }

                case None =>
                    throw new RuntimeException("Required connection parameter not available in AppConnection.currentConnection")
            }
        }
    }
}

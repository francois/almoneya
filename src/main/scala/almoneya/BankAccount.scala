package almoneya

import org.joda.time.DateTime

case class BankAccount(id: Option[BankAccountId] = None,
                       accountNum: AccountHash,
                       last4: AccountLast4,
                       account: Option[Account] = None,
                       createdAt: DateTime = new DateTime,
                       updatedAt: DateTime = new DateTime)

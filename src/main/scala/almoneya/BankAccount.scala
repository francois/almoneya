package almoneya

case class BankAccount(id: Option[BankAccountId] = None,
                       accountHash: AccountHash,
                       last4: AccountLast4,
                       account: Option[Account] = None)

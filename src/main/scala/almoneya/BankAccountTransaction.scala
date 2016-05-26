package almoneya

import org.joda.time.LocalDate

case class BankAccountTransaction(id: Option[BankAccountTransactionId] = None,
                                  bankAccount: BankAccount,
                                  checkNum: Option[CheckNum] = None,
                                  postedOn: LocalDate,
                                  description1: Option[Description] = None,
                                  description2: Option[Description] = None,
                                  amount: Amount)

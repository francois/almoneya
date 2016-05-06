package almoneya

import org.joda.time.{DateTime, LocalDate}

case class BankAccountTransaction(id: Option[BankAccountTransactionId] = None,
                                  bankAccount: BankAccount,
                                  checkNum: Option[CheckNum] = None,
                                  postedOn: LocalDate,
                                  description1: Option[TransactionDescription] = None,
                                  description2: Option[TransactionDescription] = None,
                                  amount: Amount,
                                  createdAt: DateTime = new DateTime(),
                                  updatedAt: DateTime = new DateTime())

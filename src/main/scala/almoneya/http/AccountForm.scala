package almoneya.http

import almoneya.{Account, AccountCode, AccountKind, AccountName}
import com.wix.accord.dsl._

case class AccountForm(code: Option[String], name: Option[String], kind: Option[String], virtual: Option[String]) {
    /**
      * Converts this Form object into an [[almoneya.Account]] model.
      *
      * Callers are responsible for calling this method after successful validation only,
      * else exceptions will be raised.
      *
      * @return An [[Account]] which has the values that were in this form.
      */
    def toAccount: Account = {
        Account(code = code.map(_.trim).map(AccountCode.apply),
            name = name.map(_.trim).map(AccountName.apply).get,
            kind = kind.map(AccountKind.fromString).get,
            virtual = virtual.map(_.toBoolean).get)
    }
}

object AccountForm {
    implicit val accountFormValidator = validator[AccountForm] { form =>
        (form.code is empty) or (form.code.each is notEmpty)
        form.name is notEmpty
        form.name.each is notEmpty
        form.kind is notEmpty
        form.kind.each is matchRegexFully("""(asset|liability|equity|revenue|expense|contra)""".r)
        form.virtual is notEmpty
        form.virtual.each is matchRegexFully("""(true|false)""".r)
    }
}

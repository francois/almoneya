package almoneya.http

import com.wix.accord.dsl._

case class SearchForAccountForm(q: Option[String])

object SearchForAccountForm {
    implicit val searchForAccountFormValidator = validator[SearchForAccountForm] { form =>
        form.q is notEmpty
        form.q.each is notEmpty
    }
}

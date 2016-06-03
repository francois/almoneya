package almoneya.http

import com.wix.accord._
import org.scalatest.FunSuite

class SearchForAccountFormValidationTest extends FunSuite {
    test("fails on absence of q") {
        val form = SearchForAccountForm(None)
        assert(validate(form) == Failure(Set(RuleViolation(None, "must not be empty", Some("q")))))
    }

    test("fails on the empty string") {
        val form = SearchForAccountForm(Some(""))
        assert(validate(form) == Failure(Set(RuleViolation("", "must not be empty", Some("q")))))
    }

    test("succeeds on \"checking\"") {
        val form = SearchForAccountForm(Some("checking"))
        assert(validate(form) == Success)
    }
}

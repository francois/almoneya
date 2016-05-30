package almoneya.http

import com.wix.accord._
import org.scalatest.FunSuite

class AccountFormValidationTest extends FunSuite {

    import AccountFormValidationTest.validAccountForm

    test("a code of None is acceptable") {
        val form = validAccountForm.copy(code = None)
        assert(validate(form) == Success)
    }

    test("a code of Some(\"CHECK\") is acceptable") {
        val form = validAccountForm.copy(code = Some("CHECK"))
        assert(validate(form) == Success)
    }

    test("a code of Some(\"\") is unacceptable") {
        val form = validAccountForm.copy(code = Some(""))
        assert(validate(form).isFailure)
    }

    test("a kind of Some(\"asset\") is acceptable") {
        val form = validAccountForm.copy(kind = Some("asset"))
        assert(validate(form) == Success)
    }

    test("a kind of Some(\" asset\") is unacceptable") {
        val form = validAccountForm.copy(kind = Some(" asset"))
        assert(validate(form).isFailure)
    }

    test("a kind of Some(\"asset \") is unacceptable") {
        val form = validAccountForm.copy(kind = Some("asset "))
        assert(validate(form).isFailure)
    }

    test("a kind of None is unacceptable") {
        val form = validAccountForm.copy(kind = None)
        assert(validate(form).isFailure)
    }
}

object AccountFormValidationTest {
    val validAccountForm = AccountForm(Some("CHECK"), name = Some("Checking"), kind = Some("asset"), virtual = Some("false"))
}

package almoneya

import org.mindrot.jbcrypt.BCrypt

case class PasswordHash(value: String) {
    def matches(candidate: Password) = BCrypt.checkpw(candidate.value, value)
}

object PasswordHash {
    def create(password: Password) = PasswordHash(BCrypt.hashpw(password.value, BCrypt.gensalt(12)))
}

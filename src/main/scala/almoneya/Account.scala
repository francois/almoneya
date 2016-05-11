package almoneya

import java.sql.PreparedStatement

case class Account(id: Option[AccountId] = None,
                   name: AccountName,
                   kind: AccountKind)

sealed trait AccountKind extends SqlValue {
    override def setParam(statement: PreparedStatement, index: Int): Unit = statement.setString(1 + index, kindName)

    def kindName: String
}

object AccountKind {
    def fromString(kindName: String): AccountKind = kindName match {
        case "asset" => Asset
        case "liability" => Liability
        case "equity" => Equity
        case "revenue" => Revenue
        case "expense" => Expense
        case "contra" => Contra
    }
}

case object Asset extends AccountKind {
    override def kindName: String = "asset"
}

case object Liability extends AccountKind {
    override def kindName: String = "liability"
}

case object Equity extends AccountKind {
    override def kindName: String = "equity"
}

case object Expense extends AccountKind {
    override def kindName: String = "expense"
}

case object Revenue extends AccountKind {
    override def kindName: String = "revenue"
}

case object Contra extends AccountKind {
    override def kindName: String = "contra"
}

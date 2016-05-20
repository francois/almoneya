package almoneya.automation

case class Priority(value: Int) extends Ordered[Priority] {
    override def compare(that: Priority): Int = value.compare(that.value)
}

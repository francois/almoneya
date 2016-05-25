package almoneya

case class Priority(value: Int) extends Comparable[Priority] {
    def compareTo(that: Priority): Int = value.compareTo(that.value)
}

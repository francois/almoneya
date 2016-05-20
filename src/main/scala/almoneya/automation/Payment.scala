package almoneya.automation

import almoneya.Amount

case class Payment(goal: FundingGoal, planToTake: Amount, realTake: Amount = Amount(0)) {
    def fulfilled = realTake >= planToTake
}

require "dry-validation"

module Schemas
  ObligationSchema = Dry::Validation.Form do
    key(:description) { filled? }
    key(:every)       { filled? & int? & gteq?(1) }
    key(:period)      { filled? & inclusion?(%w(day week month year)) }
    key(:start_on)    { date? }
    key(:end_on)      { none? | date? }
    key(:amount)      { filled? & decimal? & gteq?(0.01) }
  end
end

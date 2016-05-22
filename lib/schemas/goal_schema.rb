require "dry-validation"

module Schemas
  GoalSchema = Dry::Validation.Form do
    key(:description) { filled? }
    key(:amount)      { filled? & decimal? & gteq?(0.01) }
    key(:due_on)      { filled? & date? }
  end
end

require "dry-validation"

module Schemas
  NewAccountSchema = Dry::Validation.Form do
    key(:code) { none? | format?(/\A[0-9]{1,6}\z/) }
    key(:name) { filled? }
    key(:kind) { filled? & inclusion?(%w(asset liability equity revenue expense contra)) }
  end
end

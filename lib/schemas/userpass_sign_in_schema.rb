require "dry-validation"

module Schemas
  UserpassSignInSchema = Dry::Validation.Form do
    key(:username).required
    key(:password).required
  end
end

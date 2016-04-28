module Repositories
  User = Struct.new(:user_id, :surname, :rest_of_name) do
    alias_method :id, :user_id
  end
end

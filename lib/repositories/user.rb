module Repositories
  User = Struct.new(:tenant_id, :user_id, :surname, :rest_of_name) do
    alias_method :id, :user_id
  end
end

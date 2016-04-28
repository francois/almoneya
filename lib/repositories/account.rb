module Repositories
  Account = Struct.new(:account_id, :code, :name, :kind, :created_at, :updated_at) do
    alias_method :id, :account_id
  end
end
